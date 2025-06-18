package rom41k.vasilek.service.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.TrackDTO;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface TrackService {

    record TrackDownloadData(Resource resource, String filename, long contentLength) {}

    TrackDTO createTrack(String title, String genre, MultipartFile trackFile, MultipartFile coverFile, User artist);

    TrackDTO updateTrack(Long trackId, String title, String genre, MultipartFile newCover, User currentUser);

    void deleteTrack(Long trackId, User currentUser);

    Optional<TrackDTO> findById(Long id);

    List<TrackDTO> findAll();

    List<TrackDTO> findByGenre(String genre);

    List<TrackDTO> findByArtistId(Long artistId);

    Optional<TrackDownloadData> getTrackForDownload(Long id) throws IOException;

    void incrementListens(Long trackId);

    void incrementLikes(Long trackId);
}