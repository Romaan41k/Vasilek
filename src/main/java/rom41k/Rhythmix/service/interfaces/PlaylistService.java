package rom41k.Rhythmix.service.interfaces;

import rom41k.Rhythmix.database.entity.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistService {
    Playlist createPlaylist(Playlist playlist);
    Optional<Playlist> updatePlaylist(Long id, Playlist playlist, Long userId);
    boolean deletePlaylist(Long id, Long userId);
    Optional<Playlist> findById(Long id);
    List<Playlist> findByUserId(Long userId);
    Optional<Playlist> addTrackToPlaylist(Long playlistId, Long trackId, Long userId);
    Optional<Playlist> removeTrackFromPlaylist(Long playlistId, Long trackId, Long userId);
}
