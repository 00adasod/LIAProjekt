package se.liaprojekt.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import se.liaprojekt.controller.util.SupportedMediaTypeResolver;
import se.liaprojekt.service.BlobStorageService;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
public class BlobStorageController {
    private static final long CHUNK_SIZE = 10 * 1024 * 1024; // 10MB chunks

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

    @GetMapping("/stream/{fileName}")
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable String fileName,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {

        if (!mediaTypeResolver.isVideo(fileName)) {
            return ResponseEntity.badRequest().build();
        }

        MediaType contentType = mediaTypeResolver.resolve(fileName);
        long fileSize = blobStorageService.getBlobSize(fileName);

        // Parse range header — default to full file if absent
        long start = 0;
        long end = fileSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] parts = rangeHeader.substring(6).split("-");
            start = Long.parseLong(parts[0]);
            end = parts.length > 1 && !parts[1].isEmpty()
                    ? Long.parseLong(parts[1])
                    : Math.min(start + CHUNK_SIZE - 1, fileSize - 1);
        }

        final long rangeStart = start;
        final long rangeLength = end - start + 1;

        StreamingResponseBody body = outputStream ->
                blobStorageService.streamFile(fileName, outputStream, rangeStart, rangeLength);

        return ResponseEntity.status(rangeHeader != null
                        ? HttpStatus.PARTIAL_CONTENT
                        : HttpStatus.OK)
                .header(HttpHeaders.CONTENT_RANGE,
                        "bytes %d-%d/%d".formatted(rangeStart, end, fileSize))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeLength))
                .contentType(contentType)
                .body(body);
    }
}