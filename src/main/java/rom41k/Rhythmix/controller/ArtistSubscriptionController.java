package rom41k.Rhythmix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rom41k.Rhythmix.database.entity.ArtistSubscription;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.ArtistSubscriptionDTO;
import rom41k.Rhythmix.service.interfaces.ArtistSubscriptionService;

import java.util.List;

@RestController
@RequestMapping("/api/artist-subscriptions")
public class ArtistSubscriptionController {

    private final ArtistSubscriptionService artistSubscriptionService;

    public ArtistSubscriptionController(ArtistSubscriptionService artistSubscriptionService) {
        this.artistSubscriptionService = artistSubscriptionService;
    }

    // Метод для получения ID пользователя из аутентификации
    private Long getUserId(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }

    // Подписка на артиста
    @PostMapping
    public ResponseEntity<ArtistSubscriptionDTO> subscribeToArtist(
            @RequestBody ArtistSubscription subscription,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        subscription.setUser(user);

        ArtistSubscription savedSubscription = artistSubscriptionService.subscribeToArtist(subscription);
        return ResponseEntity.ok(toDTO(savedSubscription)); // Возвращаем DTO
    }

    // Получение подписок текущего пользователя
    @GetMapping("/my")
    public ResponseEntity<List<ArtistSubscriptionDTO>> getMySubscriptions(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<ArtistSubscription> subscriptions = artistSubscriptionService.findByUserId(userId);
        List<ArtistSubscriptionDTO> dtoList = subscriptions.stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(dtoList); // Возвращаем список DTO
    }

    // Отписка от артиста
    @DeleteMapping
    public ResponseEntity<Void> unsubscribeFromArtist(
            @RequestParam Long artistId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);

        try {
            artistSubscriptionService.unsubscribeFromArtist(userId, artistId);
            return ResponseEntity.ok().build(); // Успешное удаление
        } catch (IllegalStateException e) {
            // Если подписка не найдена, вернем ошибку с сообщением
            return ResponseEntity.status(400).body(null); // Код ошибки можно настроить по необходимости
        }
    }


    // Преобразование сущности ArtistSubscription в DTO
    private ArtistSubscriptionDTO toDTO(ArtistSubscription subscription) {
        return new ArtistSubscriptionDTO(
                subscription.getId(),
                subscription.getArtist().getId(),
                subscription.getArtist().getName(),
                subscription.getSubscribedAt(),
                subscription.getUser().getId(),
                subscription.getUser().getName()
        );
    }
}
