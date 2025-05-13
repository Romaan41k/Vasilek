package rom41k.Rhythmix.dto;

import java.time.LocalDateTime;

public record GenreSubscriptionDTO(
        Long id,
        String genre,
        LocalDateTime subscribedAt,
        Long userId,
        String username
) {}
