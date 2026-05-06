package se.liaprojekt.service;

import com.azure.identity.*;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserCollectionResponse;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.liaprojekt.dto.GraphResponse;
import se.liaprojekt.exception.BadRequestException;
import se.liaprojekt.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class GraphService {

    private final TokenService tokenService;
    private final RestTemplate restTemplate;

    @Value("${graph.base-url}")
    private String graphBaseUrl;

    public GraphService(TokenService tokenService) {
        this.tokenService = tokenService;
        this.restTemplate = new RestTemplate();
    }

//    public List<GraphResponse> getAllUsers(String accessToken) {
//        String token = tokenService.getAccessToken(restTemplate);
//
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        headers.setBearerAuth(token);
//        ResponseEntity<GraphAPIResponse> response = restTemplate.exchange(
//                graphBaseUrl + "/users",
//                HttpMethod.GET,
//                entity,
//                GraphAPIResponse.class
//        );
//
//        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//            return response.getBody().value();
//        } else {
//            //TODO throw appropriate exception when request fail
//        }
//
//        return null;
//    }

    public List<GraphResponse> getAllUsers() {
//        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
//                .requireEnvVars(AzureIdentityEnvVars.AZURE_CLIENT_ID, AzureIdentityEnvVars.AZURE_CLIENT_SECRET, AzureIdentityEnvVars.AZURE_TENANT_ID)
//                .build();

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

//    public GraphResponse getUserByEntraId(String entraId) {
//        String token;
//        try {
//            token = tokenService.getAccessToken(restTemplate);
//        } catch (Exception e) {
//            throw new RuntimeException(e.getMessage());
//        }
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//        headers.setBearerAuth(token);
//        ResponseEntity<GraphResponse> response = restTemplate.exchange(
//                graphBaseUrl + "/users/" + entraId,
//                HttpMethod.GET,
//                entity,
//                GraphResponse.class
//        );
//        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//            return response.getBody();
//        } else {
//            //TODO throw appropriate exception when request fail
//        }
//
//        return null;
//    }

    private record GraphAPIResponse(
            List<GraphResponse> value) {}
}