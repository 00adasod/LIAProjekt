package se.liaprojekt.worker;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailMessageHandler {

    private static final Logger log =
            LoggerFactory.getLogger(EmailMessageHandler.class);

    /**
     * Hanterar inkommande messages
     */
    public void handleMessage(ServiceBusReceivedMessageContext context) {

        ServiceBusReceivedMessage message = context.getMessage();

        log.info("📩 Received message from Service Bus");
        log.info("MessageId: {}", message.getMessageId());
        log.info("Body: {}", message.getBody());

        // TODO: din business logic här
        // t.ex. skicka email, spara i DB etc.
    }

    /**
     * Hanterar fel (important i production)
     */
    public void handleError(ServiceBusErrorContext context) {

        Throwable error = context.getException();

        log.error("❌ Service Bus error");

        log.error("Namespace: {}", context.getFullyQualifiedNamespace());
        log.error("Entity: {}", context.getEntityPath());

        if (error != null) {
            log.error("Error type: {}", error.getClass().getSimpleName());
            log.error("Message: {}", error.getMessage(), error);
        }
    }
}