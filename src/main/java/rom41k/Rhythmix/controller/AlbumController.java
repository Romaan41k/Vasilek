package rom41k.Rhythmix.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rom41k.Rhythmix.database.entity.Album;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.database.enums.Role;
import rom41k.Rhythmix.dto.AlbumResponseDTO;
import rom41k.Rhythmix.dto.ArtistDTO;
import rom41k.Rhythmix.dto.TrackDTO;
import rom41k.Rhythmix.service.interfaces.AlbumService;
import rom41k.Rhythmix.service.interfaces.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final UserService userService;

    public AlbumController(AlbumService albumService, UserService userService) {
        this.albumService = albumService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createAlbum(@RequestBody Album album, Authentication authentication) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            album.setArtist(currentUser);
            Album createdAlbum = albumService.createAlbum(album);
            AlbumResponseDTO albumResponseDTO = toAlbumResponseDTO(createdAlbum);
            return ResponseEntity.ok(albumResponseDTO);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("An album with this name already exists for this artist.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponseDTO> getAlbumById(@PathVariable Long id) {
        return albumService.findById(id)
                .map(album -> ResponseEntity.ok(toAlbumResponseDTO(album)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<?> getAlbumsByArtist(@PathVariable Long artistId) {
        Optional<User> userOptional = userService.getUserById(artistId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User artist = userOptional.get();
        if (!artist.getRole().equals(Role.ARTIST)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not an artist");
        }

        List<Album> albums = albumService.findByArtistId(artistId);
        List<AlbumResponseDTO> albumResponseDTOs = albums.stream()
                .map(this::toAlbumResponseDTO)
                .toList();

        return ResponseEntity.ok(albumResponseDTOs);
    }

    @PostMapping("/{albumId}/tracks/{trackId}")
    public ResponseEntity<AlbumResponseDTO> addTrackToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long trackId,
            Authentication authentication
    ) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        albumService.addTrackToAlbum(albumId, trackId, userId);
        Album album = albumService.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Альбом не найден"));
        AlbumResponseDTO albumResponseDTO = toAlbumResponseDTO(album);
        return ResponseEntity.ok(albumResponseDTO);
    }

    @DeleteMapping("/{albumId}/tracks/{trackId}")
    public ResponseEntity<AlbumResponseDTO> removeTrackFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long trackId,
            Authentication authentication
    ) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        albumService.removeTrackFromAlbum(albumId, trackId, userId);
        Album album = albumService.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Альбом не найден"));
        AlbumResponseDTO albumResponseDTO = toAlbumResponseDTO(album);
        return ResponseEntity.ok(albumResponseDTO);
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<?> deleteAlbum(
            @PathVariable Long albumId,
            Authentication authentication
    ) {
        Long userId = ((User) authentication.getPrincipal()).getId();
        Optional<Album> albumOptional = albumService.findById(albumId);
        if (albumOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Альбом не найден");
        }

        Album album = albumOptional.get();
        if (!album.getArtist().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не являетесь владельцем этого альбома");
        }

        albumService.deleteAlbum(albumId);
        return ResponseEntity.ok("Альбом успешно удалён");
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<?> updateAlbum(
            @PathVariable Long albumId,
            @RequestBody Album updatedAlbum,
            Authentication authentication
    ) {
        try {
            Long userId = ((User) authentication.getPrincipal()).getId();
            Album album = albumService.updateAlbum(albumId, updatedAlbum, userId);
            return ResponseEntity.ok(toAlbumResponseDTO(album));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private AlbumResponseDTO toAlbumResponseDTO(Album album) {
        AlbumResponseDTO dto = new AlbumResponseDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setCreatedAt(album.getCreatedAt());
        dto.setArtistId(album.getArtist().getId());
        dto.setArtistName(album.getArtist().getName());

        List<TrackDTO> trackDTOs = (album.getTracks() != null)
                ? album.getTracks().stream().map(this::mapTrackToDto).toList()
                : List.of();

        dto.setTracks(trackDTOs);
        return dto;
    }

    private TrackDTO mapTrackToDto(Track track) {
        return new TrackDTO(
                track.getId(),
                track.getTitle(),
                track.getGenre(),
                new ArtistDTO(track.getArtist().getId(), track.getArtist().getName()),
                track.getFilePath(),
                track.getCoverPath(),
                track.getLikesCount(),
                track.getListensCount(),
                track.getCreatedAt()
        );
    }
}
