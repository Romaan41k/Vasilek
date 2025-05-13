package rom41k.Rhythmix.service.interfaces;

import rom41k.Rhythmix.database.entity.GenreSubscription;

import java.util.List;

public interface GenreSubscriptionService {
    GenreSubscription subscribeToGenre(GenreSubscription subscription);
    List<GenreSubscription> findByUserId(Long userId);
    boolean unsubscribeFromGenre(Long userId, String genre);
}
