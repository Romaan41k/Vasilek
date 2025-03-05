package rom41k.Rhythmix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.UserDTO;
import rom41k.Rhythmix.service.interfaces.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    public ResponseEntity<Object> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            userService.loadPlaylistsAndTracks(user.getId());

            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            userDTO.setName(user.getName());
            userDTO.setRole(user.getRole().name());

            List<String> playlists = user.getPlaylists().stream()
                    .map(playlist -> playlist.getName())
                    .collect(Collectors.toList());
            userDTO.setPlaylists(playlists);

            List<String> tracks = user.getTracks().stream()
                    .map(track -> track.getTitle())
                    .collect(Collectors.toList());
            userDTO.setTracks(tracks);

            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid user data");
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        return ResponseEntity.ok(userService.allUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<String> updatePassword(@PathVariable Long id, @RequestBody String newPassword) {
        userService.updatePassword(id, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }
}
