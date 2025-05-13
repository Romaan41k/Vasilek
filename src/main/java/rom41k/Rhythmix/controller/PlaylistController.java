package rom41k.Rhythmix.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import rom41k.Rhythmix.database.entity.Playlist;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.ArtistDTO;
import rom41k.Rhythmix.dto.PlaylistResponseDTO;
import rom41k.Rhythmix.dto.TrackDTO;
import rom41k.Rhythmix.service.interfaces.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;

    private Long getUserIdFromAuth(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }

    @PostMapping
    public ResponseEntity<PlaylistResponseDTO> createPlaylist(
            @RequestBody PlaylistResponseDTO playlistDto,
            Authentication authentication
    ) {
        User authUser = (User) authentication.getPrincipal();

        Playlist playlist = new Playlist();
        playlist.setName(playlistDto.getName());
        playlist.setUser(authUser);

        Playlist createdPlaylist = playlistService.createPlaylist(playlist);

        return ResponseEntity.ok(mapToDto(createdPlaylist));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> updatePlaylist(
            @PathVariable Long id,
            @RequestBody Playlist playlist,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        User user = new User();
        user.setId(userId);
        playlist.setUser(user);

        return playlistService.updatePlaylist(id, playlist, userId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        if (playlistService.deletePlaylist(id, userId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistResponseDTO> getPlaylistById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);

        return playlistService.findById(id)
                .filter(p -> p.getUser().getId().equals(userId))
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @GetMapping("/my")
    public ResponseEntity<List<PlaylistResponseDTO>> getMyPlaylists(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<Playlist> playlists = playlistService.findByUserId(userId);
        List<PlaylistResponseDTO> dtoList = playlists.stream().map(this::mapToDto).toList();
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/{playlistId}/add-track/{trackId}")
    public ResponseEntity<PlaylistResponseDTO> addTrack(@PathVariable Long playlistId, @PathVariable Long trackId, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return playlistService.addTrackToPlaylist(playlistId, trackId, userId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{playlistId}/remove-track/{trackId}")
    public ResponseEntity<PlaylistResponseDTO> removeTrack(@PathVariable Long playlistId, @PathVariable Long trackId, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return playlistService.removeTrackFromPlaylist(playlistId, trackId, userId)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private PlaylistResponseDTO mapToDto(Playlist playlist) {
        PlaylistResponseDTO dto = new PlaylistResponseDTO();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setUserId(playlist.getUser().getId());
        dto.setUsername(playlist.getUser().getName());

        if (playlist.getTracks() != null) {
            List<TrackDTO> trackDTOs = playlist.getTracks().stream()
                    .map(this::mapTrackToDto)
                    .toList();
            dto.setTracks(trackDTOs);
        }

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
