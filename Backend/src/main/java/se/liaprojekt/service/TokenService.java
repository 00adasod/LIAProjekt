package se.liaprojekt.service;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.*;
import com.azure.spring.cloud.autoconfigure.implementation.aad.security.graph.GraphClient;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import se.liaprojekt.exception.BadRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
public class TokenService {
    @Value("${spring.cloud.azure.tenant-id}")
    private String tenantId;

    @Value("${spring.cloud.azure.credential.client-id}")
    private String clientId;

    @Value("${spring.cloud.azure.credential.client-secret}")
    private String clientSecret;


    private TokenResponseBody tokenResponseBody;
    private long tokenExpiryTimeMillis;

//    public String getAccessToken(RestTemplate restTemplate) {
//        String url = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
//
//        //If a token has been saved, and it doesn't expire within the next minute return current token
//        if (tokenResponseBody != null && tokenExpiryTimeMillis > System.currentTimeMillis() + 60000) {
//            return tokenResponseBody.access_token;
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> body =
//                new LinkedMultiValueMap<>();
//
//        body.add("client_id", clientId);
//        body.add("client_secret", clientSecret);
//        body.add(
//                "scope",
//                "https://graph.microsoft.com/.default"
//        );
//        body.add(
//                "grant_type",
//                "client_credentials"
//        );
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//        try {
//            ResponseEntity<TokenResponseBody> response = restTemplate.postForEntity(url, request, TokenResponseBody.class);
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                tokenResponseBody = response.getBody();
//                tokenExpiryTimeMillis = System.currentTimeMillis() + tokenResponseBody.expires_in * 1000;
//                return tokenResponseBody.access_token;
//            } else {
//                //TODO throw appropriate exception when request fail
//            }
//
//            return response.getBody().access_token;
//        } catch (Exception e) {
//            throw new RuntimeException("tenantId: \n" + e.getMessage());
//        }
//
//    }

    public String getAccessToken(RestTemplate restTemplate) {
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
//                .clientId(clientId)

                .build();
        TokenRequestContext tokenRequestContext = new TokenRequestContext();
        tokenRequestContext.setScopes(List.of("https://graph.microsoft.com/.default"));



        return credential.getToken(tokenRequestContext).block().getToken();
    }

    private TokenCredential credential;

    public TokenCredential getCredential() {
        if (credential == null) {
            credential = new DefaultAzureCredentialBuilder()
//                    .requireEnvVars(AzureIdentityEnvVars.AZURE_CLIENT_ID, AzureIdentityEnvVars.AZURE_CLIENT_SECRET, AzureIdentityEnvVars.AZURE_TENANT_ID)
                    .build();
        }
        return credential;
    }

//    public String getAccessToken(RestTemplate restTemplate) {
//        String url = "http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=https://graph.microsoft.com/.default";
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Metadata", "true");
//        ResponseEntity<TokenResponseBody> response = restTemplate.getForEntity(url, TokenResponseBody.class, headers);
//
//        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//            tokenResponseBody = response.getBody();
//            tokenExpiryTimeMillis = System.currentTimeMillis() + tokenResponseBody.expires_in * 1000;
//            return tokenResponseBody.access_token;
//        }
//        return null;
//    }

//    public String getAccessToken(RestTemplate restTemplate) throws Exception {
//        URL msiEndpoint = new URL("http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=https://graph.microsoft.com/.default");
//        HttpURLConnection con = (HttpURLConnection) msiEndpoint.openConnection();
//        con.setRequestMethod("GET");
//        con.setRequestProperty("Metadata", "true");
//
//        if (con.getResponseCode()!=200) {
//            throw new Exception("Error calling managed identity token endpoint.");
//        }
//
//        InputStream responseStream = con.getInputStream();
//
//        JsonFactory factory = new JsonFactory();
//        JsonParser parser = factory.createParser(responseStream);
//
//        while(!parser.isClosed()){
//            JsonToken jsonToken = parser.nextToken();
//
//            if(JsonToken.FIELD_NAME.equals(jsonToken)){
//                String fieldName = parser.getCurrentName();
//                jsonToken = parser.nextToken();
//
//                if("access_token".equals(fieldName)) {
//                    String accesstoken = parser.getValueAsString();
//                    System.out.println("Access Token: " + accesstoken.substring(0, 5) + "..." + accesstoken.substring(accesstoken.length() - 5));
//                    return accesstoken;
//                }
//            }
//        }
//        return "";
//    }


    private record TokenResponseBody(String access_token, String token_type, long expires_in) {}
}