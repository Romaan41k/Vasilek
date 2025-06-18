package rom41k.vasilek.service.interfaces;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendVerificationEmail(String to, String subject, String text) throws MessagingException;
    void sendSimpleEmail(String to, String subject, String text) throws MessagingException;
}
