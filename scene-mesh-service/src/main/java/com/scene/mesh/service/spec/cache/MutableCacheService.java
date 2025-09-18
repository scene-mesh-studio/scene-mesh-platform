package com.scene.mesh.service.spec.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.scene.mesh.foundation.impl.helper.SimpleObjectHelper;
import com.scene.mesh.foundation.spec.api.ApiClient;
import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.foundation.spec.parameter.CalculatorDescriptor;
import com.scene.mesh.foundation.spec.parameter.MetaParameterDescriptor;
import com.scene.mesh.foundation.spec.parameter.data.*;
import com.scene.mesh.model.action.DefaultMetaAction;
import com.scene.mesh.model.action.IMetaAction;
import com.scene.mesh.model.event.DefaultMetaEvent;
import com.scene.mesh.model.event.IMetaEvent;
import com.scene.mesh.model.knowledge.KnowledgeBase;
import com.scene.mesh.model.knowledge.OriginalKnowledgeBase;
import com.scene.mesh.model.llm.LanguageModel;
import com.scene.mesh.model.llm.LanguageModelProvider;
import com.scene.mesh.model.llm.OriginalLanguageModelProvider;
import com.scene.mesh.model.mcp.McpServer;
import com.scene.mesh.model.mcp.OriginalMcpServer;
import com.scene.mesh.model.product.OriginalProduct;
import com.scene.mesh.model.product.Product;
import com.scene.mesh.model.product.ProductSetting;
import com.scene.mesh.model.protocol.ProtocolConfig;
import com.scene.mesh.model.protocol.ProtocolType;
import com.scene.mesh.model.scene.Scene;
import com.scene.mesh.model.scene.WhenThen;
import com.scene.mesh.model.session.TerminalSession;
import com.scene.mesh.service.impl.cache.action.MetaActionCache;
import com.scene.mesh.service.impl.cache.action.MetaActionCacheProvider;
import com.scene.mesh.service.impl.cache.event.MetaEventCache;
import com.scene.mesh.service.impl.cache.event.MetaEventCacheProvider;
import com.scene.mesh.service.impl.cache.knowledge.KnowledgeCache;
import com.scene.mesh.service.impl.cache.knowledge.KnowledgeCacheProvider;
import com.scene.mesh.service.impl.cache.llm.LlmCache;
import com.scene.mesh.service.impl.cache.llm.LlmCacheProvider;
import com.scene.mesh.service.impl.cache.mcp.McpServerCache;
import com.scene.mesh.service.impl.cache.mcp.McpServerCacheProvider;
import com.scene.mesh.service.impl.cache.product.ProductCache;
import com.scene.mesh.service.impl.cache.product.ProductCacheProvider;
import com.scene.mesh.service.impl.cache.scene.SceneCache;
import com.scene.mesh.service.impl.cache.scene.SceneCacheProvider;
import com.scene.mesh.service.impl.cache.terminal.TerminalSessionCache;
import com.scene.mesh.service.impl.cache.terminal.TerminalSessionCacheProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存服务
 */
public class MutableCacheService {

    private final CacheObjectContainer<TerminalSessionCache, TerminalSession> terminalSessionCacheContainer;

    private final CacheObjectContainer<ProductCache, Product> productCacheContainer;

    private final CacheObjectContainer<MetaEventCache, IMetaEvent> metaEventCacheContainer;

    private final CacheObjectContainer<SceneCache, Scene> sceneCacheContainer;

    private final CacheObjectContainer<MetaActionCache, IMetaAction> metaActionCacheContainer;

    private final CacheObjectContainer<LlmCache, LanguageModelProvider> llmCacheContainerProvider;

    private final CacheObjectContainer<McpServerCache, McpServer> mcpServerCacheContainerProvider;

    private final CacheObjectContainer<KnowledgeCache, KnowledgeBase> knowledgeCacheContainerProvider;

    private final ApiClient apiClient;

