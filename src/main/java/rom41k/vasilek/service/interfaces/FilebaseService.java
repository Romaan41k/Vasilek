package rom41k.vasilek.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FilebaseService {
    String uploadFile(MultipartFile file, String bucketName) throws IOException;
    String readFromFile(String bucketName, String fileName);
}
