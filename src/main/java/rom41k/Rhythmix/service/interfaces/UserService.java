package rom41k.Rhythmix.service.interfaces;

import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.UpdateUserRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> allUsers();
    Optional<User> getUserById(Long id);
    User updateUser(Long userId, UpdateUserRequest updateRequest);
    void updatePassword(Long id, String newPassword);
    void deleteUserByEmail(String email);
    Optional<User> getUserByEmail(String email);
}
