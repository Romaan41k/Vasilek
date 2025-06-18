package rom41k.vasilek.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import rom41k.vasilek.database.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    @EntityGraph(attributePaths = {"tracks", "artist"})
    List<Album> findByArtistId(Long artistId);
    Optional<Album> findByNameAndArtistId(String name, Long artistId);
}
