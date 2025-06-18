package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import rom41k.vasilek.database.entity.Track;
import rom41k.vasilek.database.entity.TrackLike;
import rom41k.vasilek.repository.TrackLikeRepository;
import rom41k.vasilek.repository.TrackRepository;
import rom41k.vasilek.service.interfaces.TrackLikeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackLikeServiceImpl implements TrackLikeService {

    private final TrackLikeRepository trackLikeRepository;
    private final TrackRepository trackRepository;

    @Override
    @Transactional
    public TrackLike likeTrack(TrackLike trackLike) {
        Track track = trackRepository.findById(trackLike.getTrack().getId())
                .orElseThrow(() -> new RuntimeException("Track not found"));

        track.setLikesCount(track.getLikesCount() + 1);
        trackRepository.save(track);

        return trackLikeRepository.save(trackLike);
    }

    @Override
    public boolean isTrackLiked(Long userId, Long trackId) {
        return trackLikeRepository.existsByUserIdAndTrackId(userId, trackId);
    }

    @Override
    @Transactional
    public void unlikeTrack(Long userId, Long trackId) {
        TrackLike like = trackLikeRepository.findByUserIdAndTrackId(userId, trackId)
                .orElseThrow(() -> new RuntimeException("Like not found"));

        Track track = like.getTrack();
        track.setLikesCount(Math.max(0, track.getLikesCount() - 1));
        trackRepository.save(track);

        trackLikeRepository.delete(like);
    }

    @Override
    public List<TrackLike> getUserLikes(Long userId) {
        return trackLikeRepository.findByUserId(userId);
    }
}
