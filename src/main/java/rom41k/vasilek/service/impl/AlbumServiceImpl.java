package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rom41k.vasilek.database.entity.Album;
import rom41k.vasilek.database.entity.Track;
import rom41k.vasilek.repository.AlbumRepository;
import rom41k.vasilek.repository.TrackRepository;
import rom41k.vasilek.service.interfaces.AlbumService;
import rom41k.vasilek.service.interfaces.NotificationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public Album createAlbum(Album album) {
        Optional<Album> existingAlbum = albumRepository.findByNameAndArtistId(album.getName(), album.getArtist().getId());

        if (existingAlbum.isPresent()) {
            throw new DataIntegrityViolationException("An album with this name already exists for this artist.");
        }
        Album savedAlbum = albumRepository.save(album);
        notificationService.createNotificationsForNewAlbum(savedAlbum);
        return albumRepository.save(album);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Album> findAll() {
        return albumRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Album> findById(Long id) {
        return albumRepository.findById(id);
    }

    @Transactional(readOnly = true)
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

        if (!album.getArtist().getId().equals(userId)) {
            throw new SecurityException("Вы не являетесь владельцем этого альбома");
        }

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Трек не найден"));

        if (album.getTracks().stream().anyMatch(t -> t.getId().equals(trackId))) {
            throw new IllegalArgumentException("Этот трек уже есть в альбоме");
        }

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

        Optional<Album> albumWithSameName = albumRepository.findByNameAndArtistId(updatedAlbum.getName(), userId);
        if (albumWithSameName.isPresent() && !albumWithSameName.get().getId().equals(albumId)) {
            throw new DataIntegrityViolationException("Альбом с таким названием уже существует у этого исполнителя");
        }

        if (Duration.between(existingAlbum.getCreatedAt(), LocalDateTime.now()).toMinutes() > 30) {
            throw new IllegalStateException("Редактирование альбома доступно только в течение 30 минут после создания");
        }

        existingAlbum.setName(updatedAlbum.getName());
        return albumRepository.save(existingAlbum);
    }
}
