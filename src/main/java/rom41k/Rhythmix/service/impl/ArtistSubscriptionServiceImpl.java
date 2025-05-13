package rom41k.Rhythmix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import rom41k.Rhythmix.database.entity.ArtistSubscription;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.database.enums.Role;
import rom41k.Rhythmix.repository.ArtistSubscriptionRepository;
import rom41k.Rhythmix.repository.UserRepository;
import rom41k.Rhythmix.service.interfaces.ArtistSubscriptionService;
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

        // Проверка, существует ли уже подписка
        if (artistSubscriptionRepository.existsByUserIdAndArtistId(userId, artistId)) {
            throw new IllegalStateException("Вы уже подписаны на этого артиста");
        }

        // Загружаем артиста полностью из базы
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new IllegalArgumentException("Артист не найден"));

        // Проверяем, что это действительно артист (например, по роли)
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
