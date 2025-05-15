package rom41k.Rhythmix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rom41k.Rhythmix.database.entity.GenreSubscription;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.GenreSubscriptionDTO;
import rom41k.Rhythmix.service.interfaces.GenreSubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/genre-subscriptions")
public class GenreSubscriptionController {

    private final GenreSubscriptionService genreSubscriptionService;

    public GenreSubscriptionController(GenreSubscriptionService genreSubscriptionService) {
        this.genreSubscriptionService = genreSubscriptionService;
    }

    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }

    @PostMapping
    public ResponseEntity<GenreSubscriptionDTO> subscribeToGenre(
            @RequestBody GenreSubscription subscription,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        subscription.setUser(user);
        GenreSubscription savedSubscription = genreSubscriptionService.subscribeToGenre(subscription);
        return ResponseEntity.ok(toDTO(savedSubscription));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GenreSubscriptionDTO>> getMySubscriptions(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<GenreSubscription> subscriptions = genreSubscriptionService.findByUserId(userId);
        List<GenreSubscriptionDTO> dtoList = subscriptions.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @DeleteMapping
    public ResponseEntity<Void> unsubscribeFromGenre(
            @RequestParam String genre,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        boolean deleted = genreSubscriptionService.unsubscribeFromGenre(userId, genre);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    private GenreSubscriptionDTO toDTO(GenreSubscription sub) {
        return new GenreSubscriptionDTO(
                sub.getId(),
                sub.getGenre(),
                sub.getSubscribedAt(),
                sub.getUser().getId(),
                sub.getUser().getName()
        );
    }
}
