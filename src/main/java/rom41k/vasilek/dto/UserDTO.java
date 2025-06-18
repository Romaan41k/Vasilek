package rom41k.vasilek.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
