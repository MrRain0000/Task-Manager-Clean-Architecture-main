package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.DTOUsecase.request.subtask.CreateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.request.subtask.ReorderSubTasksRequest;
import com.example.task_management.application.DTOUsecase.request.subtask.UpdateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.response.subtask.SubTaskResult;
import com.example.task_management.application.usecases.subtask.CreateSubTaskUseCase;
import com.example.task_management.application.usecases.subtask.DeleteSubTaskUseCase;
import com.example.task_management.application.usecases.subtask.GetSubTasksUseCase;
import com.example.task_management.application.usecases.subtask.ReorderSubTasksUseCase;
import com.example.task_management.application.usecases.subtask.UpdateSubTaskUseCase;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Sub-task Operations
 */
@RestController
public class SubTaskController {

    private static final Logger log = LoggerFactory.getLogger(SubTaskController.class);

    private final CreateSubTaskUseCase createSubTaskUseCase;
    private final UpdateSubTaskUseCase updateSubTaskUseCase;
    private final DeleteSubTaskUseCase deleteSubTaskUseCase;
    private final GetSubTasksUseCase getSubTasksUseCase;
    private final ReorderSubTasksUseCase reorderSubTasksUseCase;

    public SubTaskController(
            CreateSubTaskUseCase createSubTaskUseCase,
            UpdateSubTaskUseCase updateSubTaskUseCase,
            DeleteSubTaskUseCase deleteSubTaskUseCase,
            GetSubTasksUseCase getSubTasksUseCase,
            ReorderSubTasksUseCase reorderSubTasksUseCase) {
        this.createSubTaskUseCase = createSubTaskUseCase;
        this.updateSubTaskUseCase = updateSubTaskUseCase;
        this.deleteSubTaskUseCase = deleteSubTaskUseCase;
        this.getSubTasksUseCase = getSubTasksUseCase;
        this.reorderSubTasksUseCase = reorderSubTasksUseCase;
    }

    /**
     * 12.1 Create Sub-task
     * POST /api/tasks/{taskId}/subtasks
     */
    @PostMapping("/api/tasks/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<SubTaskResult>> createSubTask(
            @PathVariable Long taskId,
            @RequestBody CreateSubTaskRequest request,
            Authentication authentication) {

        log.info("[API] Create sub-task - taskId={}, user={}", taskId, authentication.getName());

        SubTaskResult result = createSubTaskUseCase.createSubTask(taskId, request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tạo sub-task thành công", result));
    }

    /**
     * 12.2 Update Sub-task
     * PUT /api/subtasks/{subtaskId}
     */
    @PutMapping("/api/subtasks/{subtaskId}")
    public ResponseEntity<ApiResponse<SubTaskResult>> updateSubTask(
            @PathVariable Long subtaskId,
            @RequestBody UpdateSubTaskRequest request,
            Authentication authentication) {

        log.info("[API] Update sub-task - subtaskId={}, user={}", subtaskId, authentication.getName());

        SubTaskResult result = updateSubTaskUseCase.updateSubTask(subtaskId, request, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cập nhật sub-task thành công", result));
    }

    /**
     * 12.3 Delete Sub-task
     * DELETE /api/subtasks/{subtaskId}
     */
    @DeleteMapping("/api/subtasks/{subtaskId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubTask(
            @PathVariable Long subtaskId,
            Authentication authentication) {

        log.info("[API] Delete sub-task - subtaskId={}, user={}", subtaskId, authentication.getName());

        deleteSubTaskUseCase.deleteSubTask(subtaskId, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Xóa sub-task thành công", null));
    }

    /**
     * 12.4 Get Sub-tasks of Task
     * GET /api/tasks/{taskId}/subtasks
     */
    @GetMapping("/api/tasks/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<List<SubTaskResult>>> getSubTasks(
            @PathVariable Long taskId,
            @RequestParam(required = false) TaskStatus status,
            Authentication authentication) {

        log.info("[API] Get sub-tasks - taskId={}, status={}, user={}", taskId, status, authentication.getName());

        List<SubTaskResult> results;
        if (status != null) {
            results = getSubTasksUseCase.getSubTasksByTaskIdAndStatus(taskId, status, authentication.getName());
        } else {
            results = getSubTasksUseCase.getSubTasksByTaskId(taskId, authentication.getName());
        }

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách sub-tasks thành công", results));
    }

    /**
     * 12.5 Reorder Sub-tasks
     * PUT /api/tasks/{taskId}/subtasks/reorder
     */
    @PutMapping("/api/tasks/{taskId}/subtasks/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderSubTasks(
            @PathVariable Long taskId,
            @RequestBody ReorderSubTasksRequest request,
            Authentication authentication) {

        log.info("[API] Reorder sub-tasks - taskId={}, user={}", taskId, authentication.getName());

        reorderSubTasksUseCase.reorderSubTasks(taskId, request.getSubtaskIds(), authentication.getName());

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Sắp xếp lại sub-tasks thành công", null));
    }
}
