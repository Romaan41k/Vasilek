package rom41k.vasilek.database.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Data
@Entity
@Getter
@Setter
@Table(name = "genre_subscriptions")
public class GenreSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false, updatable = false)
    private LocalDateTime subscribedAt = LocalDateTime.now();
}
