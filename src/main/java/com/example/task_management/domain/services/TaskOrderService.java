package com.example.task_management.domain.services;

import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.enums.TaskStatus;

import java.util.List;


public interface TaskOrderService {

    /**
     * Di chuyển task trong cùng một column
     * @param projectId ID của project
     * @param task Task cần di chuyển
     * @param fromPosition Vị trí hiện tại
     * @param toPosition Vị trí mới
     * @return Danh sách các task cần cập nhật (đã được modify)
     */
    List<Task> moveWithinColumn(Long projectId, Task task, Integer fromPosition, Integer toPosition);

    /**
     * Di chuyển task sang column khác
     * @param projectId ID của project
     * @param task Task cần di chuyển
     * @param fromStatus Status cũ
     * @param toStatus Status mới
     * @param fromPosition Vị trí cũ
     * @param toPosition Vị trí mới
     * @return Danh sách các task cần cập nhật (đã được modify)
     */
    List<Task> moveToDifferentColumn(Long projectId, Task task, TaskStatus fromStatus, TaskStatus toStatus,
                                     Integer fromPosition, Integer toPosition);
}
