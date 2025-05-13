package rom41k.Rhythmix.service.impl;

import lombok.RequiredArgsConstructor;
import rom41k.Rhythmix.database.entity.Playlist;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.repository.PlaylistRepository;
import rom41k.Rhythmix.repository.TrackRepository;
import rom41k.Rhythmix.service.interfaces.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
                    existing.setName(playlist.getName()); // изменяем только имя
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
        return false; // Плейлист не найден или не принадлежит пользователю
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

        if (optionalPlaylist.isEmpty()) return Optional.empty();  // Плейлист не найден

        Playlist playlist = optionalPlaylist.get();

        if (!playlist.getUser().getId().equals(userId)) return Optional.empty();  // Плейлист не принадлежит текущему пользователю

        Optional<Track> optionalTrack = trackRepository.findById(trackId);
        if (optionalTrack.isEmpty()) return Optional.empty();  // Трек не найден

        Track track = optionalTrack.get();

        // Проверка: если трек уже есть в плейлисте — возвращаем ошибку
        boolean alreadyExists = playlist.getTracks().stream()
                .anyMatch(t -> t.getId().equals(track.getId()));
        if (alreadyExists) {
            return Optional.empty();  // Возвращаем пустой результат, чтобы в контроллере можно было обработать ошибку
        }

        // Добавляем трек в плейлист
        playlist.getTracks().add(track);
        playlistRepository.save(playlist);

        return Optional.of(playlist);
    }


    @Override
    public Optional<Playlist> removeTrackFromPlaylist(Long playlistId, Long trackId, Long userId) {
        Optional<Playlist> optionalPlaylist = playlistRepository.findById(playlistId);

        if (optionalPlaylist.isEmpty()) return Optional.empty();  // Плейлист не найден

        Playlist playlist = optionalPlaylist.get();

        if (!playlist.getUser().getId().equals(userId)) return Optional.empty();  // Плейлист не принадлежит текущему пользователю

        Optional<Track> optionalTrack = trackRepository.findById(trackId);
        if (optionalTrack.isEmpty()) return Optional.empty();  // Трек не найден

        Track track = optionalTrack.get();

        // Проверка: если трек не существует в плейлисте — возвращаем ошибку
        boolean trackExists = playlist.getTracks().stream()
                .anyMatch(t -> t.getId().equals(track.getId()));

        if (!trackExists) {
            return Optional.empty();  // Возвращаем пустой результат, если трек не найден в плейлисте
        }

        // Удаляем трек из плейлиста
        playlist.getTracks().removeIf(t -> t.getId().equals(track.getId()));

        // Сохраняем изменения в базе данных
        playlistRepository.save(playlist);

        return Optional.of(playlist);  // Возвращаем обновлённый плейлист
    }

}
