package com.example.task_management.application.usecases.impl.task;

import com.example.task_management.application.dto.request.task.AssignTaskRequest;
import com.example.task_management.application.dto.response.task.TaskResponse;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.task.TaskCommandRepository;
import com.example.task_management.application.repositories.task.TaskQueryRepository;
import com.example.task_management.application.usecases.task.AssignTaskUseCase;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.services.TaskAssignerService;
import com.example.task_management.interfaces.mappers.TaskMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignTaskUseCaseImpl implements AssignTaskUseCase {

    private static final Logger log = LoggerFactory.getLogger(AssignTaskUseCaseImpl.class);

    private final TaskQueryRepository taskQueryRepository;
    private final TaskCommandRepository taskCommandRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TaskAssignerService taskAssignerService;

    public AssignTaskUseCaseImpl(TaskQueryRepository taskQueryRepository, TaskCommandRepository taskCommandRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository, TaskMapper taskMapper, TaskAssignerService taskAssignerService) {
        this.taskQueryRepository = taskQueryRepository;
        this.taskCommandRepository = taskCommandRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
        this.taskAssignerService = taskAssignerService;
    }

    @Override
    @Transactional
    public TaskResponse assignTask(Long projectId, Long taskId, AssignTaskRequest request, String userEmail) {
        log.info("[AssignTask] Bắt đầu - projectId={}, taskId={}, assigneeId={}", 
                projectId, taskId, request.getAssigneeId());

        // 1. Lấy task
        Task task = taskQueryRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.error("[AssignTask] Task không tồn tại: taskId={}", taskId);
                    return new IllegalArgumentException("Task không tồn tại");
                });
        log.debug("[AssignTask] Tìm thấy task: id={}", task.getId());

        // 2. Validate task thuộc project
        if (!task.belongsToProject(projectId)) {
            log.error("[AssignTask] Task không thuộc project: taskId={}, projectId={}", taskId, projectId);
            throw new IllegalArgumentException("Task không thuộc dự án này");
        }

        // 3. Lấy assigner (người đang thực hiện giao task)
        User assigner = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("[AssignTask] Assigner không tồn tại");
                    return new IllegalArgumentException("Người dùng không tồn tại");
                });
        log.debug("[AssignTask] Assigner: id={}", assigner.getId());

        // 4. Check assigner là thành viên ACCEPTED của project
        log.debug("[AssignTask] Validate assigner membership...");
        taskAssignerService.validateAssignerMembership(projectId, assigner.getId());

        // 5. Nếu có assignee (không phải unassign), validate
        Long assigneeId = request.getAssigneeId();
        if (assigneeId != null) {
            log.debug("[AssignTask] Validate assignee: assigneeId={}", assigneeId);
            taskAssignerService.validateAssignee(projectId, assigneeId);
        } else {
            log.debug("[AssignTask] Unassign task (assigneeId=null)");
        }

        // 6. Gọi domain method assign
        log.debug("[AssignTask] Gọi task.assignTo({})", assigneeId);
        task.assignTo(assigneeId);

        // 7. Save và return
        Task savedTask = taskCommandRepository.save(task);
        log.info("[AssignTask] Hoàn thành - taskId={}, assigneeId={}", savedTask.getId(), assigneeId);
        
        return taskMapper.toTaskResponse(savedTask);
    }


}
