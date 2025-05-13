package rom41k.Rhythmix.repository;

import rom41k.Rhythmix.database.entity.TrackLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackLikeRepository extends JpaRepository<TrackLike, Long> {
    boolean existsByUserIdAndTrackId(Long userId, Long trackId);
    void deleteByUserIdAndTrackId(Long userId, Long trackId);
    Optional<TrackLike> findByUserIdAndTrackId(Long userId, Long trackId);
}
