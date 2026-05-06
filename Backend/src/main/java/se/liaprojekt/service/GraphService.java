package se.liaprojekt.service;

import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.stereotype.Service;
import se.liaprojekt.dto.GraphResponse;
import se.liaprojekt.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphService {

    private final TokenService tokenService;

    public GraphService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public List<GraphResponse> getAllUsers() {
        String[] scopes = {"https://graph.microsoft.com/.default"};
        GraphServiceClient graphServiceClient = new GraphServiceClient(tokenService.getCredential(), scopes);

        UserCollectionResponse userCollectionResponse = graphServiceClient.users().get();
        List<GraphResponse> graphResponses = new ArrayList<>();
        if (userCollectionResponse != null && userCollectionResponse.getValue() != null) {
            userCollectionResponse.getValue().forEach((user) -> {
                graphResponses.add(new GraphResponse(
                        user.getId(),
                        user.getDisplayName(),
                        user.getGivenName(),
                        user.getSurname(),
                        user.getMail()
                ));
            });
        }
        return graphResponses;
    }

    public GraphResponse getUserByEntraId(String entraId) {
        String[] scopes = {"https://graph.microsoft.com/.default"};
        GraphServiceClient graphServiceClient = new GraphServiceClient(tokenService.getCredential(), scopes);

        User user = graphServiceClient.users().byUserId(entraId).get();
        GraphResponse graphResponse;
        if (user != null) {
            graphResponse = new GraphResponse(
                    user.getId(),
                    user.getDisplayName(),
                    user.getGivenName(),
                    user.getSurname(),
                    user.getMail()
            );
        } else {
            throw new ResourceNotFoundException("User not found");
        }
        return graphResponse;
    }
}