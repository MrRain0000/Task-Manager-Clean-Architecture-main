package com.example.task_management.interfaces.controllers;

import com.example.task_management.application.DTOUsecase.response.project.ProjectDetailResult;
import com.example.task_management.application.DTOUsecase.response.project.ProjectListResult;
import com.example.task_management.application.DTOUsecase.response.project.ProjectResult;
import com.example.task_management.interfaces.dto.request.project.CreateProjectRequest;
import com.example.task_management.interfaces.dto.request.project.UpdateProjectRequest;
import com.example.task_management.interfaces.dto.response.ApiResponse;
import com.example.task_management.interfaces.dto.response.project.ProjectDetailResponse;
import com.example.task_management.interfaces.dto.response.project.ProjectListResponse;
import com.example.task_management.interfaces.dto.response.project.ProjectResponse;
import com.example.task_management.application.usecases.project.CreateProjectUseCase;
import com.example.task_management.application.usecases.project.DeleteProjectUseCase;
import com.example.task_management.application.usecases.project.GetProjectDetailUseCase;
import com.example.task_management.application.usecases.project.GetProjectListUseCase;
import com.example.task_management.application.usecases.project.UpdateProjectUseCase;
import com.example.task_management.interfaces.mappers.ProjectRequestMapper;
import com.example.task_management.interfaces.mappers.ProjectResponseMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

        private final CreateProjectUseCase createProjectUseCase;
        private final DeleteProjectUseCase deleteProjectUseCase;
        private final GetProjectListUseCase getProjectListUseCase;
        private final GetProjectDetailUseCase getProjectDetailUseCase;
        private final UpdateProjectUseCase updateProjectUseCase;
        private final ProjectRequestMapper projectRequestMapper;
        private final ProjectResponseMapper projectResponseMapper;

        public ProjectController(
                        CreateProjectUseCase createProjectUseCase,
                        DeleteProjectUseCase deleteProjectUseCase,
                        GetProjectListUseCase getProjectListUseCase,
                        GetProjectDetailUseCase getProjectDetailUseCase,
                        UpdateProjectUseCase updateProjectUseCase,
                        ProjectRequestMapper projectRequestMapper,
                        ProjectResponseMapper projectResponseMapper) {
                this.createProjectUseCase = createProjectUseCase;
                this.deleteProjectUseCase = deleteProjectUseCase;
                this.getProjectListUseCase = getProjectListUseCase;
                this.getProjectDetailUseCase = getProjectDetailUseCase;
                this.updateProjectUseCase = updateProjectUseCase;
                this.projectRequestMapper = projectRequestMapper;
                this.projectResponseMapper = projectResponseMapper;
        }

        // API: Lấy danh sách dự án
        @GetMapping
        public ResponseEntity<ApiResponse<ProjectListResponse>> getProjects(Authentication authentication) {
                String currentUserEmail = authentication.getName();

                ProjectListResult result = getProjectListUseCase.getMyProjects(currentUserEmail);
                List<ProjectResponse> projectResponses = result.getProjects().stream()
                                .map(projectResponseMapper::toProjectResponse)
                                .collect(Collectors.toList());

                ProjectListResponse responseData = ProjectListResponse.builder()
                                .projects(projectResponses)
                                .totalProjects(result.getTotalProjects())
                                .build();

                return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách dự án thành công", responseData));
        }

        // API: Tạo dự án mới
        @PostMapping
        public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
                        @Valid @RequestBody CreateProjectRequest request,
                        Authentication authentication) {

                // Lấy Email của user đang đăng nhập thông qua SecurityContext (được giải mã từ
                // JWT token gửi lên Header)
                String currentUserEmail = authentication.getName();

                // Pass việc xử lý vào tầng UseCase
                ProjectResult result = createProjectUseCase.createProject(request, currentUserEmail);
                ProjectResponse responseData = projectResponseMapper.toProjectResponse(result);

                // Chuẩn hóa chuỗi trả về
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Dự án đã được tạo thành công",
                                                responseData));
        }

        // API: Xóa dự án
        @DeleteMapping("/{projectId}")
        public ResponseEntity<ApiResponse<Void>> deleteProject(
                        @PathVariable Long projectId,
                        Authentication authentication) {

                String currentUserEmail = authentication.getName();

                deleteProjectUseCase.deleteProject(projectId, currentUserEmail);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiResponse.success(HttpStatus.OK.value(), "Dự án đã được xóa thành công", null));
        }

        // API: Lấy chi tiết project
        @GetMapping("/{projectId}")
        public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectDetail(
                        @PathVariable Long projectId,
                        Authentication authentication) {

                String currentUserEmail = authentication.getName();

                ProjectDetailResult result = getProjectDetailUseCase.getProjectDetail(projectId, currentUserEmail);
                ProjectDetailResponse responseData = projectResponseMapper.toProjectDetailResponse(result);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiResponse.success(HttpStatus.OK.value(), "Lấy chi tiết dự án thành công",
                                                responseData));
        }

        // API: Cập nhật dự án
        @PutMapping("/{projectId}")
        public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
                        @PathVariable Long projectId,
                        @Valid @RequestBody UpdateProjectRequest request,
                        Authentication authentication) {

                String currentUserEmail = authentication.getName();

                ProjectResult result = updateProjectUseCase.updateProject(
                                projectId,
                                projectRequestMapper.toUpdateProjectCommand(request),
                                currentUserEmail);
                ProjectResponse responseData = projectResponseMapper.toProjectResponse(result);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(ApiResponse.success(HttpStatus.OK.value(), "Dự án đã được cập nhật thành công",
                                                responseData));
        }
}
