package com.example.task_management.domain.services.impl;

import com.example.task_management.domain.entities.Task;
import com.example.task_management.domain.enums.TaskStatus;
import com.example.task_management.domain.services.Task.TaskOrderService;
import com.example.task_management.infrastructure.persistence.jparepositories.TaskJpaRepository;

import org.springframework.stereotype.Service;

/**
 * Implementation của TaskOrderService sử dụng JPQL Bulk Update.
 * Tất cả position updates được thực hiện trực tiếp trong DB, không load entity vào memory.
 */
@Service
public class TaskOrderServiceImpl implements TaskOrderService {

    private final TaskJpaRepository taskJpaRepository;

    public TaskOrderServiceImpl(TaskJpaRepository taskJpaRepository) {
        this.taskJpaRepository = taskJpaRepository;
    }

    @Override
    public void moveWithinColumn(Long projectId, Task task, Integer fromPosition, Integer toPosition) {
        if (fromPosition.equals(toPosition)) {
            return;
        }

        TaskStatus status = task.getStatus();

        if (fromPosition < toPosition) {
            // ═══════════════════════════════════════════════════════════════════
            // MOVE XUỐNG: fromPosition → toPosition (ví dụ: 0 → 3)
            // 
            // Trước:  [A(0), B(1), C(2), D(3), E(4)]
            //                   ↑
            // Sau:    [A(0), B(1), C(2), D(3), E(4)]  (A đến vị trí 3)
            //          ↑           ↑
            //          B dịch lên  C dịch lên
            // 
            // Logic: Các task từ (fromPosition+1) đến toPosition dịch lên 1
            //        Tức là: position = position - 1
            //        Range: [fromPosition+1, toPosition]
            // ═══════════════════════════════════════════════════════════════════
            taskJpaRepository.bulkDecrementPositionInRange(
                projectId,
                status,
                fromPosition + 1,  // start: ngay sau task được kéo
                toPosition         // end: đến vị trí đích
            );
        } else {
            // ═══════════════════════════════════════════════════════════════════
            // MOVE LÊN: fromPosition → toPosition (ví dụ: 3 → 0)
            // 
            // Trước:  [A(0), B(1), C(2), D(3), E(4)]
            //                              ↑
            // Sau:    [A(0), B(1), C(2), D(3), E(4)]  (D đến vị trí 0)
            //                  ↑
            //                  B dịch xuống
            // 
            // Logic: Các task từ toPosition đến (fromPosition-1) dịch xuống 1
            //        Tức là: position = position + 1
            //        Range: [toPosition, fromPosition-1]
            // ═══════════════════════════════════════════════════════════════════
            taskJpaRepository.bulkIncrementPositionInRange(
                projectId,
                status,
                toPosition,           // start: vị trí đích
                fromPosition - 1      // end: ngay trước task được kéo
            );
        }

        // Cập nhật task chính trong memory (sẽ được save bởi caller)
        task.moveTo(status, toPosition);
    }

    @Override
    public void moveToDifferentColumn(Long projectId, Task task, TaskStatus fromStatus, TaskStatus toStatus,
                                      Integer fromPosition, Integer toPosition) {
        // ═══════════════════════════════════════════════════════════════════
        // STEP 1: REMOVE khỏi source column
        // 
        // Giảm position cho các task phía SAU vị trí bị xóa
        // Ví dụ: Remove task ở position 1
        // Trước:  [A(0), B(1), C(2), D(3)]
        // Sau:    [A(0), C(1), D(2)]       (C, D dịch lên)
        //
        // bulkDecrementPositionAfter(1):
        // UPDATE tasks SET position = position - 1
        // WHERE position > 1  (tức là C và D)
        // ═══════════════════════════════════════════════════════════════════
        taskJpaRepository.bulkDecrementPositionAfter(projectId, fromStatus, fromPosition);

        // ═══════════════════════════════════════════════════════════════════
        // STEP 2: INSERT vào target column
        // 
        // Tăng position cho các task từ vị trí insert trở đi
        // Ví dụ: Insert vào position 2
        // Trước:  [X(0), Y(1), Z(2)]
        // Sau:    [X(0), Y(1), NEW(2), Z(3)]  (Y giữ nguyên, Z dịch xuống)
        //
        // bulkIncrementPositionFrom(2):
        // UPDATE tasks SET position = position + 1
        // WHERE position >= 2  (tức là Z)
        // ═══════════════════════════════════════════════════════════════════
        taskJpaRepository.bulkIncrementPositionFrom(projectId, toStatus, toPosition);

        // Cập nhật task chính: đổi status và position
        task.moveTo(toStatus, toPosition);
    }
}
