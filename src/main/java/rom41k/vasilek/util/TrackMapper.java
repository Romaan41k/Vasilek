package rom41k.vasilek.util;

import org.springframework.stereotype.Component;
import rom41k.vasilek.database.entity.Track;
import rom41k.vasilek.dto.ArtistDTO;
import rom41k.vasilek.dto.TrackDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrackMapper {

    public TrackDTO toDto(Track track) {
        if (track == null) {
            return null;
        }
        return new TrackDTO(
                track.getId(),
                track.getTitle(),
                track.getGenre(),
                new ArtistDTO(track.getArtist().getId(), track.getArtist().getName()),
                track.getFilePath(),
                track.getCoverPath(),
                track.getLikesCount(),
                track.getListensCount(),
                track.getCreatedAt()
        );
    }

    public List<TrackDTO> toDtoList(List<Track> tracks) {
        return tracks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}