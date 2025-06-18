package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import rom41k.vasilek.database.entity.PasswordResetToken;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.repository.PasswordResetTokenRepository;
import rom41k.vasilek.repository.UserRepository;
import rom41k.vasilek.service.interfaces.EmailService;
import rom41k.vasilek.service.interfaces.PasswordResetService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiresAt);
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:8080/reset-password?token=" + token;
        String emailContent = "Для сброса пароля перейдите по ссылке: " + resetLink;

        try {
            emailService.sendSimpleEmail(
                    user.getEmail(),
                    "Сброс пароля",
                    emailContent
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to send reset email");
        }
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(newPassword);
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}

