package rom41k.vasilek.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/verify-email")
    public String verifyEmailPage() {
        return "forward:/verify-email.html";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/privacy")
    public String privacyPage() {
        return "forward:/privacy.html";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "forward:/reset-password.html";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forward:/forgot-password.html";
    }

    @GetMapping("/liked-tracks")
    public String likedTracksPage() {
        return "forward:/liked-tracks.html";
    }
}
