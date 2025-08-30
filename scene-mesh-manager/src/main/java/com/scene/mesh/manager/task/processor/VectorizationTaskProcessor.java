package com.scene.mesh.manager.task.processor;

import com.scene.mesh.foundation.spec.api.ApiClient;
import com.scene.mesh.manager.task.IGeneralTask;
import com.scene.mesh.manager.task.TaskStatus;
import com.scene.mesh.manager.task.TaskType;
import com.scene.mesh.manager.task.VectorizationTask;
import com.scene.mesh.service.spec.ai.rag.IEmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
public class VectorizationTaskProcessor extends BaseTaskProcessor {

    private final IEmbeddingService embeddingService;

    private final ApiClient apiClient;

    public VectorizationTaskProcessor(IEmbeddingService embeddingService, ApiClient apiClient) {
        super(TaskType.vectorization);
        this.embeddingService = embeddingService;
        this.apiClient = apiClient;
    }

    @Override
    protected void processTask(IGeneralTask task) {
        VectorizationTask vectorizationTask = (VectorizationTask) task;

        String contentId = vectorizationTask.getContentId();
        String contentUrl = vectorizationTask.getContentUrl();
        Map<String, Object> vectorizeOptions = vectorizationTask.getOptions();

        if (contentId == null || contentId.isEmpty() || contentUrl == null
                || contentUrl.isEmpty() || vectorizeOptions == null
        ) {
            vectorizationTask.setStatusMessage("Cannot found required parameters with vectorization task.");
            return;
        }

        String providerName = (String) vectorizeOptions.get("providerName");
        String modelName = (String) vectorizeOptions.get("modelName");
        if (providerName == null || modelName == null) {
            vectorizationTask.setStatusMessage("Cannot found required parameters with vectorization task. providerName or modelName.");
            return;
        }

        //激活task
        vectorizationTask.setTaskStatus(TaskStatus.activated);

        Resource documentResource = UrlResource.from(contentUrl);

        try {
            String result = this.embeddingService.vectorize(contentId, documentResource, vectorizeOptions);
            if (result != null) {
                if ("success".equals(result)) {
                    vectorizationTask.setTaskStatus(TaskStatus.successful);
                } else {
                    vectorizationTask.setTaskStatus(TaskStatus.unsuccessful);
                }
                vectorizationTask.setStatusMessage(result);
                vectorizationTask.setCompleteTime(Instant.now());
            }
        }catch (Exception e){
            vectorizationTask.setTaskStatus(TaskStatus.unsuccessful);
            vectorizationTask.setStatusMessage(e.getMessage());
        }

//        future.whenCompleteAsync((result, throwable) -> {
//            if (throwable != null) {
//                vectorizationTask.setTaskStatus(TaskStatus.unsuccessful);
//                vectorizationTask.setStatusMessage(throwable.getMessage());
//                this.doCallback(taskCallbackUrl,vectorizationTask);
//                return;
//            }
//            if (result != null) {
//                if ("success".equals(result)) {
//                    vectorizationTask.setTaskStatus(TaskStatus.successful);
//                } else {
//                    vectorizationTask.setTaskStatus(TaskStatus.unsuccessful);
//                }
//                vectorizationTask.setStatusMessage(result);
//            }
//            vectorizationTask.setCompleteTime(Instant.now());
//
//            this.doCallback(taskCallbackUrl, vectorizationTask);
//        });
    }

//    private void doCallback(String taskCallbackUrl, VectorizationTask vectorizationTask) {
//        // 异步callback
//        try {
//            this.apiClient.customPost(taskCallbackUrl, vectorizationTask, String.class);
//        } catch (Exception e) {
//            log.error("vectorization task callback failed - {}", e.getMessage(), e);
//            throw e;
//        }
//    }
}
