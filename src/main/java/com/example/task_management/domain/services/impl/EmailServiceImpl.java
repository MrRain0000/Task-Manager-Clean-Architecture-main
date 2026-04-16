package com.example.task_management.domain.services.impl;

import com.example.task_management.domain.services.Email.EmailService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final Resend resend;

    @Value("${app.email.from:noreply@example.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public EmailServiceImpl(@Value("${app.email.resend-api-key:}") String apiKey) {
            log.info("RESEND KEY RAW: [{}]", apiKey);
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("RESEND_API_KEY không được cấu hình. Email sẽ không được gửi.");
            this.resend = null;
        } else {
            this.resend = new Resend(apiKey);
        }
    }

    @Override
    public void sendVerificationEmail(String to, String username, String token) {
        String verificationLink = frontendUrl + "/verify-email?token=" + token;

        String htmlContent = buildVerificationEmailHtml(username, verificationLink);

        sendEmail(to, "Xác thực tài khoản", htmlContent);
        log.info("[Email] Đã gửi email xác thực đến: {}", to);
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        String htmlContent = buildPasswordResetEmailHtml(username, resetLink);

        sendEmail(to, "Đặt lại mật khẩu", htmlContent);
        log.info("[Email] Đã gửi email đặt lại mật khẩu đến: {}", to);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        if (resend == null) {
            log.warn("[Email] Resend chưa được cấu hình. Không thể gửi email đến: {}", to);
            return;
        }

        try {
            log.info("=== RESEND DEBUG START ===");
            log.info("FROM EMAIL RAW: [{}]", fromEmail);
            log.info("TO EMAIL RAW: [{}]", to);
            log.info("SUBJECT: [{}]", subject);
            log.info("FROM LENGTH: {}", fromEmail.length());
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            CreateEmailResponse response = resend.emails().send(params);
            log.debug("[Email] Email ID: {}", response.getId());
        } catch (ResendException e) {
            log.error("[Email] Lỗi gửi email: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }

    private String buildVerificationEmailHtml(String username, String verificationLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px;">
                    <h2 style="color: #333;">Xin chào, %s!</h2>
                    <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng click vào nút bên dưới để xác thực email:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Xác thực tài khoản
                        </a>
                    </div>
                    <p>Hoặc copy link này vào trình duyệt:</p>
                    <p style="background-color: #e9ecef; padding: 10px; border-radius: 4px; word-break: break-all;">%s</p>
                    <p style="color: #6c757d; font-size: 14px;">Link sẽ hết hạn sau 24 giờ.</p>
                    <hr style="border: none; border-top: 1px solid #dee2e6; margin: 20px 0;">
                    <p style="color: #6c757d; font-size: 12px;">Nếu bạn không đăng ký tài khoản, vui lòng bỏ qua email này.</p>
                </div>
            </body>
            </html>
            """, username, verificationLink, verificationLink);
    }

    private String buildPasswordResetEmailHtml(String username, String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px;">
                    <h2 style="color: #333;">Đặt lại mật khẩu</h2>
                    <p>Xin chào %s,</p>
                    <p>Bạn đã yêu cầu đặt lại mật khẩu. Click vào nút bên dưới để tiếp tục:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            Đặt lại mật khẩu
                        </a>
                    </div>
                    <p>Hoặc copy link này vào trình duyệt:</p>
                    <p style="background-color: #e9ecef; padding: 10px; border-radius: 4px; word-break: break-all;">%s</p>
                    <p style="color: #6c757d; font-size: 14px;">Link sẽ hết hạn sau 24 giờ.</p>
                    <hr style="border: none; border-top: 1px solid #dee2e6; margin: 20px 0;">
                    <p style="color: #6c757d; font-size: 12px;">Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                </div>
            </body>
            </html>
            """, username, resetLink, resetLink);
    }
}
