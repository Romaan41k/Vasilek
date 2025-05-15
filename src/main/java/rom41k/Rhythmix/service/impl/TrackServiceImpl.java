package rom41k.Rhythmix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.repository.TrackRepository;
import rom41k.Rhythmix.service.interfaces.TrackService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;

    @Value("${app.upload.dir:D:/Projects/Rhythmix555/uploads}")
    private String uploadDir;

    @Override
    public Track uploadTrack(Track track) {
        return trackRepository.save(track);
    }

    @Override
    public List<Track> findAll() {
        return trackRepository.findAll();
    }

    @Override
    public Optional<Track> findById(Long id) {
        return trackRepository.findById(id);
    }

    @Override
    public List<Track> findByGenre(String genre) {
        return trackRepository.findByGenre(genre);
    }

    @Override
    public List<Track> findByArtistId(Long artistId) {
        return trackRepository.findByArtistId(artistId);
    }

    @Override
    public void deleteTrack(Long id) {
        trackRepository.deleteById(id);
    }

    @Override
    public void incrementLikes(Long trackId) {
        trackRepository.incrementLikes(trackId);
    }

    @Override
    public void incrementListens(Long trackId) {
        trackRepository.incrementListens(trackId);
    }

    @Override
    public void save(Track track) {
        trackRepository.save(track);
    }

    @Override
    public Optional<Path> getTrackFilePath(Long id) {
        return trackRepository.findById(id)
                .map(track -> {
                    String relativePath = track.getFilePath().replace("/uploads/", "");
                    return Paths.get(uploadDir, relativePath);
                });
    }

    @Override
    @Transactional
    public void updateTrack(Long trackId, String title, String genre, MultipartFile newCover, User artist) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Трек не найден"));

        if (!track.getArtist().getId().equals(artist.getId())) {
            throw new SecurityException("Вы не являетесь автором трека");
        }

        Duration duration = Duration.between(track.getCreatedAt(), LocalDateTime.now());
        if (duration.toMinutes() > 30 || track.getListensCount() > 0) {
            throw new RuntimeException("Редактирование доступно только в течение 30 минут после загрузки и до первого прослушивания");
        }

        boolean changed = false;

        if (title != null && !title.isBlank()) {
            track.setTitle(title);
            changed = true;
        }

        if (genre != null && !genre.isBlank()) {
            track.setGenre(genre);
            changed = true;
        }

        if (newCover != null && !newCover.isEmpty()) {
            String coverName = UUID.randomUUID() + "_" + newCover.getOriginalFilename();
            Path coverPath = Paths.get(uploadDir, "covers", coverName);
            File coverDirFile = coverPath.getParent().toFile();

            if (!coverDirFile.exists()) {
                coverDirFile.mkdirs();
            }

            try {
                newCover.transferTo(coverPath);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка при загрузке файла обложки", e);
            }

            track.setCoverPath("/uploads/covers/" + coverName);
            trackRepository.save(track);
            changed = true;
        }

        if (changed) {
            trackRepository.save(track);
        }
    }
}
