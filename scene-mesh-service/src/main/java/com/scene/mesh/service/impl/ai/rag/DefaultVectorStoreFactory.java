package com.scene.mesh.service.impl.ai.rag;

import com.scene.mesh.model.llm.LanguageModel;
import com.scene.mesh.model.llm.LanguageModelProvider;
import com.scene.mesh.service.impl.ai.rag.openai.CompatibleOpenAiEmbeddingModel;
import com.scene.mesh.service.spec.ai.rag.IEmbeddingModel;
import com.scene.mesh.service.spec.ai.config.ILLmConfigService;
import com.scene.mesh.service.spec.ai.rag.IVectorStoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.scene.mesh.service.impl.ai.rag.ExtendedPgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static com.scene.mesh.service.impl.ai.rag.ExtendedPgVectorStore.PgIndexType.HNSW;


@Slf4j
public class DefaultVectorStoreFactory implements IVectorStoreFactory {

    private Map<String, VectorStore> vectorStores;
    private final JdbcTemplate jdbcTemplate;

    private final ILLmConfigService lLmConfigService;


    public DefaultVectorStoreFactory(JdbcTemplate jdbcTemplate, ILLmConfigService lLmConfigService) {
        this.jdbcTemplate = jdbcTemplate;
        this.lLmConfigService = lLmConfigService;
        this.vectorStores = new ConcurrentHashMap<>();
    }

    public VectorStore getVectorStore(String modelProviderName, String embeddingModelName) {
        String key = modelProviderName + ":" + embeddingModelName;
        if (this.vectorStores.get(key) != null) {
            return this.vectorStores.get(key);
        }

        LanguageModelProvider lmp = this.lLmConfigService.getLmpConfig(modelProviderName);
        List<LanguageModel> llms = lmp.getModels();
        if (llms == null || llms.isEmpty()) {
            log.error("the llms of lmp is empty. lmp: {}, llm: {}", modelProviderName, embeddingModelName);
            return null;
        }

        LanguageModel lm = null;
        for (LanguageModel llm : llms) {
            if (embeddingModelName.equals(llm.getName()))
                lm = llm;
        }

        if (lm == null) {
            log.error("can't find the embedding model named:{} in the provider:{}", embeddingModelName, modelProviderName);
            return null;
        }

        IEmbeddingModel embeddingModel = new CompatibleOpenAiEmbeddingModel(lm.getId(),lm.getName(),
                lmp.getName(), lmp.getApiHost(), lm.getModelPath(), lm.getDimensions(), lmp.getApiKey());
        VectorStore vectorStore = this.buildVectorStore(embeddingModel);
        this.vectorStores.put(key, vectorStore);
        return vectorStore;
    }

    private VectorStore buildVectorStore(IEmbeddingModel embeddingModel) {
        String storeSchemeName = "public";
        // 作为表名，向量模型的 name 需要做特殊字符处理
        String storeTableName = generateSafeTableName(embeddingModel.getModelName());
        String fullTableName = storeSchemeName + "." + storeTableName;

        //基于向量模型创建特定向量表
        preCreateVectorTable(embeddingModel, fullTableName);

        return ExtendedPgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(embeddingModel.dimensions())
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName(storeSchemeName)
                .vectorTableName(storeTableName)
                .maxDocumentBatchSize(10000)
                .build();
    }

    private void preCreateVectorTable(IEmbeddingModel embeddingModel, String fullTableName) {
        String sql =
                "CREATE EXTENSION IF NOT EXISTS vector;\n" +
                "CREATE EXTENSION IF NOT EXISTS hstore;\n" +
                "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";\n" +
                "\n" +
                "CREATE TABLE IF NOT EXISTS "+ fullTableName +" (\n" +
                "\tid uuid DEFAULT uuid_generate_v4() PRIMARY KEY,\n" +
                "\tcontent text,\n" +
                "\tmetadata json,\n" +
                "\tembedding vector("+ embeddingModel.dimensions()+")\n" +
                ");\n" +
                "\n" +
                "CREATE INDEX ON "+ fullTableName +" USING HNSW (embedding vector_cosine_ops);";

        log.info("create vector store for {}, sql: {}", fullTableName,sql);
        jdbcTemplate.execute(sql);
    }

    private String generateSafeTableName(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new RuntimeException("modelName is null or empty");
        }

        String safeName = modelName
                // 替换所有非字母数字字符为下划线
                .replaceAll("[^a-zA-Z0-9]", "_")
                // 多个连续下划线合并为一个
                .replaceAll("_+", "_")
                // 移除首尾下划线
                .replaceAll("^_|_$", "")
                // 统一小写
                .toLowerCase();

        // 确保不以数字开头
        if (safeName.matches("^\\d.*")) {
            safeName = "tbl_" + safeName;
        }

        // 确保长度不超过63字节
        if (safeName.length() > 63) {
            safeName = safeName.substring(0, 63);
        }

        // 确保不为空
        if (safeName.isEmpty()) {
            throw new RuntimeException("safeName is empty");
        }

        return safeName;
    }

}
