package rom41k.Rhythmix.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import rom41k.Rhythmix.database.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {
    List<Track> findByGenre(String genre);
    List<Track> findByArtistId(Long artistId);
    List<Track> findAll();

    @Transactional
    @Modifying
    @Query("UPDATE Track t SET t.likesCount = t.likesCount + 1 WHERE t.id = :trackId")
    void incrementLikes(Long trackId);

    @Transactional
    @Modifying
    @Query("UPDATE Track t SET t.listensCount = t.listensCount + 1 WHERE t.id = :trackId")
    void incrementListens(Long trackId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Track t WHERE t.id = :id")
    Optional<Track> findByIdWithLock(@Param("id") Long id);
}
