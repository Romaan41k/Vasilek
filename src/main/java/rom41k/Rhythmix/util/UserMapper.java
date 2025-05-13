package rom41k.Rhythmix.util;

import rom41k.Rhythmix.dto.UserDTO;
import rom41k.Rhythmix.dto.TrackDTO;
import rom41k.Rhythmix.dto.PlaylistDTO;
import rom41k.Rhythmix.database.entity.User;

import java.util.List;
import java.util.stream.Collectors;

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
