package com.example.task_management.application.usecases.impl.auth;

import com.example.task_management.interfaces.dto.request.auth.GoogleLoginRequest;
import com.example.task_management.interfaces.dto.response.auth.GoogleUserInfo;
import com.example.task_management.application.DTOUsecase.response.auth.AuthResult;
import com.example.task_management.application.mapper.UserMapper;
import com.example.task_management.application.repositories.OAuth2Repository;
import com.example.task_management.application.repositories.UserRepository;
import com.example.task_management.application.usecases.auth.GoogleLoginUseCase;
import com.example.task_management.domain.entities.User;
import com.example.task_management.infrastructure.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GoogleLoginUseCaseImpl implements GoogleLoginUseCase {

    private final OAuth2Repository oAuth2Repository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public GoogleLoginUseCaseImpl(
            OAuth2Repository oAuth2Repository,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.oAuth2Repository = oAuth2Repository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public AuthResult loginWithGoogle(GoogleLoginRequest request) {
        // 1. Giải mã và xác thực Google ID Token (Sử dụng Port, không trực tiếp gọi
        // Google SDK ở đây)
        GoogleUserInfo googleUser = oAuth2Repository.verifyGoogleIdToken(request.getIdToken());

        if (googleUser == null || googleUser.getEmail() == null) {
            throw new IllegalArgumentException("Xác thực Google thất bại hoặc không lấy được Email.");
        }

        // 2. Tra cứu user trong Database theo Email
        User user = userRepository.findByEmail(googleUser.getEmail()).orElseGet(() -> {
            // Nếu chưa có, tự động tạo mới tài khoản
            // Mật khẩu sẽ được random sinh ra một chuỗi cực rối để chặn đăng nhập bằng
            // Password thông thường
            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

            // Username có thể lấy từ email prefix hoặc tên Google
            String username = googleUser.getEmail().split("@")[0];

            User newUser = new User();
            newUser.setEmail(googleUser.getEmail());
            newUser.setUsername(username);
            newUser.setPassword(randomPassword);
            newUser.setVerified(true); // Đăng nhập Google coi như đã xác thực email

            return userRepository.save(newUser);
        });

        // 3. Tạo Token nội bộ hệ thống (JWT)
        String accessToken = jwtTokenProvider.generateToken(user.getEmail());

        return userMapper.toAuthResult(user, accessToken);
    }
}
