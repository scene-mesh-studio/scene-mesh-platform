package com.scene.mesh.service.impl.ai.rag;

import com.scene.mesh.foundation.impl.helper.StringHelper;
import com.scene.mesh.service.spec.ai.rag.IVectorStoreFactory;
import com.scene.mesh.service.spec.ai.rag.IEmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultEmbeddingService implements IEmbeddingService {

    private IVectorStoreFactory vectorStoreFactory;

    public DefaultEmbeddingService(IVectorStoreFactory vectorStoreFactory) {
        this.vectorStoreFactory = vectorStoreFactory;
    }

    @Override
    public String vectorize(String contentId, Resource documentResource, Map<String, Object> vectorizeOptions) {

        //read document
        TikaDocumentReader reader = new TikaDocumentReader(documentResource);
        List<Document> documents = reader.get();
        documents.forEach(document -> {
            document.getMetadata().put("contentId", contentId);
            document.getMetadata().put("taskId",vectorizeOptions.get("taskId"));
        });

        //chunking
        int chunkSize = vectorizeOptions.containsKey("chunkSize") ? (int) vectorizeOptions.get("chunkSize") : -1;
        int minChunkSizeChars = vectorizeOptions.containsKey("minChunkSizeChars") ? (int) vectorizeOptions.get("minChunkSizeChars") : -1;
        int minChunkLengthToEmbed = vectorizeOptions.containsKey("minChunkLengthToEmbed") ? (int) vectorizeOptions.get("minChunkLengthToEmbed") : -1;
        int maxNumChunks = vectorizeOptions.containsKey("maxNumChunks") ? (int) vectorizeOptions.get("maxNumChunks") : -1;
        boolean keepSeparator = vectorizeOptions.containsKey("keepSeparator") ? Boolean.parseBoolean((String) vectorizeOptions.get("keepSeparator")) : true;

        TokenTextSplitter.Builder builder = TokenTextSplitter.builder();
        if (chunkSize != -1) {
            builder.withChunkSize(chunkSize);
        }
        if (minChunkSizeChars != -1) {
            builder.withMinChunkSizeChars(minChunkSizeChars);
        }
        if (minChunkLengthToEmbed != -1) {
            builder.withMinChunkLengthToEmbed(minChunkLengthToEmbed);
        }
        if (maxNumChunks != -1) {
            builder.withMaxNumChunks(maxNumChunks);
        }
        builder.withKeepSeparator(keepSeparator);

        TokenTextSplitter splitter = builder.build();
        List<Document> splittingDocuments = splitter.apply(documents);

        // write vectorize
        String provideName = (String) vectorizeOptions.get("providerName");
        String modelName = (String) vectorizeOptions.get("modelName");

        VectorStore vectorStore = this.vectorStoreFactory.getVectorStore(provideName, modelName);
        if (vectorStore == null) {
            return StringHelper.format("Cannot found vectorStore by providerName: {0}, modelName: {1}", provideName, modelName);
        }

        try {
            //删除已有 content 的 document
            FilterExpressionBuilder feb = new FilterExpressionBuilder();
            vectorStore.delete(feb.eq("contentId", contentId).build());
            //添加新的
            vectorStore.add(splittingDocuments);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return e.getMessage();
        }

        return "success";
    }

    @Override
    public List<Document> findVectors(String contentId, String providerName, String modelName) {
        ExtendedPgVectorStore vectorStore = (ExtendedPgVectorStore) this.vectorStoreFactory.getVectorStore(providerName, modelName);
        if (vectorStore == null) {
            return new ArrayList<>();
        }
        return vectorStore.findVectors(new FilterExpressionBuilder().eq("contentId",contentId).build());
    }

    @Override
    public Pair<Boolean,String> deleteVectorize(String contentId, String providerName, String modelName) {
        VectorStore vectorStore = this.vectorStoreFactory.getVectorStore(providerName,modelName);
        if (vectorStore == null) {
            String message = StringHelper.format("Cannot found vectorStore by providerName: {0}, modelName: {1}", providerName, modelName);
            log.error(message);
            return Pair.of(false,message);
        }

        try {
            vectorStore.delete(new FilterExpressionBuilder().eq("contentId", contentId).build());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return Pair.of(false,e.getMessage());
        }

        return Pair.of(true,"success");
    }

}
