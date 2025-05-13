package rom41k.Rhythmix.repository;

import rom41k.Rhythmix.database.entity.GenreSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GenreSubscriptionRepository extends JpaRepository<GenreSubscription, Long> {
    List<GenreSubscription> findByUserId(Long userId);
    void deleteByUserIdAndGenre(Long userId, String genre);
    boolean existsByUserIdAndGenre(Long userId, String genre);
    Optional<GenreSubscription> findByUserIdAndGenre(Long userId, String genre);

}
