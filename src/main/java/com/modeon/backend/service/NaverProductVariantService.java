package com.modeon.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.modeon.backend.dto.NaverProductImageDto;
import com.modeon.backend.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverProductVariantService {
    private final NaverAuthService naverAuthService;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.commerce.naver.com")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public String loadOptionGuides(String categoryId) {
        String accessToken;
        try {
            accessToken = naverAuthService.requestAccessToken();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        String response = webClient.get()
                .uri("/external/v2/standard-purchase-option-guides?categoryId="+categoryId)
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    try {
                                        String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                                                objectMapper.readValue(body, Map.class));
                                        return new RuntimeException("API Error:\n" + pretty);
                                    } catch (JsonProcessingException e) {
                                        return new RuntimeException("API Error: " + body);
                                    }
                                }))
                .bodyToMono(String.class)
                .block();

        return response;

    }
}
