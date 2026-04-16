package com.example.task_management.application.usecases.impl.user;

import com.example.task_management.application.DTOUsecase.request.user.ChangePasswordCommand;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.user.ChangePasswordUseCase;
import com.example.task_management.domain.entities.User;
import com.example.task_management.interfaces.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC23 – Đổi mật khẩu user.
 * Use Case đóng vai trò Orchestrator, validate mật khẩu cũ và hash mật khẩu mới.
 */
@Service
public class ChangePasswordUseCaseImpl implements ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordUseCaseImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, ChangePasswordCommand command) {
        // 1. Lấy user hiện tại
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Người dùng không tồn tại"));

        // 2. Validate mật khẩu cũ
        if (!passwordEncoder.matches(command.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        // 3. Validate mật khẩu mới (ít nhất 6 ký tự)
        if (command.getNewPassword() == null || command.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }

        // 4. Hash và lưu mật khẩu mới
        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        userRepository.save(user);
    }
}
