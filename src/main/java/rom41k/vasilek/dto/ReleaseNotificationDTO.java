package rom41k.vasilek.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseNotificationDTO {
    private Long id;
    private TrackDTO track;
    private AlbumResponseDTO album;
    private LocalDateTime releaseDate;
}