package rom41k.Rhythmix.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PlaylistResponseDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long userId;
    private String username;
    private List<TrackDTO> tracks;
}
