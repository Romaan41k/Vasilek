package rom41k.Rhythmix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.TrackLike;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.repository.UserRepository;
import rom41k.Rhythmix.service.interfaces.TrackLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/track-likes")
public class TrackLikeController {

    @Autowired
    private TrackLikeService trackLikeService;

    // Лайк
    @PostMapping
    public ResponseEntity<Void> likeTrack(@RequestBody TrackLike trackLike, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);

        // Убедитесь, что user не null
        if (trackLike.getUser() == null) {
            User user = new User();
            user.setId(userId);
            trackLike.setUser(user);  // Устанавливаем пользователя в TrackLike
        }

        // Проверка, был ли уже лайк
        if (trackLikeService.isTrackLiked(userId, trackLike.getTrack().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Уже лайкнуто
        }

        // Сохраняем лайк
        trackLikeService.likeTrack(trackLike);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // Лайк успешно поставлен
    }

    // Убрать лайк
    @DeleteMapping
    public ResponseEntity<Void> unlikeTrack(@RequestParam Long trackId, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);

        // Проверка, существует ли лайк
        if (!trackLikeService.isTrackLiked(userId, trackId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Лайк не найден
        }

        // Удаляем лайк
        trackLikeService.unlikeTrack(userId, trackId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Лайк успешно убран
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
