package com.example.task_management.interfaces.controllers;

import com.example.task_management.interfaces.dto.request.task.AssignTaskRequest;
import com.example.task_management.interfaces.dto.request.task.CreateTaskRequest;
import com.example.task_management.interfaces.dto.request.task.MoveTaskRequest;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.task.TaskDetailResponse;
import com.example.task_management.interfaces.dto.response.task.TaskResponse;
import com.example.task_management.application.usecases.task.AssignTaskUseCase;
import com.example.task_management.application.usecases.task.CreateTaskUseCase;
import com.example.task_management.application.usecases.task.DeleteTaskUseCase;
import com.example.task_management.application.usecases.task.GetTaskDetailUseCase;
import com.example.task_management.application.usecases.task.GetTaskUseCase;
import com.example.task_management.application.usecases.task.MoveTaskUseCase;
import com.example.task_management.interfaces.mappers.TaskResponseMapper;
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
    private final GetTaskDetailUseCase getTaskDetailUseCase;
    private final DeleteTaskUseCase deleteTaskUseCase;
    private final TaskResponseMapper taskResponseMapper;

    public TaskController(CreateTaskUseCase createTaskUseCase, GetTaskUseCase getTaskUseCase, 
                          MoveTaskUseCase moveTaskUseCase, AssignTaskUseCase assignTaskUseCase,
                          GetTaskDetailUseCase getTaskDetailUseCase,
                          DeleteTaskUseCase deleteTaskUseCase,
                          TaskResponseMapper taskResponseMapper) {
        this.createTaskUseCase = createTaskUseCase;
        this.getTaskUseCase = getTaskUseCase;
        this.moveTaskUseCase = moveTaskUseCase;
        this.assignTaskUseCase = assignTaskUseCase;
        this.getTaskDetailUseCase = getTaskDetailUseCase;
        this.deleteTaskUseCase = deleteTaskUseCase;
        this.taskResponseMapper = taskResponseMapper;
    }

    // POST /api/projects/{projectId}/tasks
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {

        var result = createTaskUseCase.createTask(projectId, request, authentication.getName());
        TaskResponse taskResponse = taskResponseMapper.toTaskResponse(result);
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

        var results = getTaskUseCase.getTasks(projectId, status, authentication.getName());
        List<TaskResponse> tasks = results.stream()
                .map(taskResponseMapper::toTaskResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách task thành công", tasks));
    }

    // GET /api/projects/{projectId}/tasks/{taskId}
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDetailResponse>> getTaskDetail(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            Authentication authentication) {

        var result = getTaskDetailUseCase.getTaskDetail(projectId, taskId, authentication.getName());
        TaskDetailResponse taskDetailResponse = taskResponseMapper.toTaskDetailResponse(result);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy chi tiết task thành công", taskDetailResponse));
    }

    // DELETE /api/projects/{projectId}/tasks/{taskId}
    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            Authentication authentication) {

        deleteTaskUseCase.deleteTask(projectId, taskId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Xóa task thành công", null));
    }

    // POST /api/projects/{projectId}/tasks/{taskId}/move
    @PostMapping("/{taskId}/move")
    public ResponseEntity<ApiResponse<TaskResponse>> moveTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody MoveTaskRequest request,
            Authentication authentication) {

        var result = moveTaskUseCase.moveTask(projectId, taskId, request, authentication.getName());
        TaskResponse taskResponse = taskResponseMapper.toTaskResponse(result);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Di chuyển task thành công", taskResponse));
    }

    // POST /api/projects/{projectId}/tasks/{taskId}/assign
    @PostMapping("/{taskId}/assign")
    public ResponseEntity<ApiResponse<TaskResponse>> assignTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {

        var result = assignTaskUseCase.assignTask(projectId, taskId, request, authentication.getName());
        TaskResponse taskResponse = taskResponseMapper.toTaskResponse(result);
        String message = request.getAssigneeId() == null 
            ? "Hủy giao task thành công" 
            : "Giao task thành công";
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), message, taskResponse));
    }
}
