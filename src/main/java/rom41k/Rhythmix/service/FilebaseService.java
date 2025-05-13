package rom41k.Rhythmix.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FilebaseService {

    private final AmazonS3 amazonS3;
    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    public FilebaseService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    // Загрузка файла на S3
    public String uploadFile(MultipartFile file, String bucketName) throws IOException {
        // Создание временного файла
        Path tempFilePath = Files.createTempFile(null, file.getOriginalFilename());
        file.transferTo(tempFilePath.toFile());

        // Загрузка файла на Filebase (S3)
        File uploadFile = tempFilePath.toFile();
        amazonS3.putObject(bucketName, uploadFile.getName(), uploadFile);

        // Удаляем временный файл
        Files.delete(tempFilePath);

        // Возвращаем URL для доступа к файлу
        return amazonS3.getUrl(bucketName, uploadFile.getName()).toString();
    }

    // Чтение файла с S3
    public String readFromFile(String bucketName, String fileName) {
        lock.lock();
        try {
            // Получаем объект из S3
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));

            // Читаем содержимое файла
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка при чтении файла с S3";
        } finally {
            lock.unlock();
        }
    }
}
