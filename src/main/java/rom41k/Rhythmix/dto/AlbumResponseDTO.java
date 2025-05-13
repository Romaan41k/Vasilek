package rom41k.Rhythmix.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AlbumResponseDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long artistId;
    private String artistName;
    private List<TrackDTO> tracks;
}
