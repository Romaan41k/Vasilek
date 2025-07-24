package rom41k.vasilek.service.interfaces;

import rom41k.vasilek.database.entity.Album;
import rom41k.vasilek.database.entity.User;

import java.util.List;
import java.util.Optional;

public interface AlbumService {
    Album createAlbum(Album album);
    Optional<Album> findById(Long id);
    List<Album> findByArtistId(Long artistId);
    void deleteAlbum(Long albumId, User currentUser);
    void addTrackToAlbum(Long albumId, Long trackId, Long userId);
    void removeTrackFromAlbum(Long albumId, Long trackId, Long userId);
    Album updateAlbum(Long albumId, Album updatedAlbum, Long userId);
    List<Album> findAll();
}