package com.scene.mesh.service.impl.ai.chat;

import com.scene.mesh.service.spec.ai.chat.IPromptService;
import com.scene.mesh.service.impl.ai.template.AviatorTemplateRenderer;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.Resource;

import java.util.Map;

public class AviatorPromptService implements IPromptService {
    @Override
    public String assembleUserMessage(Resource templateResource, Map<String, Object> variables) {
        PromptTemplate template = PromptTemplate.builder()
                .resource(templateResource)
                .variables(variables)
                .renderer(new AviatorTemplateRenderer())
                .build();

        Prompt prompt = template.create();
        return prompt.getContents();
    }
}
