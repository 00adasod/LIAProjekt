package se.liaprojekt.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import se.liaprojekt.controller.util.SupportedMediaTypeResolver;
import se.liaprojekt.service.BlobStorageService;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
public class BlobStorageController {

    private final BlobStorageService blobStorageService;
    private final SupportedMediaTypeResolver mediaTypeResolver;

    public BlobStorageController(BlobStorageService blobStorageService,
                                 SupportedMediaTypeResolver mediaTypeResolver) {
        this.blobStorageService = blobStorageService;
        this.mediaTypeResolver = mediaTypeResolver;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !mediaTypeResolver.isSupported(fileName)) {
            return ResponseEntity.badRequest()
                    .body("Unsupported file type. Allowed: pdf, mp4, mov, avi, mkv.");
        }
        blobStorageService.uploadFile(fileName, file.getInputStream(), file.getSize());
        return ResponseEntity.ok("Uploaded: " + fileName);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Void> download(@PathVariable String fileName) {
        URI downloadUrl = blobStorageService.generateDownloadUrl(fileName);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(downloadUrl)
                .build();
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<String> delete(@PathVariable String fileName) {
        blobStorageService.deleteFile(fileName);
        return ResponseEntity.ok("Deleted: " + fileName);
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> list(
            @RequestParam(required = false, defaultValue = "all") String type) {
        Set<String> extensions = mediaTypeResolver.extensionsForType(type);
        return ResponseEntity.ok(blobStorageService.listFiles(extensions));
    }
}