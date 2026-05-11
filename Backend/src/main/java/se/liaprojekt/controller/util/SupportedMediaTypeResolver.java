package se.liaprojekt.controller.util;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class SupportedMediaTypeResolver {

    private static final Map<String, MediaType> EXTENSION_TYPE_MAP = Map.of(
            "pdf",  MediaType.APPLICATION_PDF,
            "mp4",  MediaType.parseMediaType("video/mp4"),
            "mov",  MediaType.parseMediaType("video/quicktime"),
            "avi",  MediaType.parseMediaType("video/x-msvideo"),
            "mkv",  MediaType.parseMediaType("video/x-matroska")
    );

    private static final Set<String> SUPPORTED_EXTENSIONS =
            EXTENSION_TYPE_MAP.keySet();

    public boolean isSupported(String fileName) {
        return SUPPORTED_EXTENSIONS.contains(getExtension(fileName));
    }

    public MediaType resolve(String fileName) {
        return EXTENSION_TYPE_MAP.getOrDefault(
                getExtension(fileName),
                MediaType.APPLICATION_OCTET_STREAM
        );
    }

    public Set<String> extensionsForType(String type) {
        return switch (type.toLowerCase()) {
            case "pdf"   -> Set.of("pdf");
            case "video" -> Set.of("mp4", "mov", "avi", "mkv");
            default      -> SUPPORTED_EXTENSIONS;
        };
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }
}