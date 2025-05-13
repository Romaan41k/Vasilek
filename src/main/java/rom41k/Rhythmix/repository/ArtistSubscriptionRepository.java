package rom41k.Rhythmix.repository;

import rom41k.Rhythmix.database.entity.ArtistSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistSubscriptionRepository extends JpaRepository<ArtistSubscription, Long> {
    List<ArtistSubscription> findByUserId(Long userId);
    boolean existsByUserIdAndArtistId(Long userId, Long artistId);
    Optional<ArtistSubscription> findByUserIdAndArtistId(Long userId, Long artistId);
    void deleteByUserIdAndArtistId(Long userId, Long artistId);
}
