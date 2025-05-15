package rom41k.Rhythmix.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.User;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface TrackService {
    Track uploadTrack(Track track);
    Optional<Track> findById(Long id);
    List<Track> findByGenre(String genre);
    List<Track> findByArtistId(Long artistId);
    void deleteTrack(Long id);
    void incrementLikes(Long trackId);
    void incrementListens(Long trackId);
    void save(Track track);
    List<Track> findAll();
    Optional<Path> getTrackFilePath(Long id);
    void updateTrack(Long trackId, String title, String genre, MultipartFile newCover, User artist);
}
