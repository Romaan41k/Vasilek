package rom41k.vasilek.service.interfaces;

import rom41k.vasilek.database.entity.GenreSubscription;

import java.util.List;

public interface GenreSubscriptionService {
    GenreSubscription subscribeToGenre(GenreSubscription subscription);
    List<GenreSubscription> findByUserId(Long userId);
    boolean unsubscribeFromGenre(Long userId, String genre);
}