    public MutableCacheService(ICache cache, ApiClient apiClient) {

        this.apiClient = apiClient;

        terminalSessionCacheContainer =
                new NonExpiringCacheObjectContainer<>(new TerminalSessionCacheProvider(cache), false);

        productCacheContainer =
                new NonExpiringCacheObjectContainer<>(new ProductCacheProvider(cache), true);

        metaEventCacheContainer =
                new NonExpiringCacheObjectContainer<>(new MetaEventCacheProvider(cache), true);

        sceneCacheContainer =
                new NonExpiringCacheObjectContainer<>(new SceneCacheProvider(cache), true);

        metaActionCacheContainer =
                new NonExpiringCacheObjectContainer<>(new MetaActionCacheProvider(cache), true);

        llmCacheContainerProvider =
                new NonExpiringCacheObjectContainer<>(new LlmCacheProvider(cache), true);

        mcpServerCacheContainerProvider =
                new NonExpiringCacheObjectContainer<>(new McpServerCacheProvider(cache), true);

        knowledgeCacheContainerProvider =
                new NonExpiringCacheObjectContainer<>(new KnowledgeCacheProvider(cache), true);
    }

    public TerminalSession getTerminalSessionByTerminalId(String terminalId) {
        return terminalSessionCacheContainer.read().findByTerminalId(terminalId);
    }

    public void setTerminalSession(TerminalSession terminalSession) {
        terminalSessionCacheContainer.read().setTerminalSession(terminalSession);
    }

    public boolean updateTerminalSession(TerminalSession terminalSession) {
        terminalSessionCacheContainer.read().deleteTerminalSession(terminalSession.getTerminalId());
        terminalSessionCacheContainer.read().setTerminalSession(terminalSession);
        return true;
    }

    public boolean refreshAll() {
        // refresh product related
        List<OriginalProduct> originalProducts = this.getAllOriginalProducts();
        List<Product> products = this.extractProducts(originalProducts);
        List<IMetaEvent> metaEvents = this.extractMetaEvents(originalProducts);
        List<Scene> scenes = this.extractScenes(originalProducts);
        List<IMetaAction> metaActions = this.extractMetaActions(originalProducts);
        this.productCacheContainer.refresh(products);
        this.metaEventCacheContainer.refresh(metaEvents);
        this.sceneCacheContainer.refresh(scenes);
        this.metaActionCacheContainer.refresh(metaActions);

        // refresh llm related
        List<OriginalLanguageModelProvider> originalLanguageModelProviders =
                this.getAllOriginalLanguageModelProviders();
        List<LanguageModelProvider> languageModelProviders = this.extractLanguageModelProviders(originalLanguageModelProviders);
        this.llmCacheContainerProvider.refresh(languageModelProviders);

        // refresh mcp servers related
        List<OriginalMcpServer> originalMcpServers =
                this.getAllOriginalMcpServers();
        List<McpServer> mcpServers = this.extractMcpServers(originalMcpServers);
        this.mcpServerCacheContainerProvider.refresh(mcpServers);

        // refresh knowledgeBase
        List<OriginalKnowledgeBase> originalKnowledgeBases = this.getAllOriginalKnowledgeBases();
        List<KnowledgeBase> knowledgeBases = this.extractKnowledgeBases(originalKnowledgeBases);
        this.knowledgeCacheContainerProvider.refresh(knowledgeBases);

        return true;
    }

    private List<KnowledgeBase> extractKnowledgeBases(List<OriginalKnowledgeBase> originalKnowledgeBases) {
        List<KnowledgeBase> knowledgeBases = new ArrayList<>();

        for (OriginalKnowledgeBase originalKnowledgeBase : originalKnowledgeBases) {
            if (originalKnowledgeBase.getValues().getProvider() == null || originalKnowledgeBase.getValues().getEmbeddingsModel() == null) {
                continue;
            }
            KnowledgeBase knowledgeBase = new KnowledgeBase();
            knowledgeBase.setId(originalKnowledgeBase.getId());
            knowledgeBase.setName(originalKnowledgeBase.getValues().getName());
            knowledgeBase.setProviderName(originalKnowledgeBase.getValues().getProvider().getValues().getName());
            knowledgeBase.setModelName(originalKnowledgeBase.getValues().getEmbeddingsModel().getValues().getName());
            knowledgeBase.setDescription(originalKnowledgeBase.getValues().getDescription());
            knowledgeBase.setEnabled(originalKnowledgeBase.getValues().getEnabled());

            OriginalKnowledgeBase.KnowledgeItem[] knowledgeItems = originalKnowledgeBase.getValues().getKnowledgeItems();
            if (knowledgeItems != null) {
                String[] items = new String[knowledgeItems.length];
                for (int i = 0; i < knowledgeItems.length; i++) {
                    items[i] = knowledgeItems[i].getId();
                }
                knowledgeBase.setKnowledgeItemIds(items);
            }
            knowledgeBases.add(knowledgeBase);
        }

        return knowledgeBases;
    }

