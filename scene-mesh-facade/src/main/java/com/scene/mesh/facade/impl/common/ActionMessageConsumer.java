package com.scene.mesh.facade.impl.common;

import com.scene.mesh.facade.spec.common.IMessageExchanger;
import com.scene.mesh.foundation.spec.message.IMessageConsumer;
import com.scene.mesh.foundation.spec.message.MessageTopic;
import com.scene.mesh.model.action.Action;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ActionMessageConsumer {

    private final IMessageConsumer messageConsumer;
    private final IMessageExchanger messageExchanger;
    private final ExecutorService executorService;
    private final MessageTopic outboundActionTopic;

    private volatile boolean running = false;
    private int consecutiveErrors = 0;
    private static final int MAX_CONSECUTIVE_ERRORS = 5;

    public ActionMessageConsumer(IMessageConsumer messageConsumer,
                                 IMessageExchanger messageExchanger,
                                 MessageTopic outboundActionTopic) {
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "action-message-consumer");
            t.setDaemon(true);
            return t;
        });
        this.messageConsumer = messageConsumer;
        this.messageExchanger = messageExchanger;
        this.outboundActionTopic = outboundActionTopic;
        this.startConsume();
    }

    public void startConsume() {
        if (running) {
            log.warn("ActionMessageConsumer 已经在运行中");
            return;
        }

        running = true;
        consecutiveErrors = 0;
        log.info("启动 ActionMessageConsumer，监听主题: {}", outboundActionTopic.getTopicName());

        this.executorService.submit(() -> {
            while (running) {
                try {
                    List<Action> actions = messageConsumer.receive(outboundActionTopic, Action.class);

                    if (actions == null || actions.isEmpty()) {
                        // 没有消息时重置错误计数
                        consecutiveErrors = 0;
                        continue;
                    }

                    log.debug("接收到 {} 个 action", actions.size());

                    // 处理每个 action，单个失败不影响其他
                    for (Action action : actions) {
                        try {
                            messageExchanger.handleOutboundAction(action);
                            log.debug("action 已处理: {}", action.getId());
                        } catch (Exception e) {
                            log.error("处理 action 失败: actionId={}, error={}",
                                    action.getId(), e.getMessage(), e);
                            // 单个 action 处理失败不影响其他 action
                        }
                    }

                    // 成功处理消息后重置错误计数
                    consecutiveErrors = 0;

                } catch (Exception e) {
                    consecutiveErrors++;
                    log.error("接收消息失败 (连续错误次数: {}/{}): {}",
                            consecutiveErrors, MAX_CONSECUTIVE_ERRORS, e.getMessage(), e);

                    // 连续错误过多时停止消费
                    if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                        log.error("连续错误次数达到上限，停止 ActionMessageConsumer");
                        stopConsume();
                        break;
                    }

                    // 短暂休眠后重试
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("ActionMessageConsumer 被中断");
                        break;
                    }
                }
            }

            log.info("ActionMessageConsumer 已停止");
        });
    }

    public void stopConsume() {
        if (!running) {
            return;
        }

        log.info("正在停止 ActionMessageConsumer...");
        running = false;
    }

    @PreDestroy
    public void destroy() {
        log.info("销毁 ActionMessageConsumer");
        stopConsume();

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.warn("ExecutorService 未能在5秒内正常关闭，强制关闭");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdownNow();
            }
        }
    }
}