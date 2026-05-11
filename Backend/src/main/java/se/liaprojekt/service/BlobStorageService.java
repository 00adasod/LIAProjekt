package se.liaprojekt.service;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlobStorageService {

    private static final Logger log = LoggerFactory.getLogger(BlobStorageService.class);

    private final BlobContainerClient containerClient;
    private final StorageSharedKeyCredential sharedKeyCredential;
    private final String accountName;
    private final String containerName;
    private final long sasExpiryMinutes;
    private final String frontDoorEndpoint;

    public BlobStorageService(
            @Value("${spring.cloud.azure.storage.blob.account-name}") String accountName,
            @Value("${spring.cloud.azure.storage.account-key}") String accountKey,
            @Value("${spring.cloud.azure.storage.container-name}") String containerName,
            @Value("${spring.cloud.azure.storage.sas-expiry-minutes}") long sasExpiryMinutes,
            @Value("${spring.cloud.azure.storage.frontdoor-endpoint}") String frontDoorEndpoint) {

        this.accountName = accountName;
        this.containerName = containerName;
        this.sasExpiryMinutes = sasExpiryMinutes;
        this.frontDoorEndpoint = frontDoorEndpoint;
        this.sharedKeyCredential = new StorageSharedKeyCredential(accountName, accountKey);

        this.containerClient = new BlobServiceClientBuilder()
                .endpoint("https://%s.blob.core.windows.net".formatted(accountName))
                .credential(sharedKeyCredential)
                .buildClient()
                .getBlobContainerClient(containerName);

        if (!this.containerClient.exists()) {
            throw new IllegalStateException(
                    "Container '%s' does not exist.".formatted(containerName));
        }
    }

    // Generates a SAS token scoped to a single blob with the given permissions
    private String generateSasToken(String fileName, BlobSasPermission permissions) {
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(sasExpiryMinutes),
                permissions)
                .setStartTime(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5)); // clock skew buffer

        BlobClient blobClient = containerClient.getBlobClient(fileName);
        return blobClient.generateSas(sasValues);
    }

    // Builds a BlobClient from a pre-signed SAS URL — no credential needed on the client itself
    private BlobClient sasClient(String fileName, BlobSasPermission permissions) {
        String sasToken = generateSasToken(fileName, permissions);
        String sasUrl = "https://%s.blob.core.windows.net/%s/%s?%s"
                .formatted(accountName, containerName, fileName, sasToken);
        return new BlobClientBuilder().endpoint(sasUrl).buildClient();
    }

    public void uploadFile(String fileName, InputStream data, long length) {
        try {
            sasClient(fileName, new BlobSasPermission().setWritePermission(true))
                    .upload(data, length, true);
        } catch (BlobStorageException ex) {
            throw translateException(ex);
        }
    }

    // Builds a Front Door URL with a SAS token — client calls this directly
    public URI generateDownloadUrl(String fileName) {
        try {
            String sasToken = generateSasToken(
                    fileName, new BlobSasPermission().setReadPermission(true));

            return URI.create("%s/%s/%s?%s"
                    .formatted(frontDoorEndpoint, containerName, fileName, sasToken));
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
            return containerClient.listBlobs().stream()
                    .map(BlobItem::getName)
                    .filter(name -> {
                        int dot = name.lastIndexOf('.');
                        String ext = dot >= 0 ? name.substring(dot + 1).toLowerCase() : "";
                        return allowedExtensions.contains(ext);
                    })
                    .collect(Collectors.toList());
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