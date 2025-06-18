package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rom41k.vasilek.database.entity.Track;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.TrackDTO;
import rom41k.vasilek.repository.ReleaseNotificationRepository;
import rom41k.vasilek.util.TrackMapper;
import rom41k.vasilek.repository.TrackRepository;
import rom41k.vasilek.service.interfaces.NotificationService;
import rom41k.vasilek.service.interfaces.TrackService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final NotificationService notificationService;
    private final TrackMapper trackMapper;
    private final ReleaseNotificationRepository notificationRepository;

    @Value("${app.upload.dir:uploads}")
    private String baseUploadDir;

    private final String baseUploadsUri = "/uploads/";
    private final String defaultCoverPath = "/uploads/covers/default-cover.png";

    @Override
    @Transactional
    public TrackDTO createTrack(String title, String genre, MultipartFile trackFile, MultipartFile coverFile, User artist) {
        String trackPath = storeFile(trackFile, "tracks");
        String coverPath = storeFile(coverFile, "covers");
        if (coverPath == null) {
            coverPath = defaultCoverPath;
        }

        Track track = new Track();
        track.setTitle(title);
        track.setGenre(genre);
        track.setArtist(artist);
        track.setFilePath(trackPath);
        track.setCoverPath(coverPath);
        track.setLikesCount(0);
        track.setListensCount(0);

        Track savedTrack = trackRepository.save(track);
        notificationService.createNotificationsForNewTrack(savedTrack);

        return trackMapper.toDto(savedTrack);
    }

    @Override
    @Transactional
    public TrackDTO updateTrack(Long trackId, String title, String genre, MultipartFile newCoverFile, User currentUser) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Трек не найден с ID: " + trackId));

        if (!track.getArtist().getId().equals(currentUser.getId())) {
            throw new SecurityException("Вы не являетесь автором этого трека.");
        }

        Duration durationSinceCreation = Duration.between(track.getCreatedAt(), LocalDateTime.now());
        if (durationSinceCreation.toMinutes() > 30) {
            throw new IllegalStateException("Редактирование доступно только в течение 30 минут после создания.");
        }

        if (title != null && !title.isBlank()) {
            track.setTitle(title);
        }
        if (genre != null && !genre.isBlank()) {
            track.setGenre(genre);
        }
        if (newCoverFile != null && !newCoverFile.isEmpty()) {
            deleteFile(track.getCoverPath());
            String newCoverPath = storeFile(newCoverFile, "covers");
            track.setCoverPath(newCoverPath);
        }

        Track updatedTrack = trackRepository.save(track);
        return trackMapper.toDto(updatedTrack);
    }

    @Override
    @Transactional
    public void deleteTrack(Long trackId, User currentUser) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Трек для удаления не найден: " + trackId));

        if (!track.getArtist().getId().equals(currentUser.getId())) {
            throw new SecurityException("Вы не можете удалить этот трек.");
        }

        notificationRepository.deleteByTrackId(trackId);
        deleteFile(track.getFilePath());
        deleteFile(track.getCoverPath());
        trackRepository.deleteById(trackId);
    }

    @Override
    public Optional<TrackDTO> findById(Long id) {
        return trackRepository.findById(id).map(trackMapper::toDto);
    }

    @Override
    public List<TrackDTO> findAll() {
        return trackMapper.toDtoList(trackRepository.findAll());
    }

    @Override
    public List<TrackDTO> findByGenre(String genre) {
        return trackMapper.toDtoList(trackRepository.findByGenre(genre));
    }

    @Override
    public List<TrackDTO> findByArtistId(Long artistId) {
        return trackMapper.toDtoList(trackRepository.findByArtistId(artistId));
    }

    @Override
    public Optional<TrackDownloadData> getTrackForDownload(Long id) throws IOException {
        Optional<Track> trackOptional = trackRepository.findById(id);
        if (trackOptional.isEmpty()) {
            return Optional.empty();
        }

        Track track = trackOptional.get();
        Path filePath = getAbsolutePath(track.getFilePath());
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            log.error("MalformedURLException для path: {}", filePath, e);
            return Optional.empty();
        }

        if (!resource.exists() || !resource.isReadable()) {
            log.warn("Resource not found or not readable at path: {}", filePath);
            return Optional.empty();
        }

        String originalFilename = sanitizeFilename(track.getTitle()) + getFileExtension(track.getFilePath());
        return Optional.of(new TrackDownloadData(resource, originalFilename, resource.contentLength()));
    }

    @Override
    public void incrementListens(Long trackId) {
        trackRepository.incrementListens(trackId);
    }

    @Override
    public void incrementLikes(Long trackId) {
        trackRepository.incrementLikes(trackId);
    }

    private String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());
        String filenameWithUUID = UUID.randomUUID().toString() + "_" + sanitizedFilename;

        try {
            Path fileStorageLocation = Paths.get(baseUploadDir).toAbsolutePath().normalize();
            Path targetLocation = fileStorageLocation.resolve(subDirectory).resolve(filenameWithUUID);
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return baseUploadsUri + subDirectory + "/" + filenameWithUUID;
        } catch (IOException ex) {
            log.error("Не удалось сохранить файл {}", filenameWithUUID, ex);
            throw new RuntimeException("Не удалось сохранить файл " + filenameWithUUID, ex);
        }
    }

    private void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || relativePath.equals(defaultCoverPath)) {
            return;
        }
        try {
            Path filePath = getAbsolutePath(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.error("Не удалось удалить файл: {}", relativePath, ex);
        }
    }

    private Path getAbsolutePath(String relativePath) {
        String pathOnDisk = relativePath.replaceFirst("^" + baseUploadsUri, "");
        return Paths.get(this.baseUploadDir).resolve(pathOnDisk).normalize();
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unknown_file";
        }
        return filename.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9.\\-_а-яА-ЯёЁ]", "_")
                .replaceAll("_+", "_")
                .replaceAll("\\.+", ".");
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return "";
    }
}