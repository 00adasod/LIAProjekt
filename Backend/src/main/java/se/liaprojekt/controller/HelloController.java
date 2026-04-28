package se.liaprojekt.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.Map;

@RestController
public class HelloController {
    @GetMapping("admin")
    @ResponseBody
    @PreAuthorize("hasAuthority('APPROLE_Participant')")
    public String Participant(Principal principal, Authentication authentication) {
        RestTemplate restTemplate = new RestTemplate();

        String baseUrl = "https://graph.microsoft.com/v1.0";

        //TODO FRÅGA FÖR MOV, API PERMISSIONS, finns det?
        //TODO Behöver kunna hämta information om enskilda användare eller hela klasser

        HttpHeaders headers = new HttpHeaders();
        HttpEntity entity = new HttpEntity(headers);
        headers.setBearerAuth(getAccessToken(restTemplate));
        ResponseEntity<String> answer = restTemplate.exchange(
//                baseUrl + "/users?select=id,displayname,mail",
                baseUrl + "/users",
//                baseUrl + "/me",
                HttpMethod.GET,
                entity,
                String.class
        );
        return "Participant message: " + principal.getName() + ": \n" + answer.getBody();
    }

    @Value("${TENANT_ID}")
    private String tenantId;

    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${CLIENT_SECRET}")
    private String clientSecret;

    private String getAccessToken(RestTemplate restTemplate) {

        String url =
                "https://login.microsoftonline.com/"
                        + tenantId
                        + "/oauth2/v2.0/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add(
                "scope",
                "https://graph.microsoft.com/.default"
        );
        body.add(
                "grant_type",
                "client_credentials"
        );

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        url,
                        request,
                        Map.class
                );
        System.out.println(response.getBody());

        return (String) response.getBody()
                .get("access_token");
    }
}
