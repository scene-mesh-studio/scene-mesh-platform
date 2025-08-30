package com.scene.mesh.service.spec.ai.config;

import com.scene.mesh.model.llm.LanguageModel;
import com.scene.mesh.model.llm.LanguageModelProvider;

public interface ILLmConfigService {

    LanguageModelProvider getLmpConfig(String providerName);

    LanguageModel getLlmConfig(String providerName, String modelName);
}
