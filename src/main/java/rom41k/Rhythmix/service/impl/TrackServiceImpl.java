package rom41k.Rhythmix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rom41k.Rhythmix.database.entity.Track;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.repository.TrackRepository;
import rom41k.Rhythmix.service.interfaces.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Убедитесь, что этот импорт есть
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;

    // Проверьте, что аннотация @Value и объявление поля верны
    @Value("${app.upload.dir:D:/Projects/Rhythmix555/uploads}")
    private String uploadDir;

    @Override
    public Track uploadTrack(Track track) {
        return trackRepository.save(track);
    }

    @Override
    public List<Track> findAll() {
        return trackRepository.findAll();
    }

    @Override
    public Optional<Track> findById(Long id) {
        return trackRepository.findById(id);
    }

    @Override
    public List<Track> findByGenre(String genre) {
        return trackRepository.findByGenre(genre);
    }

    @Override
    public List<Track> findByArtistId(Long artistId) {
        return trackRepository.findByArtistId(artistId);
    }

    @Override
    public void deleteTrack(Long id) {
        trackRepository.deleteById(id);
    }

    @Override
    public void incrementLikes(Long trackId) {
        trackRepository.incrementLikes(trackId);
    }

    @Override
    public void incrementListens(Long trackId) {
        trackRepository.incrementListens(trackId);
    }

    @Override
    public void save(Track track) {
        trackRepository.save(track);
    }

    @Override
    public Optional<Path> getTrackFilePath(Long id) {
        return trackRepository.findById(id)
                .map(track -> {
                    // Убедитесь, что здесь используется переменная uploadDir
                    String relativePath = track.getFilePath().replace("/uploads/", "");
                    return Paths.get(uploadDir, relativePath);
                });
    }

    @Override
    @Transactional
    public void updateTrack(Long trackId, String title, String genre, MultipartFile newCover, User artist) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new RuntimeException("Трек не найден"));

        if (!track.getArtist().getId().equals(artist.getId())) {
            throw new SecurityException("Вы не являетесь автором трека");
        }

        // Ограничение по времени редактирования (30 минут)
        // Примечание: Здесь жестко задано 30 минут, не используется @Value из конфига.
        // Также добавлена проверка listenCount > 0.
        Duration duration = Duration.between(track.getCreatedAt(), LocalDateTime.now());
        if (duration.toMinutes() > 30 || track.getListensCount() > 0) {
            throw new RuntimeException("Редактирование доступно только в течение 30 минут после загрузки и до первого прослушивания");
        }

        boolean changed = false; // Флаг для отслеживания изменений

        if (title != null && !title.isBlank()) {
            track.setTitle(title);
            changed = true;
        }

        if (genre != null && !genre.isBlank()) {
            track.setGenre(genre);
            changed = true;
        }

        if (newCover != null && !newCover.isEmpty()) {
            String coverName = UUID.randomUUID() + "_" + newCover.getOriginalFilename();
            // Используйте Paths.get для надежного построения пути
            Path coverPath = Paths.get(uploadDir, "covers", coverName); // Исправлено построение пути

            File coverDirFile = coverPath.getParent().toFile(); // Получаем директорию из объекта Path
            if (!coverDirFile.exists()) {
                coverDirFile.mkdirs();
            }

            try {
                newCover.transferTo(coverPath); // Сохранение обложки
            } catch (IOException e) {
                // Лучше использовать более специфичное исключение, например, RuntimeException с сообщением
                throw new RuntimeException("Ошибка при загрузке файла обложки", e);
            }

            // Устанавливаем новый путь к обложке в сущности
            track.setCoverPath("/uploads/covers/" + coverName); // Путь для базы данных

            // !!! ЯВНО СОХРАНЯЕМ ИЗМЕНЕНИЯ В БД СРАЗУ ПОСЛЕ ОБНОВЛЕНИЯ ПУТИ ОБЛОЖКИ !!!
            trackRepository.save(track); // <--- ДОБАВЬТЕ ЭТУ СТРОКУ ЗДЕСЬ

            System.out.println("Обновленный coverPath сохранен в БД: " + track.getCoverPath());
            changed = true; // Отмечаем, что были изменения
        } else {
            System.out.println("Обложка не была предоставлена или пуста.");
        }


        // Если title или genre были изменены, но обложка нет, сохраняем еще раз (или можно было бы объединить логику)
        // Если обложка была обновлена, мы уже вызвали save() выше.
        // Чтобы избежать двойного сохранения, можно добавить флаг или структурировать if'ы иначе.
        // Простой вариант: вызвать save() в конце, но убедиться, что JPA отслеживает изменения.
        // Однако, явный save() после изменения coverPath надежнее в данном случае.
        // Оставляем этот save() на случай, если менялись только title/genre и обложка не менялась.
        // Если вы гарантируете, что save() после обложки всегда произойдет, этот save() можно сделать условным.
        if (changed) { // Опционально: сохраняем только если были изменения
            trackRepository.save(track);
        }


    }


}