package rom41k.vasilek.service.interfaces;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
}
