package rom41k.vasilek.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rom41k.vasilek.database.entity.Track; // Импортируем Track
import rom41k.vasilek.database.entity.TrackLike; // Уже есть

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackLikeDTO {
    private Long id;
    private LocalDateTime likedAt;
    private Long trackId;
    private String trackTitle;
    private String trackArtistName;
    private String trackCoverPath;
    private String trackFilePath;
    private String trackGenre;
    private Integer trackLikesCount;
    private Integer trackListensCount;
    private LocalDateTime trackCreatedAt;

    public TrackLikeDTO(TrackLike trackLike) {
        this.id = trackLike.getId();
        this.likedAt = trackLike.getLikedAt();

        Track track = trackLike.getTrack();
        if (track != null) {
            this.trackId = track.getId();
            this.trackTitle = track.getTitle();
            this.trackCoverPath = track.getCoverPath();
            this.trackFilePath = track.getFilePath();
            this.trackGenre = track.getGenre();
            this.trackLikesCount = track.getLikesCount();
            this.trackListensCount = track.getListensCount();
            this.trackCreatedAt = track.getCreatedAt();

            if (track.getArtist() != null) {
                this.trackArtistName = track.getArtist().getName();
            } else {
                this.trackArtistName = "Неизвестный артист";
            }
        } else {
            this.trackTitle = "Трек удален или недоступен";
            this.trackArtistName = "";
        }
    }
}