    private List<OriginalKnowledgeBase> getAllOriginalKnowledgeBases() {
        Map<String, String> params = new HashMap<>();
        params.put("withReference", "true");
        Object responseObj = this.apiClient.get(ApiClient.ServiceType.knowledgebase.name(), "", Object.class, params);
        if (responseObj == null) {
            throw new RuntimeException("invoke knowledgeBase list api,but not found any object.");
        }
        Map<String, Object> responseObjMap = SimpleObjectHelper.obj2Map(responseObj);
        Object result = responseObjMap.get("result");
        Map<String, Object> resultMap = SimpleObjectHelper.obj2Map(result);
        List<Object> baseObjects = (List<Object>) resultMap.get("data");

        List<OriginalKnowledgeBase> knowledgeBases = new ArrayList<>();

        for (Object baseObject : baseObjects) {
            OriginalKnowledgeBase originalKnowledgeBase = SimpleObjectHelper.obj2SpecificObj(baseObject, new TypeReference<>() {
            });
            knowledgeBases.add(originalKnowledgeBase);
        }
        return knowledgeBases;
    }

    private List<OriginalMcpServer> getAllOriginalMcpServers() {
        Map<String, String> params = new HashMap<>();
        params.put("withReference", "true");
        Object responseObj = this.apiClient.get(ApiClient.ServiceType.mcpserver.name(), "", Object.class, params);
        if (responseObj == null) {
            throw new RuntimeException("invoke mcp-server list api,but not found any object.");
        }
        Map<String, Object> responseObjMap = SimpleObjectHelper.obj2Map(responseObj);
        Object result = responseObjMap.get("result");
        Map<String, Object> resultMap = SimpleObjectHelper.obj2Map(result);
        List<Object> mcpObjects = (List<Object>) resultMap.get("data");

        List<OriginalMcpServer> mcpServers = new ArrayList<>();

        for (Object mcpObj : mcpObjects) {
            OriginalMcpServer originalMcpServer = SimpleObjectHelper.obj2SpecificObj(mcpObj, new TypeReference<>() {
            });
            mcpServers.add(originalMcpServer);
        }
        return mcpServers;
    }

    private List<McpServer> extractMcpServers(List<OriginalMcpServer> originalMcpServers) {

        List<McpServer> mcpServers = new ArrayList<>();

        for (OriginalMcpServer originalMcpServer : originalMcpServers) {

            McpServer mcpServer = new McpServer();

            String mcpId = originalMcpServer.getId();
            String mcpName = originalMcpServer.getValues().getName();
            String mcpDesc = originalMcpServer.getValues().getDescription();
            String mcpHeader = originalMcpServer.getValues().getHeader();
            String baseUrl = originalMcpServer.getValues().getBaseUrl();
            String endpoint = originalMcpServer.getValues().getEndpoint();
            String type = originalMcpServer.getValues().getType();
            Boolean enable = originalMcpServer.getValues().isEnable();
            int timeout = originalMcpServer.getValues().getTimeout();

            mcpServer.setId(mcpId);
            mcpServer.setName(mcpName);
            mcpServer.setDescription(mcpDesc);
            mcpServer.setHeader(mcpHeader);
            mcpServer.setBaseUrl(baseUrl);
            mcpServer.setEndpoint(endpoint);
            mcpServer.setType(type);
            mcpServer.setEnabled(enable);
            mcpServer.setTimeout(timeout);

            mcpServers.add(mcpServer);
        }
        return mcpServers;
    }

    private List<OriginalLanguageModelProvider> getAllOriginalLanguageModelProviders() {
        Map<String, String> params = new HashMap<>();
        params.put("withReference", "true");
        Object responseObj = this.apiClient.get(ApiClient.ServiceType.llm.name(), "", Object.class, params);
        if (responseObj == null) {
            throw new RuntimeException("invoke llm list api,but not found any object.");
        }
        Map<String, Object> responseObjMap = SimpleObjectHelper.obj2Map(responseObj);
        Object result = responseObjMap.get("result");
        Map<String, Object> resultMap = SimpleObjectHelper.obj2Map(result);
        List<Object> llmObjs = (List<Object>) resultMap.get("data");

        List<OriginalLanguageModelProvider> llms = new ArrayList<>();
        for (Object llmObj : llmObjs) {
            OriginalLanguageModelProvider originalLlm = SimpleObjectHelper.obj2SpecificObj(llmObj, new TypeReference<>() {
            });
            llms.add(originalLlm);
        }
        return llms;
    }

