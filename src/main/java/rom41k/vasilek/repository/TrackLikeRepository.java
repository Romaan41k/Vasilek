package rom41k.vasilek.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import rom41k.vasilek.database.entity.TrackLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackLikeRepository extends JpaRepository<TrackLike, Long> {
    boolean existsByUserIdAndTrackId(Long userId, Long trackId);
    void deleteByUserIdAndTrackId(Long userId, Long trackId);
    Optional<TrackLike> findByUserIdAndTrackId(Long userId, Long trackId);
    @EntityGraph(attributePaths = {"track", "track.artist"})
    List<TrackLike> findByUserId(Long userId);
}
