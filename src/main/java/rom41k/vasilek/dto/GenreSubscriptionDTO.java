package rom41k.vasilek.dto;

import java.time.LocalDateTime;

public record GenreSubscriptionDTO(
        Long id,
        String genre,
        LocalDateTime subscribedAt,
        Long userId,
        String username
) {}
