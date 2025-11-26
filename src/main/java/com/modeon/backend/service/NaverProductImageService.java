package com.modeon.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.modeon.backend.dto.NaverProductImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverProductImageService {


    private final NaverAuthService naverAuthService;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.commerce.naver.com")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public NaverProductImageDto uploadImage(List<MultipartFile> request) throws IOException {

        String accessToken;
        try {
            accessToken = naverAuthService.requestAccessToken();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        MultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        for (MultipartFile file : request) {
            // ByteArrayResource를 사용해서 MultipartFile wrapping
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            multipartData.add("imageFiles", resource);
        }


        String response = webClient.post()
                .uri("/external/v1/product-images/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + accessToken)
                .body(BodyInserters.fromMultipartData(multipartData))
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

        try {
            return objectMapper.readValue(response, NaverProductImageDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }

}
