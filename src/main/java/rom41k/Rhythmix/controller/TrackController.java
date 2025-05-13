package rom41k.Rhythmix.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource; // Правильный импорт для Resource
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException; // Импорт для ResponseStatusException
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.ArtistDTO;
import rom41k.Rhythmix.dto.TrackDTO;
import rom41k.Rhythmix.repository.UserRepository;
import rom41k.Rhythmix.service.interfaces.TrackService; // Убран FilebaseService, так как не используется в предоставленном коде
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException; // Импорт для MalformedURLException
import java.nio.file.Files; // Импорт для Files
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tracks")
public class TrackController {

    @Autowired
    private TrackService trackService;

    @Autowired
    private UserRepository userRepository;

    // Убран @Autowired private FilebaseService filebaseService; так как он не используется в предоставленном коде

    @PostMapping("/upload")
    public ResponseEntity<String> uploadTrack(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("genre") String genre,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile
    ) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body("Не авторизован");
        }

        String email = authentication.getName();
        User artist = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String uploadDir = "uploads/tracks/";
        String coverDir = "uploads/covers/";

        new File(uploadDir).mkdirs();
        new File(coverDir).mkdirs();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        file.transferTo(filePath);

        String coverName = "default-cover.png";
        if (coverFile != null && !coverFile.isEmpty()) {
            coverName = UUID.randomUUID() + "_" + coverFile.getOriginalFilename();
            Path coverPath = Paths.get(coverDir + coverName);
            coverFile.transferTo(coverPath);
        }

        Track track = new Track();
        track.setTitle(title);
        track.setGenre(genre);
        track.setArtist(artist);
        track.setFilePath("/uploads/tracks/" + fileName);
        track.setCoverPath("/uploads/covers/" + coverName);
        track.setLikesCount(0);
        track.setListensCount(0);
        trackService.save(track);

        return ResponseEntity.ok("Трек загружен");
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackDTO> getTrackById(@PathVariable Long id) {
        return trackService.findById(id)
                .map(track -> new TrackDTO(
                        track.getId(),
                        track.getTitle(),
                        track.getGenre(),
                        new ArtistDTO(track.getArtist().getId(), track.getArtist().getName()),
                        track.getFilePath(),
                        track.getCoverPath(),
                        track.getLikesCount(),
                        track.getListensCount(),
                        track.getCreatedAt()
                ))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<TrackDTO>> getTracksByGenre(@PathVariable String genre) {
        List<Track> tracks = trackService.findByGenre(genre);

        List<TrackDTO> trackDTOs = tracks.stream()
                .map(track -> new TrackDTO(
                        track.getId(),
                        track.getTitle(),
                        track.getGenre(),
                        new ArtistDTO(track.getArtist().getId(), track.getArtist().getName()),
                        track.getFilePath(),
                        track.getCoverPath(),
                        track.getLikesCount(),
                        track.getListensCount(),
                        track.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(trackDTOs);
    }

    @PatchMapping("/{id}/listen")
    public ResponseEntity<Void> listenTrack(@PathVariable Long id) {
        trackService.incrementListens(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrackDTO>> getAllTracks() {
        List<Track> tracks = trackService.findAll();

        List<TrackDTO> trackDTOs = tracks.stream()
                .map(track -> new TrackDTO(
                        track.getId(),
                        track.getTitle(),
                        track.getGenre(),
                        new ArtistDTO(track.getArtist().getId(), track.getArtist().getName()),
                        track.getFilePath(),
                        track.getCoverPath(),
                        track.getLikesCount(),
                        track.getListensCount(),
                        track.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(trackDTOs);
    }


    @GetMapping("/my")
    public ResponseEntity<List<TrackDTO>> getMyTracks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Track> tracks = trackService.findByArtistId(currentUser.getId());

        List<TrackDTO> trackDTOs = tracks.stream()
                .map(track -> new TrackDTO(
                        track.getId(),
                        track.getTitle(),
                        track.getGenre(),
                        new ArtistDTO(track.getArtist().getId(), track.getArtist().getName()),
                        track.getFilePath(),
                        track.getCoverPath(),
                        track.getLikesCount(),
                        track.getListensCount(),
                        track.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(trackDTOs);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadTrack(@PathVariable Long id) {
        try {
            Optional<Path> filePathOptional = trackService.getTrackFilePath(id);

            if (filePathOptional.isEmpty()) {
                // Трек или файл не найден
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Track or file not found");
            }

            Path filePath = filePathOptional.get();
            Resource resource;
            try {
                resource = new UrlResource(filePath.toUri());
            } catch (MalformedURLException e) {
                // Ошибка при создании URL ресурса
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating file resource URL", e);
            }


            // Проверяем, существует ли файл и доступен ли для чтения
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found or not readable");
            }

            // Определяем тип контента
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream"; // Тип по умолчанию, если не удалось определить
            }

            // Определяем имя файла для скачивания
            String filename = resource.getFilename();

            // Устанавливаем заголовки ответа
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            try {
                headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(resource.contentLength()));
            } catch (IOException e) {
                // Ошибка при получении длины файла
                // Можно проигнорировать или вернуть ошибку, в зависимости от требований
            }


            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (ResponseStatusException ex) {
            // Перебрасываем уже созданные ResponseStatusException
            throw ex;
        } catch (Exception e) {
            // Любые другие непредвиденные ошибки
            log.error("Error during track download", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while downloading the track", e);
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).build();
            }

            String email = auth.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            Track track = trackService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Трек не найден"));

            if (!track.getArtist().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }

            trackService.deleteTrack(id);
            return ResponseEntity.ok().build();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/edit")
    public ResponseEntity<String> editTrack(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Не авторизован");
        }

        String email = auth.getName();
        User artist = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        try {
            trackService.updateTrack(id, title, genre, coverFile, artist);
            return ResponseEntity.ok("Трек успешно обновлён");
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("Вы не можете редактировать этот трек");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при редактировании трека");
        }
    }

}