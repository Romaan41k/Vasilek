package rom41k.vasilek.controller;

import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.LoginUserDto;
import rom41k.vasilek.dto.RegisterUserDto;
import rom41k.vasilek.dto.VerifyUserDto;
import rom41k.vasilek.responses.ApiResponse;
import rom41k.vasilek.responses.LoginResponse;
import rom41k.vasilek.service.interfaces.AuthenticationService;
import rom41k.vasilek.service.interfaces.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(new ApiResponse<>(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(new ApiResponse<>(loginResponse));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok(ApiResponse.message("Account verified successfully"));
    }

    @PostMapping("/resend")
    public ResponseEntity<ApiResponse<Map<String, String>>> resendVerificationCode(@RequestParam String email) {
        authenticationService.resendVerificationCode(email);
        return ResponseEntity.ok(ApiResponse.message("Verification code resent"));
    }
}
