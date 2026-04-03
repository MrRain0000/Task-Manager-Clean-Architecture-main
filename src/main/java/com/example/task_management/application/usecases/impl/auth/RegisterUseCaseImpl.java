package com.example.task_management.application.usecases.impl.auth;

import com.example.task_management.interfaces.dto.request.auth.RegisterRequest;
import com.example.task_management.application.DTOUsecase.response.auth.RegisterResult;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.repositories.VerificationTokenRepository;
import com.example.task_management.application.usecases.auth.RegisterUseCase;
import com.example.task_management.domain.entities.User;
import com.example.task_management.domain.entities.VerificationToken;
import com.example.task_management.domain.services.Email.EmailService;
import com.example.task_management.application.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUseCaseImpl implements RegisterUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUseCaseImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    public RegisterUseCaseImpl(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            VerificationTokenRepository tokenRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public RegisterResult register(RegisterRequest request) {
        log.info("[Register] Bắt đầu đăng ký user: email={}", request.getEmail());

        // 1. Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            log.error("[Register] Email đã tồn tại: {}", request.getEmail());
            throw new IllegalArgumentException("Email đã được sử dụng: " + request.getEmail());
        }

        // 2. Tạo domain entity, hash password
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setVerified(false);

        // 3. Lưu vào DB
        User savedUser = userRepository.save(newUser);
        log.info("[Register] Đã lưu user: userId={}", savedUser.getId());

        // 4. Tạo verification token
        VerificationToken token = new VerificationToken(savedUser.getId());
        tokenRepository.save(token);
        log.debug("[Register] Đã tạo verification token");

        // 5. Gửi email xác thực
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getUsername(), token.getToken());
            log.info("[Register] Đã gửi email xác thực");
        } catch (Exception e) {
            log.error("[Register] Lỗi gửi email: {}", e.getMessage());
            // Không throw exception - user đã đăng ký thành công, có thể gửi lại email sau
        }

        // 6. Convert → DTO response
        RegisterResult response = userMapper.toRegisterResult(savedUser);
        log.info("[Register] Hoàn thành đăng ký");
        return response;
    }
}
