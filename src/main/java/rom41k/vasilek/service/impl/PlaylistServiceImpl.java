package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import rom41k.vasilek.database.entity.Playlist;
import rom41k.vasilek.database.entity.Track;
import rom41k.vasilek.repository.PlaylistRepository;
import rom41k.vasilek.repository.TrackRepository;
import rom41k.vasilek.service.interfaces.PlaylistService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaylistServiceImpl implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final TrackRepository trackRepository;

    @Override
    public Playlist createPlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    @Override
    public Optional<Playlist> updatePlaylist(Long id, Playlist playlist, Long userId) {
        return playlistRepository.findById(id)
                .filter(p -> p.getUser().getId().equals(userId))
                .map(existing -> {
                    existing.setName(playlist.getName());
                    return playlistRepository.save(existing);
                });
    }

    @Override
    public boolean deletePlaylist(Long id, Long userId) {
        Optional<Playlist> existingPlaylist = playlistRepository.findById(id);
        if (existingPlaylist.isPresent() && existingPlaylist.get().getUser().getId().equals(userId)) {
            playlistRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public Optional<Playlist> findById(Long id) {
        return playlistRepository.findById(id);
    }

    @Override
    public List<Playlist> findByUserId(Long userId) {
        return playlistRepository.findByUserId(userId);
    }

    @Override
    public Optional<Playlist> addTrackToPlaylist(Long playlistId, Long trackId, Long userId) {
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if (optionalPlaylist.isEmpty()) return Optional.empty();

        Playlist playlist = optionalPlaylist.get();

        if (!playlist.getUser().getId().equals(userId)) return Optional.empty();

        Optional<Track> optionalTrack = trackRepository.findById(trackId);
        if (optionalTrack.isEmpty()) return Optional.empty();

        Track track = optionalTrack.get();

        boolean alreadyExists = playlist.getTracks().stream()
                .anyMatch(t -> t.getId().equals(track.getId()));
        if (alreadyExists) {
            return Optional.empty();
        }

        playlist.getTracks().add(track);
        playlistRepository.save(playlist);

        return Optional.of(playlist);
    }

    @Override
    public Optional<Playlist> removeTrackFromPlaylist(Long playlistId, Long trackId, Long userId) {
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);
        if (optionalPlaylist.isEmpty()) return Optional.empty();

        Playlist playlist = optionalPlaylist.get();

        if (!playlist.getUser().getId().equals(userId)) return Optional.empty();

        Optional<Track> optionalTrack = trackRepository.findById(trackId);
        if (optionalTrack.isEmpty()) return Optional.empty();

        Track track = optionalTrack.get();

        boolean trackExists = playlist.getTracks().stream()
                .anyMatch(t -> t.getId().equals(track.getId()));

        if (!trackExists) {
            return Optional.empty();
        }

        playlist.getTracks().removeIf(t -> t.getId().equals(track.getId()));
        playlistRepository.save(playlist);

        return Optional.of(playlist);
    }
}
