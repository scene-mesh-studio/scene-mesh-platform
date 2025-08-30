package com.scene.mesh.service.spec.ai.advisor;

import org.springframework.ai.chat.client.advisor.api.Advisor;

public interface IAdvisorFactory {

    Advisor getAdvisors(AdvisorBuildRequest request);
}
