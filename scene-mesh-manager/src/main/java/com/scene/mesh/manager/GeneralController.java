package com.scene.mesh.manager;

import com.scene.mesh.manager.dto.PageResult;
import com.scene.mesh.manager.dto.SubmittedTaskDTO;
import com.scene.mesh.manager.dto.VectorDTO;
import com.scene.mesh.manager.task.IGeneralTask;
import com.scene.mesh.manager.task.TaskType;
import com.scene.mesh.manager.task.VectorizationTask;
import com.scene.mesh.manager.task.processor.ITaskProcessor;
import com.scene.mesh.manager.task.processor.TaskProcessorManager;
import com.scene.mesh.model.terminal.Terminal;
import com.scene.mesh.model.terminal.TerminalStatus;
import com.scene.mesh.service.spec.ai.rag.IEmbeddingService;
import com.scene.mesh.service.spec.terminal.ITerminalService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.document.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sm/v1")
public class GeneralController {

    private final ITerminalService terminalService;

    private final IEmbeddingService embeddingService;

    private final TaskProcessorManager taskProcessorManager;

    public GeneralController(ITerminalService terminalService, IEmbeddingService embeddingService, TaskProcessorManager taskProcessorManager) {
        this.terminalService = terminalService;
        this.embeddingService = embeddingService;
        this.taskProcessorManager = taskProcessorManager;
    }

    @GetMapping("/terminals")
    public ResponseEntity<Page<Terminal>> findTerminals(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String terminalId,
            @RequestParam(required = false) TerminalStatus terminalStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Instant createTimeBegin,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Instant createTimeEnd,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Terminal> terminals = this.terminalService
                .searchTerminals(productId,terminalId,terminalStatus,createTimeBegin,createTimeEnd,page,size);

        return ResponseEntity.ok(terminals);
    }

    @PostMapping("/tasks")
    public ResponseEntity<IGeneralTask> submitVectorizationTask(@RequestBody SubmittedTaskDTO submittedTaskDTO) {
        if (submittedTaskDTO == null || submittedTaskDTO.getTaskType() == null) {
            return ResponseEntity.badRequest().build();
        }

        IGeneralTask task = null;
        if (TaskType.vectorization.equals(submittedTaskDTO.getTaskType())) {
            task = new VectorizationTask();
            task.setPayload(submittedTaskDTO.getTaskData());
        }

        if (task == null) {
            return ResponseEntity.badRequest().build();
        }

        ITaskProcessor processor = this.taskProcessorManager.getTaskProcessor(task.getTaskType());
        if (processor == null) {
            return ResponseEntity.badRequest().build();
        }

        processor.process(task);

        return ResponseEntity.ok(processor.getGeneralTask());
    }

    @GetMapping("/vectors")
    public ResponseEntity<PageResult<VectorDTO>> getVectorsByKnowledgeId(
            @RequestParam String knowledgeBaseId,
            @RequestParam String knowledgeItemId,
            @RequestParam String providerName,
            @RequestParam String modelName,
            @RequestParam int page,
            @RequestParam int size) {
        if (page <= 0 || size <= 0) {
            page = 1;
            size = 10;
        }

        // 获取总数
        long total = embeddingService.findCount(knowledgeBaseId, knowledgeItemId, providerName, modelName);
        if (total <= 0) {
            return ResponseEntity.ok(new PageResult<>(new ArrayList<>(),0,page,size));
        }

        List<Document> documents = this.embeddingService.findVectors(
                knowledgeBaseId, knowledgeItemId, providerName, modelName, page, size);

        List<VectorDTO> vectorDTOS = new ArrayList<>();
        if (documents == null || documents.isEmpty())
            return ResponseEntity.ok(new PageResult<>(new ArrayList<>(),0,page,size));

        documents.forEach(document -> {
            VectorDTO vectorDTO = new VectorDTO();
            vectorDTO.setId(document.getId());
            vectorDTO.setText(document.getText());
            vectorDTOS.add(vectorDTO);
        });

        return ResponseEntity.ok(new PageResult<>(vectorDTOS,total,page,size));
    }

    @DeleteMapping("/vectors")
    public ResponseEntity<Pair<Boolean,String>> deleteVector(@RequestParam String knowledgeBaseId, @RequestParam String knowledgeItemId, @RequestParam String providerName, @RequestParam String modelName) {
        Pair<Boolean,String> result = this.embeddingService.deleteVectorize(knowledgeBaseId, knowledgeItemId, providerName, modelName);
        return ResponseEntity.ok(result);
    }
}