    private List<LanguageModelProvider> extractLanguageModelProviders(List<OriginalLanguageModelProvider> originalLanguageModelProviders) {

        List<LanguageModelProvider> lmps = new ArrayList<>();

        for (OriginalLanguageModelProvider originalLmp : originalLanguageModelProviders) {
            String providerId = originalLmp.getId();
            String providerName = originalLmp.getValues().getName();
            String providerDes = originalLmp.getValues().getDescription();
            String apiHost = originalLmp.getValues().getApiHost();
            String apiKey = originalLmp.getValues().getApiKey();
            String apiMode = originalLmp.getValues().getApiMode();
            boolean isApiCompatibility = originalLmp.getValues().isApiCompatibility();

            LanguageModelProvider lmp = new LanguageModelProvider();
            lmp.setId(providerId);
            lmp.setName(providerName);
            lmp.setDescription(providerDes);
            lmp.setApiHost(apiHost);
            lmp.setApiKey(apiKey);
            lmp.setApiCompatibility(isApiCompatibility);
            lmp.setApiMode(apiMode);

            List<OriginalLanguageModelProvider.LanguageModel> originalLlms = originalLmp.getValues().getModels();
            if (originalLlms != null) {
                List<LanguageModel> llms = new ArrayList<>();
                for (OriginalLanguageModelProvider.LanguageModel originalLm : originalLlms) {
                    String modelId = originalLm.getId();
                    String modelName = originalLm.getValues().getName();
                    String modelDes = originalLm.getValues().getDescription();
                    String modelPath = originalLm.getValues().getModelPath();
                    int dimensions = originalLm.getValues().getDimensions();
                    List<String> features = originalLm.getValues().getFeature();

                    LanguageModel llm = new LanguageModel();
                    llm.setId(modelId);
                    llm.setName(modelName);
                    llm.setDescription(modelDes);
                    llm.setFeature(features);
                    llm.setModelPath(modelPath);
                    llm.setDimensions(dimensions);


                    llms.add(llm);
                }
                lmp.setModels(llms);
            }
            lmps.add(lmp);
        }
        return lmps;
    }

    private List<OriginalProduct> getAllOriginalProducts() {
        Map<String, String> params = new HashMap<>();
//        params.put("withReference", "true");
        Object responseObj = this.apiClient.get(ApiClient.ServiceType.product.name(), "", Object.class, params);
        if (responseObj == null) {
            throw new RuntimeException("invoke product list api,but not found any object.");
        }
        Map<String, Object> responseObjMap = SimpleObjectHelper.obj2Map(responseObj);
        Object result = responseObjMap.get("result");
        Map<String, Object> resultMap = SimpleObjectHelper.obj2Map(result);
        List<Object> productObjs = (List<Object>) resultMap.get("data");

        List<OriginalProduct> products = new ArrayList<>();
        for (Object productObj : productObjs) {
            OriginalProduct originalProduct = SimpleObjectHelper.obj2SpecificObj(productObj, new TypeReference<>() {
            });
            products.add(originalProduct);
        }
        return products;
    }

