package rom41k.Rhythmix.repository;

import rom41k.Rhythmix.database.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(Long userId);
    Optional<Playlist> findByIdAndUserId(Long id, Long userId);
}
