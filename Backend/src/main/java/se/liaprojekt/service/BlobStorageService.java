package se.liaprojekt.service;

import com.azure.core.util.Context;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.liaprojekt.exception.BlobOperationException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BlobStorageService {

    private static final Logger log = LoggerFactory.getLogger(BlobStorageService.class);

    private final BlobContainerClient pdfContainerClient;
    private final BlobContainerClient videoContainerClient;
    private final String accountName;
    private final String pdfContainerName;
    private final String videoContainerName;
    private final long sasExpiryMinutes;
    private final String frontDoorEndpoint;

    public BlobStorageService(
            @Value("${spring.cloud.azure.storage.blob.account-name}") String accountName,
            @Value("${spring.cloud.azure.storage.account-key}") String accountKey,
            @Value("${spring.cloud.azure.storage.container-name-pdf}") String pdfContainerName,
            @Value("${spring.cloud.azure.storage.container-name-video}") String videoContainerName,
            @Value("${spring.cloud.azure.storage.sas-expiry-minutes}") long sasExpiryMinutes,
            @Value("${spring.cloud.azure.storage.frontdoor-endpoint}") String frontDoorEndpoint) {


        if (!frontDoorEndpoint.startsWith("https://")) {
            throw new IllegalStateException(
                    "azure.storage.frontdoor-endpoint must be an absolute HTTPS URL, got: '%s'"
                            .formatted(frontDoorEndpoint));
        }

        this.accountName = accountName;
        this.pdfContainerName = pdfContainerName;
        this.videoContainerName = videoContainerName;
        this.sasExpiryMinutes = sasExpiryMinutes;
        this.frontDoorEndpoint = frontDoorEndpoint.replaceAll("/+$", "");
        StorageSharedKeyCredential sharedKeyCredential = new StorageSharedKeyCredential(accountName, accountKey);

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .endpoint("https://%s.blob.core.windows.net".formatted(accountName))
                .credential(sharedKeyCredential)
                .buildClient();

        this.pdfContainerClient = serviceClient.getBlobContainerClient(pdfContainerName);
        this.videoContainerClient = serviceClient.getBlobContainerClient(videoContainerName);

        validateContainer(pdfContainerClient, pdfContainerName);
        validateContainer(videoContainerClient, videoContainerName);
    }

    private void validateContainer(BlobContainerClient client, String name) {
        if (!client.exists()) {
            throw new IllegalStateException(
                    "Container '%s' does not exist.".formatted(name));
        }
    }

    // Resolves the correct container client based on file extension
    private BlobContainerClient resolveContainer(String fileName) {
        String ext = getExtension(fileName);
        return switch (ext) {
            case "pdf"            -> pdfContainerClient;
            case "mp4", "mov",
                 "avi", "mkv"    -> videoContainerClient;
            default -> throw new BlobOperationException(
                    "Unsupported file type: " + ext, 400, "UnsupportedFileType");
        };
    }

    // Resolves the correct container name based on file extension (for URL building)
    private String resolveContainerName(String fileName) {
        String ext = getExtension(fileName);
        return switch (ext) {
            case "pdf"            -> pdfContainerName;
            case "mp4", "mov",
                 "avi", "mkv"    -> videoContainerName;
            default -> throw new BlobOperationException(
                    "Unsupported file type: " + ext, 400, "UnsupportedFileType");
        };
    }

    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "";
    }

    private String generateSasToken(String fileName, BlobSasPermission permissions) {
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(sasExpiryMinutes),
                permissions)
                .setStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));

        return resolveContainer(fileName).getBlobClient(fileName).generateSas(sasValues);
    }

    private BlobClient sasClient(String fileName, BlobSasPermission permissions) {
        String sasToken = generateSasToken(fileName, permissions);
        String containerName = resolveContainerName(fileName);
        String sasUrl = "https://%s.blob.core.windows.net/%s/%s?%s"
                .formatted(accountName, containerName, fileName, sasToken);
        return new BlobClientBuilder().endpoint(sasUrl).buildClient();
    }

    public void uploadFile(String fileName, InputStream data,
                           long length, String sectionId) {
        try {
            BlobClient client = sasClient(fileName,
                    new BlobSasPermission()
                            .setWritePermission(true)
                            .setTagsPermission(true));

            client.upload(data, length, true);

            if (sectionId != null) {
                client.setTags(Map.of("sectionId", sectionId));
            }
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public URI generateDownloadUrl(String fileName) {
        try {
            String sasToken = generateSasToken(
                    fileName, new BlobSasPermission().setReadPermission(true));
            String containerName = resolveContainerName(fileName);

            String rawUrl = "%s/%s/%s?%s"
                    .formatted(frontDoorEndpoint, containerName, fileName, sasToken);

            URI uri = URI.create(rawUrl);
            if (!uri.isAbsolute()) {
                throw new IllegalStateException(
                        "Generated download URL is not absolute: '%s'.".formatted(rawUrl));
            }
            return uri;
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            sasClient(fileName, new BlobSasPermission().setDeletePermission(true))
                    .delete();
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public List<String> listFiles(Set<String> allowedExtensions) {
        try {
            Stream<String> pdfs = allowedExtensions.stream().anyMatch(e -> e.equals("pdf"))
                    ? pdfContainerClient.listBlobs().stream().map(BlobItem::getName)
                    : Stream.empty();

            Stream<String> videos = allowedExtensions.stream().anyMatch(
                    e -> Set.of("mp4", "mov", "avi", "mkv").contains(e))
                    ? videoContainerClient.listBlobs().stream().map(BlobItem::getName)
                    : Stream.empty();

            return Stream.concat(pdfs, videos)
                    .filter(name -> allowedExtensions.contains(getExtension(name)))
                    .collect(Collectors.toList());
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public long getBlobSize(String fileName) {
        try {
            return resolveContainer(fileName)
                    .getBlobClient(fileName)
                    .getProperties()
                    .getBlobSize();
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public List<String> listFilesBySectionId(String sectionId) {
        try {
            String query = "\"sectionId\" = '%s'".formatted(sectionId);

            // Query runs across both containers and returns matching blob names
            Stream<String> fromPdf = pdfContainerClient
                    .findBlobsByTags(query).stream()
                    .map(TaggedBlobItem::getName);

            Stream<String> fromVideo = videoContainerClient
                    .findBlobsByTags(query).stream()
                    .map(TaggedBlobItem::getName);

            return Stream.concat(fromPdf, fromVideo)
                    .collect(Collectors.toList());
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public Map<String, String> getFileTags(String fileName) {
        try {
            return resolveContainer(fileName)
                    .getBlobClient(fileName)
                    .getTags();
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public void updateSectionId(String fileName, String sectionId) {
        try {
            BlobClient client = resolveContainer(fileName).getBlobClient(fileName);
            Map<String, String> existing = client.getTags();
            existing.put("sectionId", sectionId);
            client.setTags(existing);
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    public void streamFile(String fileName, OutputStream outputStream,
                           long offset, long length) {
        try {
            BlobRange range = new BlobRange(offset, length);
            DownloadRetryOptions retryOptions = new DownloadRetryOptions().setMaxRetryRequests(3);

            resolveContainer(fileName)
                    .getBlobClient(fileName)
                    .downloadStreamWithResponse(outputStream, range, retryOptions, null, false, null, Context.NONE);
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    private BlobOperationException translateException(BlobStorageException ex) {
        log.error("Azure Blob Storage error — HTTP {}: {}", ex.getStatusCode(), ex.getMessage());
        return new BlobOperationException(
                ex.getMessage(),
                ex.getStatusCode(),
                ex.getErrorCode() != null ? ex.getErrorCode().toString() : "unknown"
        );
    }
}