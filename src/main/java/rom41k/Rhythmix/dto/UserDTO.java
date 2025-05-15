package rom41k.Rhythmix.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserDTO {
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
