package rom41k.vasilek.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rom41k.vasilek.service.interfaces.FilebaseService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FilebaseServiceImpl implements FilebaseService {

    private final AmazonS3 amazonS3;
    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    public FilebaseServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String uploadFile(MultipartFile file, String bucketName) throws IOException {
        Path tempFilePath = Files.createTempFile(null, file.getOriginalFilename());
        file.transferTo(tempFilePath.toFile());

        File uploadFile = tempFilePath.toFile();
        amazonS3.putObject(bucketName, uploadFile.getName(), uploadFile);

        Files.delete(tempFilePath);

        return amazonS3.getUrl(bucketName, uploadFile.getName()).toString();
    }

    @Override
    public String readFromFile(String bucketName, String fileName) {
        lock.lock();
        try {
            S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));

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