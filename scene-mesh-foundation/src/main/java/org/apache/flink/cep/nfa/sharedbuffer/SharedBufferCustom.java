/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOVICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Vhe ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.cep.nfa.sharedbuffer;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.annotation.VisibleForTesting;

import java.lang.reflect.Constructor;
import org.apache.flink.api.common.state.KeyedStateStore;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.base.IntSerializer;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.cep.configuration.SharedBufferCacheConfig;
import org.apache.flink.cep.nfa.DeweyNumber;
import org.apache.flink.cep.nfa.NFAState;
import org.apache.flink.runtime.state.KeyedStateBackend;
import org.apache.flink.runtime.state.VoidNamespace;
import org.apache.flink.runtime.state.VoidNamespaceSerializer;
import org.apache.flink.shaded.guava31.com.google.common.cache.Cache;
import org.apache.flink.shaded.guava31.com.google.common.cache.CacheBuilder;
import org.apache.flink.shaded.guava31.com.google.common.cache.RemovalCause;
import org.apache.flink.shaded.guava31.com.google.common.cache.RemovalListener;
import org.apache.flink.shaded.guava31.com.google.common.collect.Iterables;
import org.apache.flink.util.WrappingRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A shared buffer implementation which stores values under according state. Additionally, the
 * values can be versioned such that it is possible to retrieve their predecessor element in the
 * buffer.
 *
 * <p>The idea of the implementation is to have a buffer for incoming events with unique ids
 * assigned to them. This way we do not need to deserialize events during processing and we factory
 * only one copy of the event.
 *
 * <p>The entries in {@link SharedBuffer} are {@link SharedBufferNode}. The shared buffer node
 * allows to factory relations between different entries. A dewey versioning scheme allows to
 * discriminate between different relations (e.g. preceding element).
 *
 * <p>The implementation is strongly based on the paper "Efficient Pattern Matching over Event
 * Streams".
 *
 * @param <V> Type of the values
 * @see <a href="https://people.cs.umass.edu/~yanlei/publications/sase-sigmod08.pdf">
 * https://people.cs.umass.edu/~yanlei/publications/sase-sigmod08.pdf</a>
 */
@Slf4j
public class SharedBufferCustom<V> extends SharedBuffer<V> {

    private static final String LEGACY_ENTRIES_STATE_NAME = "sharedBuffer-entries";
    private static final String ENTRIES_STATE_NAME = "sharedBuffer-entries-with-lockable-edges";
    private static final String EVENTS_STATE_NAME = "sharedBuffer-events";
    private static final String EVENTS_COUNT_STATE_NAME = "sharedBuffer-events-count";

    private final MapState<EventId, Lockable<V>> eventsBuffer;
    /**
     * The number of events seen so far in the stream per timestamp.
     */
    private final MapState<Long, Integer> eventsCount;

    private final MapState<NodeId, Lockable<SharedBufferNode>> entries;

    /**
     * The cache of eventsBuffer State.
     */
    private final Cache<EventId, Lockable<V>> eventsBufferCache;

    /**
     * The cache of sharedBufferNode.
     */
    private final Cache<NodeId, Lockable<SharedBufferNode>> entryCache;

    private final Timer cacheStatisticsTimer;

    @VisibleForTesting
    public SharedBufferCustom(KeyedStateStore stateStore, TypeSerializer<V> valueSerializer) {
        this(stateStore, valueSerializer, new SharedBufferCacheConfig());
    }

