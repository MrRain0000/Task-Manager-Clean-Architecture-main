package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.dto.request.task.AssignTaskRequest;
import com.example.task_management.application.dto.request.task.CreateTaskRequest;
import com.example.task_management.application.dto.request.task.MoveTaskRequest;
import com.example.task_management.application.dto.response.ApiResponse;
import com.example.task_management.application.dto.response.task.TaskResponse;
import com.example.task_management.application.usecases.task.AssignTaskUseCase;
import com.example.task_management.application.usecases.task.CreateTaskUseCase;
import com.example.task_management.application.usecases.task.GetTaskUseCase;
import com.example.task_management.application.usecases.task.MoveTaskUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final CreateTaskUseCase createTaskUseCase;
    private final GetTaskUseCase getTaskUseCase;
    private final MoveTaskUseCase moveTaskUseCase;
    private final AssignTaskUseCase assignTaskUseCase;

    public TaskController(CreateTaskUseCase createTaskUseCase, GetTaskUseCase getTaskUseCase, 
                          MoveTaskUseCase moveTaskUseCase, AssignTaskUseCase assignTaskUseCase) {
        this.createTaskUseCase = createTaskUseCase;
        this.getTaskUseCase = getTaskUseCase;
        this.moveTaskUseCase = moveTaskUseCase;
        this.assignTaskUseCase = assignTaskUseCase;
    }

    // POST /api/projects/{projectId}/tasks
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {

        TaskResponse taskResponse = createTaskUseCase.createTask(projectId, request, authentication.getName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tạo task thành công", taskResponse));
    }

    // GET /api/projects/{projectId}/tasks?status=TODO
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) String status,
            Authentication authentication) {

        List<TaskResponse> tasks = getTaskUseCase.getTasks(projectId, status, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách task thành công", tasks));
    }

    // POST /api/projects/{projectId}/tasks/{taskId}/move
    @PostMapping("/{taskId}/move")
    public ResponseEntity<ApiResponse<TaskResponse>> moveTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody MoveTaskRequest request,
            Authentication authentication) {

        TaskResponse taskResponse = moveTaskUseCase.moveTask(projectId, taskId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Di chuyển task thành công", taskResponse));
    }

    // POST /api/projects/{projectId}/tasks/{taskId}/assign
    @PostMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {

        TaskResponse taskResponse = assignTaskUseCase.assignTask(projectId, taskId, request, authentication.getName());
        String message = request.getAssigneeId() == null 
            ? "Hủy giao task thành công" 
            : "Giao task thành công";
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, taskResponse));
    }
}
