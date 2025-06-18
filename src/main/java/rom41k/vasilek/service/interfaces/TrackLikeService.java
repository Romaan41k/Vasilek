package rom41k.vasilek.service.interfaces;

import rom41k.vasilek.database.entity.TrackLike;

import java.util.List;

public interface TrackLikeService {
    TrackLike likeTrack(TrackLike trackLike);
    boolean isTrackLiked(Long userId, Long trackId);
    void unlikeTrack(Long userId, Long trackId);
    List<TrackLike> getUserLikes(Long userId);
}
