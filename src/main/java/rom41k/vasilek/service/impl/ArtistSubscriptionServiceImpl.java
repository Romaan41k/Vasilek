package rom41k.vasilek.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import rom41k.vasilek.database.entity.ArtistSubscription;
import rom41k.vasilek.database.entity.User;
import rom41k.vasilek.database.enums.Role;
import rom41k.vasilek.repository.ArtistSubscriptionRepository;
import rom41k.vasilek.repository.UserRepository;
import rom41k.vasilek.service.interfaces.ArtistSubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArtistSubscriptionServiceImpl implements ArtistSubscriptionService {

    private final ArtistSubscriptionRepository artistSubscriptionRepository;
    private final UserRepository userRepository;

    @Override
    public ArtistSubscription subscribeToArtist(ArtistSubscription subscription) {
        Long userId = subscription.getUser().getId();
        Long artistId = subscription.getArtist().getId();

        if (artistSubscriptionRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new IllegalStateException("Вы уже подписаны на этого артиста");
        }

        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Артист не найден"));

        if (artist.getRole() != Role.ARTIST) {
            throw new IllegalArgumentException("Пользователь не является артистом");
        }

        subscription.setArtist(artist);

        return artistSubscriptionRepository.save(subscription);
    }

    @Override
    public List<ArtistSubscription> findByUserId(Long userId) {
        return artistSubscriptionRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void unsubscribeFromArtist(Long userId, Long artistId) {
        Optional<ArtistSubscription> subscriptionOpt = artistSubscriptionRepository.findByUserIdAndArtistId(userId, artistId);

        if (subscriptionOpt.isEmpty()) {
            throw new IllegalStateException("Подписка на этого артиста не найдена.");
        }

        artistSubscriptionRepository.delete(subscriptionOpt.get());
    }
}
