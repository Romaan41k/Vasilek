package rom41k.vasilek.service.interfaces;

import rom41k.vasilek.database.entity.Album;
import rom41k.vasilek.database.entity.Track;

public interface NotificationService {
    void createNotificationsForNewTrack(Track track);
    void createNotificationsForNewAlbum(Album album);
    void markNotificationAsSeen(Long notificationId, Long userId);
}