package com.modeon.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.modeon.backend.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverCategoryService {

    private final NaverAuthService naverAuthService;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.commerce.naver.com")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public List<Map<String, Object>> getNaverCategory(String naverCategoryId, String accessToken) throws JsonProcessingException {
        String url = "/external/v1/categories/" + naverCategoryId + "/sub-categories";

        String response = webClient.get()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    try {
                                        // JSON 예쁘게 출력
                                        String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
                                                objectMapper.readValue(body, Map.class));
                                        return new RuntimeException("API Error:\n" + pretty);
                                    } catch (JsonProcessingException e) {
                                        return new RuntimeException("API Error: " + body);
                                    }
                                }))
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(
                response,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
        );

    }
}
