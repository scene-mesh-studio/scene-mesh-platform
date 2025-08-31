package com.scene.mesh.service.spec.ai.advisor;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.chat.client.advisor.api.Advisor;

import java.util.List;
import java.util.Map;

public interface IAdvisorFactory {

    List<Advisor> getMutableAdvisors(Map<String, Object> advisorsInput);

}
