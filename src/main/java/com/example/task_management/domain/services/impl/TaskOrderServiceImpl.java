package com.example.task_management.domain.services.impl;

import com.example.task_management.application.repositories.task.TaskQueryRepository;
import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.domain.services.TaskOrderService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation của TaskOrderService
 * Chứa logic phức tạp để reorder tasks trong Kanban board
 */
@Service
public class TaskOrderServiceImpl implements TaskOrderService {

    private final TaskQueryRepository taskQueryRepository;

    public TaskOrderServiceImpl(TaskQueryRepository taskQueryRepository) {
        this.taskQueryRepository = taskQueryRepository;
    }

    @Override
    public List<Task> moveWithinColumn(Long projectId, Task task, Integer fromPosition, Integer toPosition) {
        if (fromPosition.equals(toPosition)) {
            return List.of();
        }

        TaskStatus status = task.getStatus();
        List<Task> tasksToUpdate = new ArrayList<>();

        if (fromPosition < toPosition) {
            // Di chuyển xuống: các task từ fromPosition+1 đến toPosition dịch lên 1 (decreasePosition)
            taskQueryRepository
                    .findAllByProjectIdAndStatusAndPositionGreaterThanEqual(projectId, status, fromPosition + 1)
                    .stream()
                    .filter(t -> t.getPosition() <= toPosition)
                    .forEach(t -> {
                        t.decreasePosition();
                        tasksToUpdate.add(t);
                    });
        } else {
            // Di chuyển lên: các task từ toPosition đến fromPosition-1 dịch xuống 1 (increasePosition)
            taskQueryRepository
                    .findAllByProjectIdAndStatusAndPositionGreaterThanEqual(projectId, status, toPosition)
                    .stream()
                    .filter(t -> t.getPosition() < fromPosition)
                    .forEach(t -> {
                        t.increasePosition();
                        tasksToUpdate.add(t);
                    });
        }

        // Di chuyển task chính
        task.moveTo(status, toPosition);

        return tasksToUpdate;
    }

    @Override
    public List<Task> moveToDifferentColumn(Long projectId, Task task, TaskStatus fromStatus, TaskStatus toStatus,
                                             Integer fromPosition, Integer toPosition) {
        List<Task> tasksToUpdate = new ArrayList<>();

        // Xóa khỏi column cũ - shift các task sau vị trí cũ lên 1 (decreasePosition)
        taskQueryRepository
                .findAllByProjectIdAndStatusAndPositionGreaterThanEqual(projectId, fromStatus, fromPosition + 1)
                .forEach(t -> {
                    t.decreasePosition();
                    tasksToUpdate.add(t);
                });

        // Thêm vào column mới - shift các task từ toPosition trở đi xuống 1 (increasePosition)
        taskQueryRepository
                .findAllByProjectIdAndStatusAndPositionGreaterThanEqual(projectId, toStatus, toPosition)
                .forEach(t -> {
                    t.increasePosition();
                    tasksToUpdate.add(t);
                });

        // Di chuyển task chính sang column mới
        task.moveTo(toStatus, toPosition);

        return tasksToUpdate;
    }
}
