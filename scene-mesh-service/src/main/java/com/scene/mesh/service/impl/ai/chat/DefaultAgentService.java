package com.scene.mesh.service.impl.ai.chat;

import com.scene.mesh.model.event.Event;
import com.scene.mesh.model.scene.WhenThen;
import com.scene.mesh.service.spec.ai.chat.IAgentService;
import com.scene.mesh.service.spec.ai.chat.IChatClientFactory;
import com.scene.mesh.service.spec.ai.chat.IPromptService;
import com.scene.mesh.service.spec.ai.mcp.IToolsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultAgentService implements IAgentService {

    private final IChatClientFactory chatClientFactory;

    private final IPromptService promptService;

    private final IToolsService toolsService;

    private final Resource templateResource = new ClassPathResource("user_prompt_template.av");

    public DefaultAgentService(IChatClientFactory chatClientFactory,IToolsService toolsService) {
        this.chatClientFactory = chatClientFactory;
        this.toolsService = toolsService;
        this.promptService = new AviatorPromptService();
    }

    @Override
    public boolean callAgent(WhenThen.Then then, List<Event> inputEvents) {
        List<WhenThen.OutputAction> outputActions = then.getOutputActions();
        String[] mcps = then.getMcps();
        String model = then.getModel();
        String modelProvider = then.getModelProvider();
        String scenePrompt = then.getPromptTemplate();
        Double temperature = then.getTemperature();
        Integer topP = then.getTopP();

        // needful action ids
        List<String> actionIds = new ArrayList<>();
        if (outputActions != null) {
            for (WhenThen.OutputAction outputAction : outputActions) {
                actionIds.add(outputAction.getActionId());
            }
        }

        // needful toolCallbacks
        List<ToolCallback> toolCallbacks = this.toolsService.findToolCallbacks(actionIds, List.of(mcps));

        Map<String, Object> variables = Map.of(
                "events", inputEvents,
                "terminalId", inputEvents.get(0).getTerminalId()
        );

        String userMessage = this.promptService.assembleUserMessage(templateResource, variables);
        Prompt prompt = Prompt.builder()
                .messages(new UserMessage(userMessage))
                .build()
                .augmentSystemMessage(scenePrompt);

        ChatClient chatClient = this.chatClientFactory.getChatClient(modelProvider,model);

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .temperature(temperature)
                .topP(Double.valueOf(topP))
                .frequencyPenalty(0.8)
                .parallelToolCalls(true)
                .build();

        ChatResponse response = chatClient
                .prompt(prompt)
                .options(chatOptions)
                .call()
                .chatClientResponse()
                .chatResponse();

        if (response != null) {
            log.info("测试输出 -- {}", response.getResult().getOutput().getText());
        }
        return true;

//        LanguageModel llm = agent.getLanguageModel();
//        String scenePrompt = agent.getScenePrompt();
//        List<String> toolNames = agent.getToolNames();
//
//        //获取可用的 tools
//        List<ToolCallback> toolCallbacks = toolsService.findToolCallbacks(toolNames);
//
//        Map<String, Object> variables = Map.of(
//                "events", eventsInScene,
//                "terminalId", eventsInScene.get(0).getTerminalId()
//        );
//
//        String userMessage = this.promptService.assembleUserMessage(templateResource, variables);
//
//        ChatClient chatClient = this.chatClientFactory.getChatClient(llm);
//
//        ChatOptions chatOptions = this.chatClientFactory.getDefaultChatOptions(llm);
//
//        Prompt prompt = Prompt.builder()
//                .chatOptions(chatOptions)
//                .messages(new UserMessage(userMessage))
//                .build()
//                .augmentSystemMessage(scenePrompt);
//
//                ChatResponse response = chatClient
//                        .prompt(prompt)
//                        .toolCallbacks(toolCallbacks)
//                        .call()
//                        .chatClientResponse()
//                        .chatResponse();
    }
}
