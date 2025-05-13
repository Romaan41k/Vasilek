package rom41k.Rhythmix.dto;

import java.time.LocalDateTime;

public record TrackDTO(
        Long id,
        String title,
        String genre,
        ArtistDTO artist,
        String filePath,
        String coverPath,
        int likesCount,
        int listensCount,
        LocalDateTime createdAt
) {}
