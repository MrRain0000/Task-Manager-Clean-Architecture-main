package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.DTOUsecase.request.subtask.CreateSubTaskRequest;
import com.example.task_management.application.DTOUsecase.request.subtask.UpdateSubTaskRequest;
import com.example.task_management.application.usecases.subtask.CreateSubTaskUseCase;
import com.example.task_management.application.usecases.subtask.DeleteSubTaskUseCase;
import com.example.task_management.application.usecases.subtask.GetSubTasksUseCase;
import com.example.task_management.application.usecases.subtask.UpdateSubTaskUseCase;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.subtask.SubTaskResponse;
import com.example.task_management.interfaces.mappers.SubTaskMapper;
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
    private final SubTaskMapper subTaskMapper;

    public SubTaskController(
            CreateSubTaskUseCase createSubTaskUseCase,
            UpdateSubTaskUseCase updateSubTaskUseCase,
            DeleteSubTaskUseCase deleteSubTaskUseCase,
            GetSubTasksUseCase getSubTasksUseCase,
            SubTaskMapper subTaskMapper) {
        this.createSubTaskUseCase = createSubTaskUseCase;
        this.updateSubTaskUseCase = updateSubTaskUseCase;
        this.deleteSubTaskUseCase = deleteSubTaskUseCase;
        this.getSubTasksUseCase = getSubTasksUseCase;
        this.subTaskMapper = subTaskMapper;
    }

    /**
     * 12.1 Create Sub-task
     * POST /api/tasks/{taskId}/subtasks
     */
    @PostMapping("/api/tasks/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<SubTaskResponse>> createSubTask(
            @PathVariable Long taskId,
            @RequestBody CreateSubTaskRequest request,
            Authentication authentication) {

        log.info("[API] Create sub-task - taskId={}, user={}", taskId, authentication.getName());

        var result = createSubTaskUseCase.createSubTask(taskId, request, authentication.getName());
        var response = subTaskMapper.toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tạo sub-task thành công", response));
    }

    /**
     * 12.2 Update Sub-task
     * PUT /api/subtasks/{subtaskId}
     */
    @PutMapping("/api/subtasks/{subtaskId}")
    public ResponseEntity<ApiResponse<SubTaskResponse>> updateSubTask(
            @PathVariable Long subtaskId,
            @RequestBody UpdateSubTaskRequest request,
            Authentication authentication) {

        log.info("[API] Update sub-task - subtaskId={}, user={}", subtaskId, authentication.getName());

        var result = updateSubTaskUseCase.updateSubTask(subtaskId, request, authentication.getName());
        var response = subTaskMapper.toResponse(result);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cập nhật sub-task thành công", response));
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
    public ResponseEntity<ApiResponse<List<SubTaskResponse>>> getSubTasks(
            @PathVariable Long taskId,
            @RequestParam(required = false) TaskStatus status,
            Authentication authentication) {

        log.info("[API] Get sub-tasks - taskId={}, status={}, user={}", taskId, status, authentication.getName());

        var results = status != null
                ? getSubTasksUseCase.getSubTasksByTaskIdAndStatus(taskId, status, authentication.getName())
                : getSubTasksUseCase.getSubTasksByTaskId(taskId, authentication.getName());
        var responses = subTaskMapper.toResponseList(results);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách sub-tasks thành công", responses));
    }

}
