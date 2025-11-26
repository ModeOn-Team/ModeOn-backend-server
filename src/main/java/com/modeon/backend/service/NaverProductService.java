package com.modeon.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.modeon.backend.dto.NaverProductImageDto;
import com.modeon.backend.dto.NaverProductRequest;
import com.modeon.backend.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NaverProductService {

    private final NaverAuthService naverAuthService;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.commerce.naver.com")
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public void uploadProduct(ProductResponse product, NaverProductImageDto imageDto) {
        System.out.println("product: " + product);
        String accessToken;
        try {
            accessToken = naverAuthService.requestAccessToken();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String categoryCode = "50000805";

        NaverProductRequest request = NaverProductRequest.builder()
                .leafCategoryId("50000805")
                .name("테스트 상품 - 니트/ 스웨터")
                .guideId(4681)
                .minorPurchasable(true)
                .commonDetailContent("상품 상세 정보")
                .productInfoProvidedNotice(
                        NaverProductRequest.ProductInfoProvidedNotice.builder()
                                .productInfoProvidedNoticeType("WEAR")
                                .wear(
                                        NaverProductRequest.ProductInfoProvidedNotice.Wear.builder()
                                                .returnCostReason("0")
                                                .noRefundReason("0")
                                                .qualityAssuranceStandard("0")
                                                .compensationProcedure("0")
                                                .troubleShootingContents("0")
                                                .material("재질 정보")
                                                .color("색 정보")
                                                .size("사이즈 정보")
                                                .manufacturer("Mode On")
                                                .caution("세탁 방법")
                                                .packDate("2000-05")
                                                .warrantyPolicy("품질 보증 기간")
                                                .afterServiceDirector("AS 전화번호")
                                            .build()
                                )
                                .build()

                )
                .afterServiceInfo(
                        NaverProductRequest.AfterServiceInfo.builder()
                                .afterServiceTelephoneNumber("1588-0000")
                                .afterServiceGuideContent("A/S는 고객센터로 문의 바랍니다.")
                                .build()
                )
//                .productSize(
//                        NaverProductRequest.ProductSize.builder()
//                                .sizeTypeNo(100)
//                                .build()
//                )
                .specificProducts(List.of(
                        NaverProductRequest.SpecificProducts.builder()
                                .salePrice(12000)
                                .stockQuantity(11)
                                .standardPurchaseOptions(List.of(
                                        NaverProductRequest.SpecificProducts.StandardPurchaseOptions.builder()
                                                .optionId(5611)
                                                .valueName("골드")
                                                .build(),
                                        NaverProductRequest.SpecificProducts.StandardPurchaseOptions.builder()
                                                .optionId(5831)
                                                .valueName("L")
                                                .build()
                                ))
                                .images(
                                        NaverProductRequest.SpecificProducts.Images.builder()
                                                .representativeImage(
                                                        NaverProductRequest.SpecificProducts.Images.RepresentativeImage.builder()
                                                                .url(imageDto.getImages().get(0).getUrl())
                                                                .build()
                                                )
                                                .optionalImages(
                                                        imageDto.getImages().stream()
                                                                .skip(1) // 0번 대표 이미지 제외
                                                                .map(img -> NaverProductRequest.SpecificProducts.Images.OptionalImages.builder()
                                                                        .url(img.getUrl())
                                                                        .build())
                                                                .collect(Collectors.toList())
                                                )
                                                .build()
                                )
                                .originAreaInfo(
                                        NaverProductRequest.OriginAreaInfo.builder()
                                                .originAreaCode("00")
                                                .build()
                                )
                                .smartstoreChannelProduct(
                                        NaverProductRequest.SpecificProducts.SmartstoreChannelProduct.builder()
                                                .naverShoppingRegistration(false)
                                                .channelProductDisplayStatusType("ON")
                                                .build()
                                )
                                .build()
                ))
                .smartstoreGroupChannel(
                        NaverProductRequest.SmartstoreGroupChannel.builder()
                                .build()
                )
                .originAreaInfo(
                        NaverProductRequest.OriginAreaInfo.builder()
                                .originAreaCode("00")
                                .build()
                )
                .build();

        try {
            // groupProduct로 감싸기
            Map<String, Object> requestBody = Map.of("groupProduct", request);

            String jsonRequest = objectMapper.writeValueAsString(requestBody);
            System.out.println("Sending to Naver:\n" + jsonRequest);

            String response = webClient.post()
                    .uri("/external/v2/standard-group-products")
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(requestBody)
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

            System.out.println("Naver Response:\n" + response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