    public SharedBufferCustom(
            KeyedStateStore stateStore,
            TypeSerializer<V> valueSerializer,
            SharedBufferCacheConfig cacheConfig) {
        super(stateStore, valueSerializer);
        this.eventsBuffer =
                stateStore.getMapState(
                        new MapStateDescriptor<>(
                                EVENTS_STATE_NAME,
                                EventId.EventIdSerializer.INSTANCE,
                                new Lockable.LockableTypeSerializer<>(valueSerializer)));

        this.entries =
                stateStore.getMapState(
                        new MapStateDescriptor<>(
                                ENTRIES_STATE_NAME,
                                new NodeId.NodeIdSerializer(),
                                new Lockable.LockableTypeSerializer<>(
                                        new SharedBufferNodeSerializer())));

        this.eventsCount =
                stateStore.getMapState(
                        new MapStateDescriptor<>(
                                EVENTS_COUNT_STATE_NAME,
                                LongSerializer.INSTANCE,
                                IntSerializer.INSTANCE));

        // set the events buffer cache and strategy of exchanging out
        this.eventsBufferCache =
                CacheBuilder.newBuilder()
                        .maximumSize(cacheConfig.getEventsBufferCacheSlots())
                        .removalListener(
                                (RemovalListener<EventId, Lockable<V>>)
                                        removalNotification -> {
                                            if (RemovalCause.SIZE
                                                    == removalNotification.getCause()) {
                                                try {
                                                    eventsBuffer.put(
                                                            removalNotification.getKey(),
                                                            removalNotification.getValue());
                                                } catch (Exception e) {
                                                    log.error(
                                                            "Error in putting value into eventsBuffer.",
                                                            e);
                                                }
                                            }
                                        })
                        .build();
        // set the entry cache and strategy of exchanging out
        this.entryCache =
                CacheBuilder.newBuilder()
                        .maximumSize(cacheConfig.getEntryCacheSlots())
                        .removalListener(
                                (RemovalListener<NodeId, Lockable<SharedBufferNode>>)
                                        removalNotification -> {
                                            if (RemovalCause.SIZE
                                                    == removalNotification.getCause()) {
                                                try {
                                                    entries.put(
                                                            removalNotification.getKey(),
                                                            removalNotification.getValue());
                                                } catch (Exception e) {
                                                    log.error(
                                                            "Error in putting value into entries.",
                                                            e);
                                                }
                                            }
                                        })
                        .build();
        cacheStatisticsTimer = new Timer();
        cacheStatisticsTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        log.info(
                                "Statistics details of eventsBufferCache: {}, statistics details of entryCache: {}.",
                                eventsBufferCache.stats(),
                                entryCache.stats());
                    }
                },
                cacheConfig.getCacheStatisticsInterval().toMillis(),
                cacheConfig.getCacheStatisticsInterval().toMillis());
    }

    public void migrateOldState(
            KeyedStateBackend<?> stateBackend, ValueState<NFAState> computationStates)
            throws Exception {
        stateBackend.applyToAllKeys(
                VoidNamespace.INSTANCE,
                VoidNamespaceSerializer.INSTANCE,
                new MapStateDescriptor<>(
                        LEGACY_ENTRIES_STATE_NAME,
                        new NodeId.NodeIdSerializer(),
                        new Lockable.LockableTypeSerializer<>(
                                new SharedBufferNode.SharedBufferNodeSerializer())),
                (key, state) -> {
                    copyEntries(state);
                    state.entries().forEach(this::lockPredecessorEdges);
                    state.clear();

                    NFAState nfaState = computationStates.value();
                    nfaState.getPartialMatches()
                            .forEach(
                                    computationState ->
                                            lockEdges(
                                                    computationState.getPreviousBufferEntry(),
                                                    computationState.getVersion()));
                    nfaState.getCompletedMatches()
                            .forEach(
                                    computationState ->
                                            lockEdges(
                                                    computationState.getPreviousBufferEntry(),
                                                    computationState.getVersion()));
                });
    }

    private void copyEntries(MapState<NodeId, Lockable<SharedBufferNode>> state) throws Exception {
        state.entries()
                .forEach(
                        e -> {
                            try {
                                entries.put(e.getKey(), e.getValue());
                            } catch (Exception exception) {
                                throw new RuntimeException(exception);
                            }
                        });
    }

    private void lockPredecessorEdges(Map.Entry<NodeId, Lockable<SharedBufferNode>> e) {
        SharedBufferNode oldNode = e.getValue().getElement();
        oldNode.getEdges()
                .forEach(
                        edge -> {
                            SharedBufferEdge oldEdge = edge.getElement();
                            lockEdges(oldEdge.getTarget(), oldEdge.getDeweyNumber());
                        });
    }

    private void lockEdges(NodeId nodeId, DeweyNumber version) {

        if (nodeId == null) {
            return;
        }

        try {
            SharedBufferNode newNode = entries.get(nodeId).getElement();
            newNode.getEdges()
                    .forEach(
                            newEdge -> {
                                if (version.isCompatibleWith(
                                        newEdge.getElement().getDeweyNumber())) {
                                    newEdge.lock();
                                }
                            });
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Construct an accessor to deal with this sharedBuffer.
     *
     * @return an accessor to deal with this sharedBuffer.
     */
    public SharedBufferAccessor<V> getAccessor() {
        try {
            log.debug("=== 开始创建 SharedBufferAccessor ===");
            log.debug("当前类加载器: {}", this.getClass().getClassLoader());
            log.debug("SharedBufferAccessor 类加载器: {}", SharedBufferAccessor.class.getClassLoader());
            log.debug("SharedBuffer 类加载器: {}", SharedBuffer.class.getClassLoader());
            
            // 检查类是否存在
            Class<?> accessorClass = SharedBufferAccessor.class;
            log.debug("SharedBufferAccessor 类存在: {}", accessorClass.getName());
            
            // 检查构造函数
            java.lang.reflect.Constructor<?>[] constructors = accessorClass.getDeclaredConstructors();
            log.debug("SharedBufferAccessor 构造函数数量: {}", constructors.length);
            for (int i = 0; i < constructors.length; i++) {
                java.lang.reflect.Constructor<?> constructor = constructors[i];
                log.debug("构造函数[{}]: 参数数量={}, 修饰符={}, 是否可访问={}",
                    i, constructor.getParameterCount(), 
                    java.lang.reflect.Modifier.toString(constructor.getModifiers()),
                    constructor.isAccessible());
            }
            
            // 尝试使用反射创建实例（绕过类加载器隔离）
            log.debug("尝试使用反射创建 SharedBufferAccessor 实例...");
            
            // 使用反射绕过访问权限限制
            Constructor<SharedBufferAccessor> constructor = SharedBufferAccessor.class.getDeclaredConstructor(SharedBuffer.class);
            constructor.setAccessible(true); // 绕过访问权限检查
            
            SharedBufferAccessor<V> accessor = constructor.newInstance(this);
            log.debug("=== SharedBufferAccessor 创建成功 ===");
            return accessor;
            
        } catch (NoClassDefFoundError e) {
            log.error("=== 类找不到错误 ===");
            log.error("错误类型: NoClassDefFoundError");
            log.error("错误信息: {}", e.getMessage());
            log.error("堆栈跟踪:", e);
            throw e;
        } catch (IllegalAccessError e) {
            log.error("=== 访问权限错误 ===");
            log.error("错误类型: IllegalAccessError");
            log.error("错误信息: {}", e.getMessage());
            log.error("堆栈跟踪:", e);
            throw e;
        } catch (Exception e) {
            log.error("=== 其他错误 ===");
            log.error("错误类型: {}", e.getClass().getSimpleName());
            log.error("错误信息: {}", e.getMessage());
            log.error("堆栈跟踪:", e);
            throw new RuntimeException("Failed to create SharedBufferAccessor", e);
        }
    }

    void advanceTime(long timestamp) throws Exception {
        Iterator<Long> iterator = eventsCount.keys().iterator();
        while (iterator.hasNext()) {
            Long next = iterator.next();
            if (next < timestamp) {
                iterator.remove();
            }
        }
    }

    EventId registerEvent(V value, long timestamp) throws Exception {
        Integer id = eventsCount.get(timestamp);
        if (id == null) {
            id = 0;
        }
        EventId eventId = new EventId(id, timestamp);
        Lockable<V> lockableValue = new Lockable<>(value, 1);
        eventsCount.put(timestamp, id + 1);
        eventsBufferCache.put(eventId, lockableValue);
        return eventId;
    }

    /**
     * Checks if there is no elements in the buffer.
     *
     * @return true if there is no elements in the buffer
     * @throws Exception Thrown if the system cannot access the state.
     */
    public boolean isEmpty() throws Exception {
        return Iterables.isEmpty(eventsBufferCache.asMap().keySet())
                && Iterables.isEmpty(eventsBuffer.keys());
    }

    public void releaseCacheStatisticsTimer() {
        if (cacheStatisticsTimer != null) {
            cacheStatisticsTimer.cancel();
        }
    }

    /**
     * Inserts or updates an event in cache.
     *
     * @param eventId id of the event
     * @param event   event body
     */
    void upsertEvent(EventId eventId, Lockable<V> event) {
        this.eventsBufferCache.put(eventId, event);
    }

    /**
     * Inserts or updates a shareBufferNode in cache.
     *
     * @param nodeId id of the event
     * @param entry  SharedBufferNode
     */
    void upsertEntry(NodeId nodeId, Lockable<SharedBufferNode> entry) {
        this.entryCache.put(nodeId, entry);
    }

    /**
     * Removes an event from cache and state.
     *
     * @param eventId id of the event
     */
    void removeEvent(EventId eventId) throws Exception {
        this.eventsBufferCache.invalidate(eventId);
        this.eventsBuffer.remove(eventId);
    }

    /**
     * Removes a ShareBufferNode from cache and state.
     *
     * @param nodeId id of the event
     */
    void removeEntry(NodeId nodeId) throws Exception {
        this.entryCache.invalidate(nodeId);
        this.entries.remove(nodeId);
    }

    /**
     * It always returns node either from state or cache.
     *
     * @param nodeId id of the node
     * @return SharedBufferNode
     */
    Lockable<SharedBufferNode> getEntry(NodeId nodeId) {
        try {
            Lockable<SharedBufferNode> lockableFromCache = entryCache.getIfPresent(nodeId);
            if (Objects.nonNull(lockableFromCache)) {
                return lockableFromCache;
            } else {
                Lockable<SharedBufferNode> lockableFromState = entries.get(nodeId);
                if (Objects.nonNull(lockableFromState)) {
                    entryCache.put(nodeId, lockableFromState);
                }
                return lockableFromState;
            }
        } catch (Exception ex) {
            throw new WrappingRuntimeException(ex);
        }
    }

    /**
     * It always returns event either from state or cache.
     *
     * @param eventId id of the event
     * @return event
     */
    Lockable<V> getEvent(EventId eventId) {
        try {
            Lockable<V> lockableFromCache = eventsBufferCache.getIfPresent(eventId);
            if (Objects.nonNull(lockableFromCache)) {
                return lockableFromCache;
            } else {
                Lockable<V> lockableFromState = eventsBuffer.get(eventId);
                if (Objects.nonNull(lockableFromState)) {
                    eventsBufferCache.put(eventId, lockableFromState);
                }
                return lockableFromState;
            }
        } catch (Exception ex) {
            throw new WrappingRuntimeException(ex);
        }
    }

    /**
     * Flush the event and node from cache to state.
     *
     * @throws Exception Thrown if the system cannot access the state.
     */
    void flushCache() throws Exception {
        if (!entryCache.asMap().isEmpty()) {
            entries.putAll(entryCache.asMap());
            entryCache.invalidateAll();
        }
        if (!eventsBufferCache.asMap().isEmpty()) {
            eventsBuffer.putAll(eventsBufferCache.asMap());
            eventsBufferCache.invalidateAll();
        }
    }


    public void clear() {
        eventsBuffer.clear();
        entries.clear();
    }

    @VisibleForTesting
    Iterator<Map.Entry<Long, Integer>> getEventCounters() throws Exception {
        return eventsCount.iterator();
    }

    @VisibleForTesting
    public int getEventsBufferCacheSize() {
        return (int) eventsBufferCache.size();
    }

    @VisibleForTesting
    public int getEventsBufferSize() throws Exception {
        return Iterables.size(eventsBuffer.entries());
    }

    @VisibleForTesting
    public int getSharedBufferNodeSize() throws Exception {
        return Iterables.size(entries.entries());
    }

    @VisibleForTesting
    public int getSharedBufferNodeCacheSize() throws Exception {
        return (int) entryCache.size();
    }
}
