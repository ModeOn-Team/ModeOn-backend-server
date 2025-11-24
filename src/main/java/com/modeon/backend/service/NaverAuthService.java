package com.modeon.backend.service;

import com.modeon.backend.utill.NaverSignUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.commerce.naver.com")
            .build();



    public String requestAccessToken() {
        String clientId = "7N7vxws0OVPvbvj6F5eFJL";
        String clientSecret = "$2a$04$VGrl08g/BvXEwcZxcLV7Ve";
        Long timestamp = Instant.now().toEpochMilli();
        String signature = NaverSignUtil.generateSignature(clientId, clientSecret, timestamp);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("timestamp", String.valueOf(timestamp));
        formData.add("type", "SELF");
        formData.add("grant_type", "client_credentials");
        formData.add("client_secret_sign", signature);

        System.out.println("Request Body: " + formData);

        String response = webClient.post()
                .uri("/external/v1/oauth2/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(response);
        return response;
    }

}
