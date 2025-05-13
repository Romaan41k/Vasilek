    package rom41k.Rhythmix.controller;

    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.web.bind.annotation.*;
    import rom41k.Rhythmix.database.entity.User;
    import rom41k.Rhythmix.dto.PlaylistDTO;
    import rom41k.Rhythmix.dto.TrackDTO;
    import rom41k.Rhythmix.dto.UpdateUserRequest;
    import rom41k.Rhythmix.dto.UserDTO;
    import rom41k.Rhythmix.service.interfaces.UserService;
    import rom41k.Rhythmix.util.UserMapper;

    import java.security.Principal;
    import java.util.List;
    import java.util.Optional;
    import java.util.stream.Collectors;

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
            String email = userDetails.getUsername(); // Извлекаем email из JWT
            Optional<User> user = userService.getUserByEmail(email);

            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            UserDTO userDTO = UserMapper.convertToDto(user.get());

            return ResponseEntity.ok(userDTO);
        }


    }
