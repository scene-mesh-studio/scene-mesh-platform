package com.scene.mesh.service.spec.ai.chat;

import com.scene.mesh.model.event.Event;
import com.scene.mesh.model.scene.WhenThen;

import java.util.List;

/**
 * Agent 服务
 */
public interface IAgentService {

    boolean callAgent(WhenThen.Then agent, List<Event> inputEvents);

}
