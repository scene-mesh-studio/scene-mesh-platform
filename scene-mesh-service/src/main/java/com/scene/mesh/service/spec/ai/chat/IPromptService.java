package com.scene.mesh.service.spec.ai.chat;

import org.springframework.core.io.Resource;

import java.util.Map;

public interface IPromptService {

    String assemblePrompt(Resource templateResource, Map<String, Object> variables);

}
