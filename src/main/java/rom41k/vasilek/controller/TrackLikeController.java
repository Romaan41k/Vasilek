package rom41k.vasilek.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import rom41k.vasilek.database.entity.TrackLike;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.TrackLikeDTO;
import rom41k.vasilek.service.interfaces.TrackLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/track-likes")
public class TrackLikeController {

    private final TrackLikeService trackLikeService;

    @Autowired
    public TrackLikeController(TrackLikeService trackLikeService) {
        this.trackLikeService = trackLikeService;
    }

    @PostMapping
    public ResponseEntity<Void> likeTrack(@RequestBody TrackLike trackLike, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);

        User currentUser = new User();
        currentUser.setId(userId);
        trackLike.setUser(currentUser);

        if (trackLike.getTrack() == null || trackLike.getTrack().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (trackLikeService.isTrackLiked(userId, trackLike.getTrack().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        trackLikeService.likeTrack(trackLike);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlikeTrack(@RequestParam Long trackId, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);

        if (!trackLikeService.isTrackLiked(userId, trackId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        trackLikeService.unlikeTrack(userId, trackId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<TrackLikeDTO>> getMyLikes(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<TrackLike> userLikes = trackLikeService.getUserLikes(userId);

        List<TrackLikeDTO> dtoList = userLikes.stream()
                .map(TrackLikeDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("User not authenticated or authentication principal is not of type User");
        }
        return ((User) authentication.getPrincipal()).getId();
    }
}