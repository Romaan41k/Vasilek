package rom41k.vasilek.service.interfaces;

import org.springframework.security.core.Authentication;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.UpdateUserRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> allUsers();
    Optional<User> getUserById(Long id);
    User updateUser(Long userId, UpdateUserRequest updateRequest);
    void updatePassword(Long id, String newPassword);
    void deleteUserByEmail(String email);
    Optional<User> getUserByEmail(String email);
    User getUserFromAuthentication(Authentication authentication);
}
