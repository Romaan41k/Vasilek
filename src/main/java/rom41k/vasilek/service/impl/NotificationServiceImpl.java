package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rom41k.vasilek.database.entity.*;
import rom41k.vasilek.repository.ArtistSubscriptionRepository;
import rom41k.vasilek.repository.ReleaseNotificationRepository;
import rom41k.vasilek.service.interfaces.NotificationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ArtistSubscriptionRepository artistSubscriptionRepository;
    private final ReleaseNotificationRepository notificationRepository;

    @Async
    @Transactional
    @Override
    public void createNotificationsForNewTrack(Track track) {
        List<ArtistSubscription> subscriptions = artistSubscriptionRepository.findByArtistId(track.getArtist().getId());

        List<ReleaseNotification> notifications = subscriptions.stream().map(sub -> {
            ReleaseNotification notification = new ReleaseNotification();
            notification.setUser(sub.getUser());
            notification.setTrack(track);
            notification.setAlbum(null);
            notification.setReleaseDate(track.getCreatedAt());
            notification.setSeen(false);
            return notification;
        }).toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Async
    @Transactional
    @Override
    public void createNotificationsForNewAlbum(Album album) {
        List<ArtistSubscription> subscriptions = artistSubscriptionRepository.findByArtistId(album.getArtist().getId());

        List<ReleaseNotification> notifications = subscriptions.stream().map(sub -> {
            ReleaseNotification notification = new ReleaseNotification();
            notification.setUser(sub.getUser());
            notification.setTrack(null);
            notification.setAlbum(album);
            notification.setReleaseDate(album.getCreatedAt());
            notification.setSeen(false);
            return notification;
        }).toList();

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    @Override
    @Transactional
    public void markNotificationAsSeen(Long notificationId, Long userId) {
        ReleaseNotification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new SecurityException("Notification not found or access denied."));

        notification.setSeen(true);
        notificationRepository.save(notification);
    }
}