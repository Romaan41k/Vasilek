package rom41k.Rhythmix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.TrackLike;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.repository.TrackLikeRepository;
import rom41k.Rhythmix.repository.TrackRepository;
import rom41k.Rhythmix.service.interfaces.TrackLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
        trackRepository.save(track); // обновляем лайки

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

        Track track = like.getTrack(); // гарантированно инициализированный трек
        track.setLikesCount(Math.max(0, track.getLikesCount() - 1));
        trackRepository.save(track); // обновляем лайки

        trackLikeRepository.delete(like);
    }
}
