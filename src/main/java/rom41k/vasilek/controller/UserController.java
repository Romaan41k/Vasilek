package rom41k.vasilek.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.UpdateUserRequest;
import rom41k.vasilek.dto.UserDTO;
import rom41k.vasilek.service.interfaces.UserService;
import rom41k.vasilek.util.UserMapper;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateUser(@RequestBody UpdateUserRequest updateRequest, @AuthenticationPrincipal User user) {
        User updatedUser = userService.updateUser(user.getId(), updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok("User deleted successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getUserByEmail(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<User> user = userService.getUserByEmail(email);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserDTO userDTO = UserMapper.convertToDto(user.get());

        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(UserMapper::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