    private List<IMetaAction> extractMetaActions(List<OriginalProduct> originalProducts) {
        if (originalProducts == null || originalProducts.isEmpty()) return null;
        List<IMetaAction> metaActions = new ArrayList<>();
        for (OriginalProduct originalProduct : originalProducts) {
            List<OriginalProduct.Action> actions = originalProduct.getValues().getActions();
            if (actions == null || actions.isEmpty()) continue;

            actions.forEach(action -> {
                String aId = action.getId();
                String aName = action.getValues().getName();
                String aTitle = action.getValues().getTitle();
                String aDescription = action.getValues().getDescription();
                IMetaAction metaAction = new DefaultMetaAction(aId, aName, aDescription, originalProduct.getId());

                List<OriginalProduct.ActionField> actionFields = action.getValues().getFields();
                if (actionFields != null && !actionFields.isEmpty()) {
                    action.getValues().getFields().forEach(f -> {
                        String fName = f.getValues().getFieldName();
                        String fTitle = f.getValues().getFieldTitle();
                        String fDes = f.getValues().getFieldDescription();
                        String fType = f.getValues().getFieldType();
                        String fCategory = f.getValues().getFieldCategory();
                        String fCalculateType = f.getValues().getFieldCalculateType();
                        String fCalculateSource = f.getValues().getFieldCalculateSource();
                        IParameterDataType dataType = confirmDataType(fType);
                        if (dataType == null) {
                            throw new RuntimeException("cannot find dataType pass field type:" + fType);
                        }
                        MetaParameterDescriptor metaParameterDescriptor = new MetaParameterDescriptor(
                                fName, fTitle, fDes, dataType, false);
                        if ("compute".equals(fCategory)) {
                            metaParameterDescriptor.setCalculatorDescriptor(new CalculatorDescriptor(fCalculateType, fCalculateSource));
                        }

                        metaAction.addParameterDescriptor(metaParameterDescriptor);
                    });
                }
                metaActions.add(metaAction);
            });
        }
        return metaActions;
    }

    public List<Scene> extractScenes(List<OriginalProduct> originalProducts) {
        List<Scene> sceneList = new ArrayList<>();
        for (OriginalProduct originalProduct : originalProducts) {
            List<OriginalProduct.Scene> originalScenes = originalProduct.getValues().getRootScene();
            if (originalScenes == null || originalScenes.isEmpty()) {
                continue;
            }
            OriginalProduct.Scene originalScene = originalScenes.get(0);
            transformOriginalScene(sceneList, originalScene, originalProduct.getId());
        }
        return sceneList;
    }

    private void transformOriginalScene(List<Scene> scenes, OriginalProduct.Scene originalScene, String productId) {
        if (originalScene == null) {
            return;
        }
        String sceneId = originalScene.getId();
        String sceneName = originalScene.getValues().getName();
        String sceneDesc = originalScene.getValues().getDescription();
        Boolean enable = originalScene.getValues().getEnable();
        String prompt = originalScene.getValues().getPrompt();
        String flowDataPublishTime = originalScene.getValues().getFlowDataPublishTime();
        List<WhenThen> whenThens = originalScene.getValues().getFlowData();

        Scene scene = new Scene();
        scene.setId(sceneId);
        scene.setProductId(productId);
        scene.setName(sceneName);
        scene.setDescription(sceneDesc);
        scene.setEnable(enable);
        scene.setWhenThenList(whenThens);
        scene.setFlowDataPublishTime(flowDataPublishTime);
        scene.setPrompt(prompt);

        List<OriginalProduct.Scene> childrens = originalScene.getValues().getChildren();
        if (childrens != null && !childrens.isEmpty()) {
            for (OriginalProduct.Scene childScene : childrens) {
                transformOriginalScene(scenes, childScene, productId);
            }
        }

        scenes.add(scene);
    }

    public List<IMetaEvent> extractMetaEvents(List<OriginalProduct> originalProducts) {
        List<IMetaEvent> metaEvents = new ArrayList<>();
        for (OriginalProduct originalProduct : originalProducts) {
            List<OriginalProduct.Event> events = originalProduct.getValues().getEvents();
            if (events == null || events.isEmpty()) {
                continue;
            }
            events.forEach(e -> {
                String productId = originalProduct.getId();
                String metaEventId = e.getValues().getName();
                String metaEventDes = e.getValues().getDescription();
                String metaEventName = e.getValues().getTitle();
                IMetaEvent metaEvent = new DefaultMetaEvent(metaEventId, metaEventName, metaEventDes, productId);

                // fields
                List<OriginalProduct.EventField> fields = e.getValues().getFields();
                fields.forEach(f -> {
                    String fName = f.getValues().getFieldName();
                    String fTitle = f.getValues().getFieldTitle();
                    String fDes = f.getValues().getFieldDescription();
                    String fType = f.getValues().getFieldType();
                    Boolean fAsInput = f.getValues().getFieldAsInput();
                    String fCategory = f.getValues().getFieldCategory();
                    String fCalculateType = f.getValues().getFieldCalculateType();
                    String fCalculateSource = f.getValues().getFieldCalculateSource();
                    IParameterDataType dataType = confirmDataType(fType);
                    if (dataType == null) {
                        throw new RuntimeException("cannot find dataType pass field type:" + fType);
                    }
                    MetaParameterDescriptor metaParameterDescriptor = new MetaParameterDescriptor(
                            fName, fTitle, fDes, dataType, false);
                    if ("compute".equals(fCategory)){
                        metaParameterDescriptor.setCalculatorDescriptor(new CalculatorDescriptor(fCalculateType,fCalculateSource));
                    }
                    metaParameterDescriptor.setAsInput(fAsInput);

                    metaEvent.addParameterDescriptor(metaParameterDescriptor);
                });

                metaEvents.add(metaEvent);
            });
        }

        return metaEvents;
    }

