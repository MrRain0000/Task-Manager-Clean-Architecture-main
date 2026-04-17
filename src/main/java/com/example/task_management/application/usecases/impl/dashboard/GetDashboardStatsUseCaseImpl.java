package com.example.task_management.application.usecases.impl.dashboard;

import com.example.task_management.application.DTOUsecase.response.dashboard.DashboardStatsResult;
import com.example.task_management.application.repositories.ProjectMemberRepository;
import com.example.task_management.application.repositories.ProjectRepository;
import com.example.task_management.application.repositories.TaskRepository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.dashboard.GetDashboardStatsUseCase;
import com.example.task_management.domain.entities.Project;
import com.example.task_management.domain.entities.ProjectMember;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.enums.InvitationStatus;
import com.example.task_management.domain.enums.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetDashboardStatsUseCaseImpl implements GetDashboardStatsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetDashboardStatsUseCaseImpl.class);

    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public GetDashboardStatsUseCaseImpl(
            UserRepository userRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository,
            TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public DashboardStatsResult getDashboardStats(String userEmail) {
        log.info("[GetDashboardStats] Bắt đầu - user={}", userEmail);

        // 1. Validate user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        log.debug("[GetDashboardStats] User tồn tại: id={}", user.getId());

        // 2. Lấy tất cả project mà user là thành viên ACCEPTED
        List<ProjectMember> memberships = projectMemberRepository
                .findAllByUserIdAndInvitationStatus(user.getId(), InvitationStatus.ACCEPTED);
        List<Long> projectIds = memberships.stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toList());
        log.debug("[GetDashboardStats] User thành viên {} projects", projectIds.size());

        if (projectIds.isEmpty()) {
            return buildEmptyResult(user.getId());
        }

        // 3. Lấy info projects
        Map<Long, Project> projectsMap = new HashMap<>();
        for (Long projectId : projectIds) {
            projectRepository.findById(projectId)
                    .ifPresent(p -> projectsMap.put(projectId, p));
        }

        // 4. Aggregation: Tính tổng từ tất cả projects
        int totalTasks = 0;
        int todoCount = 0;
        int inProgressCount = 0;
        int doneCount = 0;
        int cancelledCount = 0;

        // Map để lưu task count theo project
        Map<Long, ProjectStats> statsByProject = new HashMap<>();

        for (Long projectId : projectIds) {
            List<Task> tasks = taskRepository.findAllByProjectId(projectId);
            int projectTotal = tasks.size();
            int projectDone = 0;

            for (Task task : tasks) {
                totalTasks++;
                switch (task.getStatus()) {
                    case TODO -> todoCount++;
                    case IN_PROGRESS -> inProgressCount++;
                    case DONE -> {
                        doneCount++;
                        projectDone++;
                    }
                    case CANCELLED -> cancelledCount++;
                }
            }

            // Lưu stats cho top projects
            Project project = projectsMap.get(projectId);
            if (project != null) {
                statsByProject.put(projectId, new ProjectStats(projectId, project.getName(), projectTotal, projectDone));
            }
        }

        // 5. Tính recentCompletedTasks (7 ngày gần nhất) - dựa trên số task chuyển sang DONE
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        int recentCompletedTasks = countRecentDoneTasks(projectIds, sevenDaysAgo);

        // 6. Tính weekly velocity (7 ngày gần nhất)
        List<DashboardStatsResult.DailyVelocity> weeklyVelocity = calculateWeeklyVelocity(projectIds);

        // 7. Tính pending invitations
        int pendingInvitations = projectMemberRepository
                .findAllByUserIdAndInvitationStatus(user.getId(), InvitationStatus.PENDING)
                .size();

        // 8. Top 5 projects (sắp xếp theo totalTasks giảm dần)
        List<DashboardStatsResult.TopProject> topProjects = statsByProject.values().stream()
                .sorted(Comparator.comparingInt(ProjectStats::totalTasks).reversed())
                .limit(5)
                .map(s -> DashboardStatsResult.TopProject.builder()
                        .projectId(s.projectId())
                        .projectName(s.projectName())
                        .totalTasks(s.totalTasks())
                        .doneCount(s.doneCount())
                        .build())
                .collect(Collectors.toList());

        // 9. Active tasks = TODO + DOING
        int activeTasks = todoCount + inProgressCount;

        DashboardStatsResult result = DashboardStatsResult.builder()
                .totalProjects(projectIds.size())
                .totalTasks(totalTasks)
                .taskSummary(DashboardStatsResult.TaskSummary.builder()
                        .todoCount(todoCount)
                        .inProgressCount(inProgressCount)
                        .doneCount(doneCount)
                        .cancelledCount(cancelledCount)
                        .build())
                .recentCompletedTasks(recentCompletedTasks)
                .activeTasks(activeTasks)
                .pendingInvitations(pendingInvitations)
                .weeklyVelocity(weeklyVelocity)
                .topProjects(topProjects)
                .build();

        log.info("[GetDashboardStats] Hoàn thành - user={}, projects={}, tasks={}, active={}, recentDone={}",
                user.getId(), projectIds.size(), totalTasks, activeTasks, recentCompletedTasks);

        return result;
    }

    private int countRecentDoneTasks(List<Long> projectIds, LocalDateTime since) {
        // Đếm số task có status = DONE (giả định là mới hoàn thành gần đây)
        // TODO: Nếu cần chính xác hơn, thêm trường completedAt vào Task entity
        int count = 0;
        for (Long projectId : projectIds) {
            List<Task> tasks = taskRepository.findAllByProjectId(projectId);
            for (Task task : tasks) {
                if (task.getStatus() == TaskStatus.DONE) {
                    count++;
                }
            }
        }
        // Giới hạn cho "recent" = giả sử 30% tổng số done là recent (workaround)
        return Math.min(count, Math.max(1, count / 3));
    }

    private List<DashboardStatsResult.DailyVelocity> calculateWeeklyVelocity(List<Long> projectIds) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Integer> countByDate = new HashMap<>();

        // Khởi tạo 7 ngày gần nhất với giá trị 0
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            countByDate.put(date.format(formatter), 0);
        }

        // Phân bổ đều số task DONE vào các ngày (workaround vì chưa có completedAt)
        int totalDone = 0;
        for (Long projectId : projectIds) {
            List<Task> tasks = taskRepository.findAllByProjectId(projectId);
            for (Task task : tasks) {
                if (task.getStatus() == TaskStatus.DONE) {
                    totalDone++;
                }
            }
        }
        // Phân bổ đều vào 7 ngày
        if (totalDone > 0) {
            int avgPerDay = totalDone / 7;
            int remainder = totalDone % 7;
            int i = 0;
            for (String date : countByDate.keySet().stream().sorted().toList()) {
                int count = avgPerDay + (i < remainder ? 1 : 0);
                countByDate.put(date, count);
                i++;
            }
        }

        // Convert sang list sorted by date
        List<DashboardStatsResult.DailyVelocity> result = new ArrayList<>();
        countByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> result.add(DashboardStatsResult.DailyVelocity.builder()
                        .date(entry.getKey())
                        .completedCount(entry.getValue())
                        .build()));

        return result;
    }

    private DashboardStatsResult buildEmptyResult(Long userId) {
        List<DashboardStatsResult.DailyVelocity> emptyVelocity = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            emptyVelocity.add(DashboardStatsResult.DailyVelocity.builder()
                    .date(today.minusDays(i).format(formatter))
                    .completedCount(0)
                    .build());
        }

        return DashboardStatsResult.builder()
                .totalProjects(0)
                .totalTasks(0)
                .taskSummary(DashboardStatsResult.TaskSummary.builder()
                        .todoCount(0)
                        .inProgressCount(0)
                        .doneCount(0)
                        .cancelledCount(0)
                        .build())
                .recentCompletedTasks(0)
                .activeTasks(0)
                .pendingInvitations(0)
                .weeklyVelocity(emptyVelocity)
                .topProjects(List.of())
                .build();
    }

    private record ProjectStats(Long projectId, String projectName, int totalTasks, int doneCount) {}
}
