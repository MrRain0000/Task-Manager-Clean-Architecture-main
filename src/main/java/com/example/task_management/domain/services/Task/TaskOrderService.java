package com.example.task_management.domain.services.Task;

import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.enums.TaskStatus;

/**
 * Service xử lý reorder tasks trong Kanban board bằng Bulk Update.
 * Không trả về List<Task> nữa - tất cả position updates được thực hiện qua JPQL bulk operations.
 */
public interface TaskOrderService {

    /**
     * Di chuyển task trong cùng một column sử dụng bulk update.
     * Tất cả position shifts được thực hiện trực tiếp trong DB.
     *
     * @param projectId ID của project
     * @param task Task cần di chuyển (chỉ task này được modify trong memory, sau đó save)
     * @param fromPosition Vị trí hiện tại
     * @param toPosition Vị trí mới
     */
    void moveWithinColumn(Long projectId, Task task, Integer fromPosition, Integer toPosition);

    /**
     * Di chuyển task sang column khác sử dụng bulk update.
     * Gồm 2 bước bulk update: remove khỏi source column + insert vào target column.
     *
     * @param projectId ID của project
     * @param task Task cần di chuyển (chỉ task này được modify trong memory, sau đó save)
     * @param fromStatus Status cũ
     * @param toStatus Status mới
     * @param fromPosition Vị trí cũ
     * @param toPosition Vị trí mới
     */
    void moveToDifferentColumn(Long projectId, Task task, TaskStatus fromStatus, TaskStatus toStatus,
                               Integer fromPosition, Integer toPosition);
}