    private IParameterDataType confirmDataType(String fType) {
        switch (fType) {
            case "string":
                return new StringParameterDataType();
            case "boolean":
                return new BooleParameterDataType();
            case "number":
                return new DoubleParameterDataType();
            case "datetime":
                return new TimeParameterDataType();
            case "binary":
                return new BinaryParameterDateType();
            case "json":
                return new JsonParameterDateType();
            case "array":
                return new ArrayParameterDateType();
        }
        return null;
    }

    private List<Product> extractProducts(List<OriginalProduct> originalProducts) {
        List<Product> products = new ArrayList<>();

        for (OriginalProduct originalProduct : originalProducts) {
            Product product = new Product();
            product.setId(originalProduct.getId());
            product.setName(originalProduct.getValues().getName());
            product.setDescription(originalProduct.getValues().getDescription());
            product.setCategory(originalProduct.getValues().getCategory());

            // product image
            OriginalProduct.Image originalImage = originalProduct.getValues().getImage();
            Product.ProductImage image = new Product.ProductImage();
            image.setFileName(originalImage.getFileName());
            image.setFileType(originalImage.getFileType());
            image.setFileSize(originalImage.getFileSize());
            image.setFilePath(originalImage.getFilePath());
            product.setImage(image);

            // product setting
            List<OriginalProduct.Setting> originalSettingsList = originalProduct.getValues().getSettings();
            if (originalSettingsList != null && originalSettingsList.size() == 1) {
                OriginalProduct.SettingValues oriSettingVals = originalSettingsList.get(0).getValues();
                Boolean mqttEnabled = oriSettingVals.getMqttEnabled();
                Boolean wsEnabled = oriSettingVals.getWebSocketEnabled();
                String secretKey = oriSettingVals.getSecret();
                ProtocolConfig protocolConfig = new ProtocolConfig();
                if (mqttEnabled) {
                    protocolConfig.add(ProtocolType.MQTT);
                }
                if (wsEnabled) {
                    protocolConfig.add(ProtocolType.WEBSOCKET);
                }
                ProductSetting settings = new ProductSetting();
                settings.setProtocolConfig(protocolConfig);
                settings.setSecretKey(new String[]{secretKey});
                //TODO 修改为真实的产品配置
                settings.setSttProcessor("VOSK");
                settings.setTtsProcessor("EDGE");
                product.setSettings(settings);
            }

            products.add(product);
        }

        return products;
    }

    public List<IMetaEvent> getAllMetaEvent() {
        return metaEventCacheContainer.read().getMetaEvents();
    }

    public IMetaEvent getIMetaEvent(String metaEventId) {
        return this.metaEventCacheContainer.read().getMetaEvent(metaEventId);
    }

    public Product getProductById(String productId) {
        return this.productCacheContainer.read().getProduct(productId);
    }

    public List<IMetaAction> getAllMetaAction() {
        return this.metaActionCacheContainer.read().getMetaActions();
    }

    public IMetaAction getMetaActionById(String metaActionId) {
        return this.metaActionCacheContainer.read().getMetaAction(metaActionId);
    }

    public Scene getSceneById(String sceneId) {
        return this.sceneCacheContainer.read().getScene(sceneId);
    }

    public List<Scene> getAllScenes() {
        return this.sceneCacheContainer.read().getScenes();
    }

    public List<LanguageModelProvider> getAllLmp() {
        return this.llmCacheContainerProvider.read().getAllLanguageModelProviders();
    }

    public List<McpServer> getAllMcpServers() {
        return this.mcpServerCacheContainerProvider.read().getAllMcpServers();
    }

    public KnowledgeBase getKnowledgeById(String kbId) {
        return this.knowledgeCacheContainerProvider.read().getKnowledge(kbId);
    }

    public List<Product> getAllProducts() {
        return this.productCacheContainer.read().getAllProducts();
    }
}
