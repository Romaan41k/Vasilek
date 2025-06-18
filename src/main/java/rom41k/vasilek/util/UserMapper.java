package rom41k.vasilek.util;

import rom41k.vasilek.dto.UserDTO;
import rom41k.vasilek.database.entity.User;

public class UserMapper {
    public static UserDTO convertToDto(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole().name());
        userDTO.setCreatedAt(user.getCreatedAt());

        return userDTO;
    }
}
