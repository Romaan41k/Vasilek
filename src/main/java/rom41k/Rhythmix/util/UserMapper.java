package rom41k.Rhythmix.util;

import rom41k.Rhythmix.dto.UserDTO;
import rom41k.Rhythmix.database.entity.User;

public class UserMapper {
    public static UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole().name());
        userDTO.setCreatedAt(user.getCreatedAt());

        return userDTO;
    }
}
