package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import rom41k.vasilek.database.entity.GenreSubscription;
import rom41k.vasilek.repository.GenreSubscriptionRepository;
import rom41k.vasilek.service.interfaces.GenreSubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreSubscriptionServiceImpl implements GenreSubscriptionService {

    private final GenreSubscriptionRepository genreSubscriptionRepository;

    @Override
    public GenreSubscription subscribeToGenre(GenreSubscription subscription) {
        Long userId = subscription.getUser().getId();
        String genre = subscription.getGenre();

        boolean alreadyExists = genreSubscriptionRepository.existsByUserIdAndGenre(userId, genre);
        if (alreadyExists) {
            throw new IllegalStateException("Вы уже подписаны на жанр: " + genre);
        }

        return genreSubscriptionRepository.save(subscription);
    }

    @Override
    public List<GenreSubscription> findByUserId(Long userId) {
        return genreSubscriptionRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public boolean unsubscribeFromGenre(Long userId, String genre) {
        Optional<GenreSubscription> subscriptionOpt = genreSubscriptionRepository.findByUserIdAndGenre(userId, genre);
        if (subscriptionOpt.isPresent()) {
            genreSubscriptionRepository.delete(subscriptionOpt.get());
            return true;
        }
        return false;
    }
}
