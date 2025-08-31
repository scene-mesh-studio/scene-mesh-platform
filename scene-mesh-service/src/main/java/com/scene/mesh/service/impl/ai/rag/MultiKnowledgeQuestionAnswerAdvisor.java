/*
 * Copyright 2023-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.scene.mesh.service.impl.ai.rag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class MultiKnowledgeQuestionAnswerAdvisor implements BaseAdvisor {

    public static final String RETRIEVED_DOCUMENTS = "qa_retrieved_documents";
    public static final String FILTER_EXPRESSION = "qa_filter_expression";

    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("""
            {query}

            Context information is below, surrounded by ---------------------

            ---------------------
            {question_answer_context}
            ---------------------

            Given the context and provided history information and not prior knowledge,
            reply to the user comment. If the answer is not in the context, inform
            the user that you can't answer the question.
            """);

    private static final int DEFAULT_ORDER = 0;

    private final List<Pair<Integer, VectorStore>> vectorStores;
    private final PromptTemplate promptTemplate;
    private final SearchRequest searchRequest;
    private final Scheduler scheduler;
    private final int order;

    public MultiKnowledgeQuestionAnswerAdvisor(List<Pair<Integer, VectorStore>> vectorStores) {
        this(vectorStores, SearchRequest.builder().build(), DEFAULT_PROMPT_TEMPLATE, BaseAdvisor.DEFAULT_SCHEDULER,
                DEFAULT_ORDER);
    }

    MultiKnowledgeQuestionAnswerAdvisor(List<Pair<Integer, VectorStore>> vectorStores, SearchRequest searchRequest, @Nullable PromptTemplate promptTemplate,
                                        @Nullable Scheduler scheduler, int order) {
        Assert.notNull(vectorStores, "vectorStores cannot be null");
        Assert.notEmpty(vectorStores, "vectorStores cannot be empty");
        Assert.notNull(searchRequest, "searchRequest cannot be null");

        this.vectorStores = new ArrayList<>(vectorStores);
        this.searchRequest = searchRequest;
        this.promptTemplate = promptTemplate != null ? promptTemplate : DEFAULT_PROMPT_TEMPLATE;
        this.scheduler = scheduler != null ? scheduler : BaseAdvisor.DEFAULT_SCHEDULER;
        this.order = order;
    }

    public static Builder builder(List<Pair<Integer, VectorStore>> vectorStores) {
        return new Builder(vectorStores);
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        var searchRequestToUse = SearchRequest.from(this.searchRequest)
                .query(chatClientRequest.prompt().getUserMessage().getText())
                .filterExpression(doGetFilterExpression(chatClientRequest.context()))
                .build();

        List<Document> allDocuments = new ArrayList<>();

        // 根据优先级搜索并标记文档
        for (Pair<Integer, VectorStore> storePair : vectorStores) {
            int priority = storePair.getLeft();
            VectorStore vectorStore = storePair.getRight();

            try {
                List<Document> documents = vectorStore.similaritySearch(searchRequestToUse);
                if (documents != null) {
                    // 为每个文档添加优先级信息
                    documents.forEach(doc -> {
                        doc.getMetadata().put("priority", priority);
                        doc.getMetadata().put("source_priority", priority);
                    });
                    allDocuments.addAll(documents);
                }
            } catch (Exception e) {
                // Continue with other vector stores
            }
        }

        // 根据优先级和相关性重新排序（优先级1 > 优先级10）
        allDocuments.sort((d1, d2) -> {
            // 首先按优先级排序（数字越小优先级越高）
            int priority1 = (Integer) d1.getMetadata().getOrDefault("priority", Integer.MAX_VALUE);
            int priority2 = (Integer) d2.getMetadata().getOrDefault("priority", Integer.MAX_VALUE);

            if (priority1 != priority2) {
                return Integer.compare(priority1, priority2); // 小数字在前（高优先级）
            }

            // 优先级相同时，按相关性分数排序
            Double score1 = getDocumentScore(d1);
            Double score2 = getDocumentScore(d2);
            return Double.compare(score2, score1); // 高分数在前
        });

        Map<String, Object> context = new HashMap<>(chatClientRequest.context());
        context.put(RETRIEVED_DOCUMENTS, allDocuments);

        String documentContext = allDocuments.isEmpty() ? ""
                : allDocuments.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));

        UserMessage userMessage = chatClientRequest.prompt().getUserMessage();
        String augmentedUserText = this.promptTemplate
                .render(Map.of("query", userMessage.getText(), "question_answer_context", documentContext));

        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .context(context)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        ChatResponse.Builder chatResponseBuilder;
        if (chatClientResponse.chatResponse() == null) {
            chatResponseBuilder = ChatResponse.builder();
        }
        else {
            chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        }
        chatResponseBuilder.metadata(RETRIEVED_DOCUMENTS, chatClientResponse.context().get(RETRIEVED_DOCUMENTS));
        return ChatClientResponse.builder()
                .chatResponse(chatResponseBuilder.build())
                .context(chatClientResponse.context())
                .build();
    }

    @Nullable
    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        if (!context.containsKey(FILTER_EXPRESSION)
                || !StringUtils.hasText(context.get(FILTER_EXPRESSION).toString())) {
            return this.searchRequest.getFilterExpression();
        }
        return new FilterExpressionTextParser().parse(context.get(FILTER_EXPRESSION).toString());
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    private Double getDocumentScore(Document document) {
        try {
            Object score = document.getMetadata().get("score");
            if (score instanceof Number) {
                return ((Number) score).doubleValue();
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        return 0.0;
    }

    public static final class Builder {

        private final List<Pair<Integer, VectorStore>> vectorStores;
        private SearchRequest searchRequest = SearchRequest.builder().build();
        private PromptTemplate promptTemplate;
        private Scheduler scheduler;
        private int order = DEFAULT_ORDER;

        private Builder(List<Pair<Integer, VectorStore>> vectorStores) {
            Assert.notNull(vectorStores, "The vectorStores must not be null!");
            Assert.notEmpty(vectorStores, "The vectorStores must not be empty!");
            this.vectorStores = new ArrayList<>(vectorStores);
        }

        public Builder promptTemplate(PromptTemplate promptTemplate) {
            Assert.notNull(promptTemplate, "promptTemplate cannot be null");
            this.promptTemplate = promptTemplate;
            return this;
        }

        public Builder searchRequest(SearchRequest searchRequest) {
            Assert.notNull(searchRequest, "The searchRequest must not be null!");
            this.searchRequest = searchRequest;
            return this;
        }

        public Builder protectFromBlocking(boolean protectFromBlocking) {
            this.scheduler = protectFromBlocking ? BaseAdvisor.DEFAULT_SCHEDULER : Schedulers.immediate();
            return this;
        }

        public Builder scheduler(Scheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public MultiKnowledgeQuestionAnswerAdvisor build() {
            return new MultiKnowledgeQuestionAnswerAdvisor(this.vectorStores, this.searchRequest, this.promptTemplate, this.scheduler,
                    this.order);
        }
    }
}