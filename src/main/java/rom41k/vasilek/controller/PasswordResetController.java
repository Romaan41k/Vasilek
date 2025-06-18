package rom41k.vasilek.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rom41k.vasilek.dto.PasswordResetRequest;
import rom41k.vasilek.service.interfaces.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        passwordResetService.requestPasswordReset(email);
        return "Письмо для сброса пароля отправлено";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String token,
            @RequestBody PasswordResetRequest request
    ) {
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        passwordResetService.resetPassword(token, encodedPassword);
        return "Пароль успешно изменен";
    }
}
