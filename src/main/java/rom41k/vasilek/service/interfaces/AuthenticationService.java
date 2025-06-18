package rom41k.vasilek.service.interfaces;

import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.LoginUserDto;
import rom41k.vasilek.dto.RegisterUserDto;
import rom41k.vasilek.dto.VerifyUserDto;

public interface AuthenticationService {
    User signup(RegisterUserDto input);
    User authenticate(LoginUserDto input);
    void verifyUser(VerifyUserDto input);
    void resendVerificationCode(String email);
}
