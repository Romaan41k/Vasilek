package rom41k.Rhythmix.repository;

import rom41k.Rhythmix.database.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByArtistId(Long artistId);
    Optional<Album> findByNameAndArtistId(String name, Long artistId);

}
