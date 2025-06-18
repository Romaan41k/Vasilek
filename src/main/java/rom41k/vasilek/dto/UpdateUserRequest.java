package rom41k.vasilek.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String name;
    private String email;
    private String oldPassword;
    private String newPassword;
}