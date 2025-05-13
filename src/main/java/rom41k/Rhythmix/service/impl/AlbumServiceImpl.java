package rom41k.Rhythmix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rom41k.Rhythmix.database.entity.Album;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.repository.AlbumRepository;
import rom41k.Rhythmix.repository.TrackRepository;
import rom41k.Rhythmix.service.interfaces.AlbumService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    @Override
    public Album createAlbum(Album album) {
        // Проверяем, существует ли альбом с таким же названием у артиста
        Optional<Album> existingAlbum = albumRepository.findByNameAndArtistId(album.getName(), album.getArtist().getId());

        if (existingAlbum.isPresent()) {
            throw new DataIntegrityViolationException("An album with this name already exists for this artist.");
        }
        return albumRepository.save(album);
    }

    @Override
    public Optional<Album> findById(Long id) {
        return albumRepository.findById(id);
    }

    @Override
    public List<Album> findByArtistId(Long artistId) {
        return albumRepository.findByArtistId(artistId);
    }

    @Override
    public void deleteAlbum(Long id) {
        albumRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addTrackToAlbum(Long albumId, Long trackId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Альбом не найден"));

        // Проверяем, что пользователь является владельцем альбома
        if (!album.getArtist().getId().equals(userId)) {
            throw new SecurityException("Вы не являетесь владельцем этого альбома");
        }

        // Ищем трек в базе
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Трек не найден"));

        // Проверяем, что трек еще не добавлен в альбом
        if (album.getTracks().stream().anyMatch(t -> t.getId().equals(trackId))) {
            throw new IllegalArgumentException("Этот трек уже есть в альбоме");
        }

        // Добавляем трек в альбом
        album.getTracks().add(track);
        albumRepository.save(album);
    }


    @Override
    @Transactional
    public void removeTrackFromAlbum(Long albumId, Long trackId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Альбом не найден"));

        if (!album.getArtist().getId().equals(userId)) {
            throw new SecurityException("Вы не являетесь владельцем этого альбома");
        }

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Трек не найден"));

        if (!album.getTracks().contains(track)) {
            throw new IllegalArgumentException("Трек не найден в альбоме");
        }

        album.getTracks().remove(track);
        albumRepository.save(album);

    }

    @Override
    @Transactional
    public Album updateAlbum(Long albumId, Album updatedAlbum, Long userId) {
        Album existingAlbum = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Альбом не найден"));

        if (!existingAlbum.getArtist().getId().equals(userId)) {
            throw new SecurityException("Вы не являетесь владельцем этого альбома");
        }

        // Проверка на дубликат названия (если меняется имя)
        Optional<Album> albumWithSameName = albumRepository.findByNameAndArtistId(updatedAlbum.getName(), userId);
        if (albumWithSameName.isPresent() && !albumWithSameName.get().getId().equals(albumId)) {
            throw new DataIntegrityViolationException("Альбом с таким названием уже существует у этого исполнителя");
        }

        // ⏱ Ограничение: редактировать можно только в течение 30 минут после создания
        if (Duration.between(existingAlbum.getCreatedAt(), LocalDateTime.now()).toMinutes() > 30) {
            throw new IllegalStateException("Редактирование альбома доступно только в течение 30 минут после создания");
        }

        existingAlbum.setName(updatedAlbum.getName());
        return albumRepository.save(existingAlbum);
    }


}
