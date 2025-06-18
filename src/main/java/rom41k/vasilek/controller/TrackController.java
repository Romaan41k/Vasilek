package rom41k.vasilek.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.TrackDTO;
import rom41k.vasilek.service.interfaces.TrackService;
import rom41k.vasilek.service.interfaces.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;
    private final UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<TrackDTO> uploadTrack(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("genre") String genre,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            Authentication authentication
    ) {
        User currentUser = userService.getUserFromAuthentication(authentication);
        TrackDTO createdTrack = trackService.createTrack(title, genre, file, coverFile, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTrack);
    }

    @PatchMapping("/{id}/edit")
    public ResponseEntity<TrackDTO> editTrack(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(value = "cover", required = false) MultipartFile coverFile,
            Authentication authentication
    ) {
        User currentUser = userService.getUserFromAuthentication(authentication);
        try {
            TrackDTO updatedTrack = trackService.updateTrack(id, title, genre, coverFile, currentUser);
            return ResponseEntity.ok(updatedTrack);
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.getUserFromAuthentication(authentication);
        try {
            trackService.deleteTrack(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackDTO> getTrackById(@PathVariable Long id) {
        return trackService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrackDTO>> getAllTracks() {
        return ResponseEntity.ok(trackService.findAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<TrackDTO>> getMyTracks(Authentication authentication) {
        User currentUser = userService.getUserFromAuthentication(authentication);
        return ResponseEntity.ok(trackService.findByArtistId(currentUser.getId()));
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<List<TrackDTO>> getTracksByArtistId(@PathVariable Long artistId) {
        return ResponseEntity.ok(trackService.findByArtistId(artistId));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<TrackDTO>> getTracksByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(trackService.findByGenre(genre));
    }

    @PatchMapping("/{id}/listen")
    public ResponseEntity<Void> listenTrack(@PathVariable Long id) {
        trackService.incrementListens(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadTrack(@PathVariable Long id) throws IOException {
        var downloadDataOptional = trackService.getTrackForDownload(id);

        if (downloadDataOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var downloadData = downloadDataOptional.get();
        Resource resource = downloadData.resource();

        String contentType = Files.probeContentType(resource.getFile().toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadData.filename() + "\"")
                .contentLength(downloadData.contentLength())
                .body(resource);
    }
}