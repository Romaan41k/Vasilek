package rom41k.Rhythmix.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rom41k.Rhythmix.service.interfaces.FilebaseService;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private final FilebaseService filebaseService;

    @Autowired
    public FileController(FilebaseService filebaseService) {
        this.filebaseService = filebaseService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = filebaseService.uploadFile(file, "my-music-bucket");
            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при загрузке файла: " + e.getMessage());
        }
    }
}
