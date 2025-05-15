package rom41k.Rhythmix.service.interfaces;

import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.LoginUserDto;
import rom41k.Rhythmix.dto.RegisterUserDto;
import rom41k.Rhythmix.dto.VerifyUserDto;

public interface AuthenticationService {
    User signup(RegisterUserDto input);
    User authenticate(LoginUserDto input);
    void verifyUser(VerifyUserDto input);
    void resendVerificationCode(String email);
}
