package rom41k.vasilek.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rom41k.vasilek.database.entity.Album;
import rom41k.vasilek.database.entity.ReleaseNotification;
import rom41k.vasilek.database.entity.Track;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.dto.AlbumResponseDTO;
import rom41k.vasilek.dto.ArtistDTO;
import rom41k.vasilek.dto.ReleaseNotificationDTO;
import rom41k.vasilek.dto.TrackDTO;
import rom41k.vasilek.repository.ReleaseNotificationRepository;
import rom41k.vasilek.service.interfaces.NotificationService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class NotificationController {

    private final ReleaseNotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @GetMapping("/new")
    public ResponseEntity<List<ReleaseNotificationDTO>> getNewReleases(@AuthenticationPrincipal User currentUser) {
        List<ReleaseNotification> notifications = notificationRepository
                .findByUserAndSeenIsFalseOrderByReleaseDateDesc(currentUser);

        List<ReleaseNotificationDTO> dtoList = notifications.stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/mark-as-seen/{notificationId}")
    public ResponseEntity<Void> markAsSeen(@PathVariable Long notificationId, @AuthenticationPrincipal User currentUser) {
        notificationService.markNotificationAsSeen(notificationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    private ReleaseNotificationDTO toDto(ReleaseNotification notification) {
        TrackDTO trackDto = (notification.getTrack() != null) ? mapTrackToDto(notification.getTrack()) : null;
        AlbumResponseDTO albumDto = (notification.getAlbum() != null) ? mapAlbumToDto(notification.getAlbum()) : null;

        return new ReleaseNotificationDTO(
                notification.getId(),
                trackDto,
                albumDto,
                notification.getReleaseDate()
        );
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

    private AlbumResponseDTO mapAlbumToDto(Album album) {
        AlbumResponseDTO dto = new AlbumResponseDTO();
        dto.setId(album.getId());
        dto.setName(album.getName());
        dto.setArtistId(album.getArtist().getId());
        dto.setArtistName(album.getArtist().getName());
        dto.setCreatedAt(album.getCreatedAt());
        dto.setTracks(Collections.emptyList());
        return dto;
    }
}