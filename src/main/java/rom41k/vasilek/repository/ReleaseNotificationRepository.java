package rom41k.vasilek.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rom41k.vasilek.database.entity.ReleaseNotification;
import rom41k.vasilek.database.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseNotificationRepository extends JpaRepository<ReleaseNotification, Long> {
    List<ReleaseNotification> findByUserAndSeenIsFalseOrderByReleaseDateDesc(User user);
    Optional<ReleaseNotification> findByIdAndUserId(Long id, Long userId);
    void deleteByTrackId(Long trackId);
    void deleteByAlbumId(Long albumId);
}