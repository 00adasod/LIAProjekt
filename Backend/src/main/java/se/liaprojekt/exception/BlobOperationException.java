package se.liaprojekt.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus
public class BlobOperationException extends RuntimeException {

    private final int statusCode;
    private final String azureErrorCode;

    public BlobOperationException(String message, int statusCode, String azureErrorCode) {
        super(message);
        this.statusCode = statusCode;
        this.azureErrorCode = azureErrorCode;
    }

    public int getStatusCode() { return statusCode; }
    public String getAzureErrorCode() { return azureErrorCode; }
}