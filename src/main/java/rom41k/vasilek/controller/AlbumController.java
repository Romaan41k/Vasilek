package rom41k.vasilek.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rom41k.vasilek.database.entity.Album;
import rom41k.vasilek.database.entity.Track;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.database.enums.Role;
import rom41k.vasilek.dto.AlbumResponseDTO;
import rom41k.vasilek.dto.ArtistDTO;
import rom41k.vasilek.dto.TrackDTO;
import rom41k.vasilek.service.interfaces.AlbumService;
import rom41k.vasilek.service.interfaces.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<AlbumResponseDTO> createAlbum(@RequestBody Album album, @AuthenticationPrincipal User currentUser) {
        album.setArtist(currentUser);
        Album createdAlbum = albumService.createAlbum(album);
        return ResponseEntity.ok(toAlbumResponseDTO(createdAlbum));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AlbumResponseDTO>> getAllAlbums() {
        List<Album> albums = albumService.findAll();
        List<AlbumResponseDTO> albumDtos = albums.stream()
                .map(this::toAlbumResponseDTO)
                .toList();
        return ResponseEntity.ok(albumDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumResponseDTO> getAlbumById(@PathVariable Long id) {
        return albumService.findById(id)
                .map(album -> ResponseEntity.ok(toAlbumResponseDTO(album)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/artist/{artistId}")
    public ResponseEntity<?> getAlbumsByArtist(@PathVariable Long artistId) {
        User artist = userService.getUserById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (artist.getRole() != Role.ARTIST) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not an artist");
        }

        List<Album> albums = albumService.findByArtistId(artistId);
        List<AlbumResponseDTO> albumResponseDTOs = albums.stream()
                .map(this::toAlbumResponseDTO)
                .toList();
        return ResponseEntity.ok(albumResponseDTOs);
    }

    @PostMapping("/{albumId}/tracks/{trackId}")
    public ResponseEntity<AlbumResponseDTO> addTrackToAlbum(@PathVariable Long albumId,
                                                            @PathVariable Long trackId,
                                                            @AuthenticationPrincipal User currentUser) {
        albumService.addTrackToAlbum(albumId, trackId, currentUser.getId());
        Album album = albumService.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));
        return ResponseEntity.ok(toAlbumResponseDTO(album));
    }

    @DeleteMapping("/{albumId}/tracks/{trackId}")
    public ResponseEntity<AlbumResponseDTO> removeTrackFromAlbum(@PathVariable Long albumId,
                                                                 @PathVariable Long trackId,
                                                                 @AuthenticationPrincipal User currentUser) {
        albumService.removeTrackFromAlbum(albumId, trackId, currentUser.getId());
        Album album = albumService.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));
        return ResponseEntity.ok(toAlbumResponseDTO(album));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<String> deleteAlbum(@PathVariable Long albumId, @AuthenticationPrincipal User currentUser) {
        albumService.deleteAlbum(albumId, currentUser);
        return ResponseEntity.ok("Album successfully deleted");
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<AlbumResponseDTO> updateAlbum(@PathVariable Long albumId,
                                                        @RequestBody Album updatedAlbum,
                                                        @AuthenticationPrincipal User currentUser) {
        Album album = albumService.updateAlbum(albumId, updatedAlbum, currentUser.getId());
        return ResponseEntity.ok(toAlbumResponseDTO(album));
    }

    private AlbumResponseDTO toAlbumResponseDTO(Album album) {
        AlbumResponseDTO dto = new AlbumResponseDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setCreatedAt(album.getCreatedAt());

        if (album.getArtist() != null) {
            dto.setArtistId(album.getArtist().getId());
            dto.setArtistName(album.getArtist().getName());
        }

        List<TrackDTO> trackDTOs = (album.getTracks() != null)
                ? album.getTracks().stream().map(this::mapTrackToDto).toList()
                : List.of();

        dto.setTracks(trackDTOs);
        return dto;
    }

    private TrackDTO mapTrackToDto(Track track) {
        ArtistDTO artistDTO = (track.getArtist() != null)
                ? new ArtistDTO(track.getArtist().getId(), track.getArtist().getName())
                : null;

        return new TrackDTO(
                track.getId(),
                track.getTitle(),
                track.getGenre(),
                artistDTO,
                track.getFilePath(),
                track.getCoverPath(),
                track.getLikesCount(),
                track.getListensCount(),
                track.getCreatedAt()
        );
    }
}