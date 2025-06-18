package rom41k.vasilek.service.interfaces;

import rom41k.vasilek.database.entity.ArtistSubscription;

import java.util.List;

public interface ArtistSubscriptionService {
    ArtistSubscription subscribeToArtist(ArtistSubscription subscription);
    List<ArtistSubscription> findByUserId(Long userId);
    void unsubscribeFromArtist(Long userId, Long artistId);
}
