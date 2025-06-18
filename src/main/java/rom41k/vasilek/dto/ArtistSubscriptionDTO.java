package rom41k.vasilek.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ArtistSubscriptionDTO {
    private Long id;
    private Long artistId;
    private String artistName;
    private LocalDateTime subscribedAt;
    private Long userId;
    private String userName;

    public ArtistSubscriptionDTO(Long id, Long artistId, String artistName, LocalDateTime subscribedAt, Long userId, String userName) {
        this.id = id;
        this.artistId = artistId;
        this.artistName = artistName;
        this.subscribedAt = subscribedAt;
        this.userId = userId;
        this.userName = userName;
    }
}
