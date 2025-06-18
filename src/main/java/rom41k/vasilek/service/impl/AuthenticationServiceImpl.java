package rom41k.vasilek.service.impl;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.database.enums.Role;
import rom41k.vasilek.dto.LoginUserDto;
import rom41k.vasilek.dto.RegisterUserDto;
import rom41k.vasilek.dto.VerifyUserDto;
import rom41k.vasilek.exception.AccountNotVerifiedException;
import rom41k.vasilek.repository.UserRepository;
import rom41k.vasilek.service.interfaces.EmailService;
import rom41k.vasilek.service.interfaces.AuthenticationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${email.template-path}")
    private String emailTemplatePath;

    @Override
    public User signup(RegisterUserDto input) {
        if (userRepository.findByEmail(input.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setName(input.username());
        user.setEmail(input.email());
        user.setPassword(passwordEncoder.encode(input.password()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.valueOf(input.role()));
        user.setEnabled(false);
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);
        sendVerificationEmail(user);

        return user;
    }

    @Override
    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new AccountNotVerifiedException("Account not verified. Please verify your account.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.email(), input.password())
        );

        return user;
    }

    @Override
    public void verifyUser(VerifyUserDto input) {
        User user = userRepository.findByEmail(input.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }

        if (!user.getVerificationCode().equals(input.verificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);

        userRepository.save(user);
    }

    @Override
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new RuntimeException("Account is already verified");
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        sendVerificationEmail(user);
        userRepository.save(user);
    }

    private void sendVerificationEmail(User user) {
        String verificationCode = user.getVerificationCode();
        String htmlMessage = readEmailTemplate().replace("${verificationCode}", verificationCode);

        try {
            emailService.sendVerificationEmail(user.getEmail(), "Account Verification", htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String readEmailTemplate() {
        try {
            Path path = Path.of(emailTemplatePath);
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Error reading email template file", e);
        }
    }

    private String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}