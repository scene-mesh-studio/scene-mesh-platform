/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

package org.apache.flink.cep.dynamic.impl.json.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.cep.dynamic.impl.json.deserializer.ConditionSpecStdDeserializer;
import org.apache.flink.cep.dynamic.impl.json.deserializer.GraphSpecStdDeserializer;
import org.apache.flink.cep.dynamic.impl.json.deserializer.NodeSpecStdDeserializer;
import org.apache.flink.cep.dynamic.impl.json.deserializer.TimeStdDeserializer;
import org.apache.flink.cep.dynamic.impl.json.spec.ConditionSpec;
import org.apache.flink.cep.dynamic.impl.json.spec.GraphSpec;
import org.apache.flink.cep.dynamic.impl.json.spec.NodeSpec;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for translating a PatternWrapper to JSON string and vice versa.
 */
@Slf4j
public class CepJsonUtils {

    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .registerModule(
                            new SimpleModule()
                                    .addDeserializer(GraphSpec.class, GraphSpecStdDeserializer.INSTANCE)
                                    .addDeserializer(
                                            ConditionSpec.class,
                                            ConditionSpecStdDeserializer.INSTANCE)
                                    .addDeserializer(Time.class, TimeStdDeserializer.INSTANCE)
                                    .addDeserializer(
                                            NodeSpec.class, NodeSpecStdDeserializer.INSTANCE));

    public static String convertPatternToJSONString(Pattern<?, ?> pattern)
            throws JsonProcessingException {
        GraphSpec graphSpec = GraphSpec.fromPattern(pattern);
        return objectMapper.writeValueAsString(graphSpec);
    }

    public static Pattern<?, ?> convertJSONStringToPattern(String jsonString) throws Exception {
        return convertJSONStringToPattern(
                jsonString, Thread.currentThread().getContextClassLoader(), new Configuration());
    }

    public static Pattern<?, ?> convertJSONStringToPattern(
            String jsonString, ClassLoader userCodeClassLoader, Configuration globalConfiguration) throws Exception {
        if (userCodeClassLoader == null) {
            log.warn(
                    "The given userCodeClassLoader is null. Will try to use ContextClassLoader of current thread.");
            return convertJSONStringToPattern(jsonString);
        }
        GraphSpec graphSpec = objectMapper.readValue(jsonString, GraphSpec.class);
        return graphSpec.toPattern(userCodeClassLoader, globalConfiguration);
    }

    public static GraphSpec convertJSONStringToGraphSpec(String jsonString) throws Exception {
        return objectMapper.readValue(jsonString, GraphSpec.class);
    }

    public static String convertGraphSpecToJSONString(GraphSpec graphSpec) throws Exception {
        return objectMapper.writeValueAsString(graphSpec);
    }
}
