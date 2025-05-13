package rom41k.Rhythmix.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.database.enums.Role;
import rom41k.Rhythmix.dto.LoginUserDto;
import rom41k.Rhythmix.dto.RegisterUserDto;
import rom41k.Rhythmix.dto.VerifyUserDto;
import rom41k.Rhythmix.exception.AccountNotVerifiedException;
import rom41k.Rhythmix.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${email.template-path}")
    private String emailTemplatePath;

    // Регистрация пользователя
    public User signup(RegisterUserDto input) {
        // Проверяем, существует ли уже пользователь с таким email
        if (userRepository.findByEmail(input.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Создаем нового пользователя
        User user = new User();
        user.setName(input.username());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.valueOf(input.role()));
        user.setEnabled(false); // Статус не подтвержден
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        // Сохраняем пользователя
        userRepository.save(user);

        // Отправляем email с кодом подтверждения
        sendVerificationEmail(user);

        return user;
    }

    // Аутентификация пользователя
    public User authenticate(LoginUserDto input) {
        // Ищем пользователя по email
        User user = userRepository.findByEmail(input.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверяем, подтверждена ли учетная запись
        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify your account.");
        }

        // Аутентификация
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.email(), input.password())
        );

        return user;
    }

    // Подтверждение учетной записи пользователя
    public void verifyUser(VerifyUserDto input) {
        User user = userRepository.findByEmail(input.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }
        if (!user.getVerificationCode().equals(input.verificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }

        user.setEnabled(true); // Подтверждаем учетную запись
        user.setVerificationCode(null); // Убираем код подтверждения
        user.setVerificationCodeExpiresAt(null); // Убираем срок действия кода

        userRepository.save(user); // Сохраняем изменения
    }

    // Повторная отправка кода подтверждения
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new RuntimeException("Account is already verified");
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        sendVerificationEmail(user); // Отправляем новый код
        userRepository.save(user); // Сохраняем изменения
    }

    // Отправка email с кодом подтверждения
    private void sendVerificationEmail(User user) {
        String verificationCode = user.getVerificationCode();
        String htmlMessage = readEmailTemplate().replace("${verificationCode}", verificationCode);

        try {
            emailService.sendVerificationEmail(user.getEmail(), "Account Verification", htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    // Чтение шаблона email
    private String readEmailTemplate() {
        try {
            Path path = Path.of(emailTemplatePath);
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Error reading email template file", e);
        }
    }

    // Генерация кода подтверждения
    private String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}
