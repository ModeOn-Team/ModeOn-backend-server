// NaverProductRequest.java
package com.modeon.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class NaverProductRequest {

    private String leafCategoryId;      // 리프 카테고리 ID
    private String name;      // 상품명
    private Integer guideId; // 판매 옵션 가이드
    private Boolean minorPurchasable; // 미성년자 구매 가능 여부
    private String commonDetailContent; // 그룹 상품 공통 상품 상세 정보

    private ProductInfoProvidedNotice productInfoProvidedNotice; // 필수
    private AfterServiceInfo afterServiceInfo;
//    private ProductSize productSize;
    private List<SpecificProducts> specificProducts;
    private OriginAreaInfo originAreaInfo;
    private SmartstoreGroupChannel smartstoreGroupChannel;

    @Data
    @Builder
    public static class ProductInfoProvidedNotice{
        private String productInfoProvidedNoticeType; // 품정보제공고시 상품군 유형, WEAR
        private Wear wear;

        @Data
        @Builder
        public static class Wear {
            private String returnCostReason;
            private String noRefundReason;
            private String qualityAssuranceStandard;
            private String compensationProcedure;
            private String troubleShootingContents;

            private String material;
            private String color;
            private String size;
            private String manufacturer;
            private String caution;
            private String packDate;
            private String warrantyPolicy;
            private String afterServiceDirector;
        }
    }

    @Data
    @Builder
    public static class AfterServiceInfo{
        private String afterServiceTelephoneNumber; // A/S 전화번호
        private String afterServiceGuideContent; // A/S 안내
    }

//    @Data
//    @Builder
//    public static class ProductSize{
//        private Integer sizeTypeNo; // 사이즈 타입 번호
//        @Data
//        @Builder
//        public static class SizeAttributes { // 사이즈 상세 정보
//            private String name; // 판매 옵션 ID
//            @Data
//            @Builder
//            public static class SizeValues {
//                private Integer sizeValueTypeNo; // 판매 옵션 ID
//                private Number value; // 판매 옵션 값 명
//            }
//        }
//    }

    @Data
    @Builder
    public static class SpecificProducts{ // 그룹 상품
        private List<StandardPurchaseOptions> standardPurchaseOptions;
        private Images images;
        private Integer stockQuantity;
        private OriginAreaInfo originAreaInfo;
        private SmartstoreChannelProduct smartstoreChannelProduct;

        @Data
        @Builder
        public static class StandardPurchaseOptions { // 판매 옵션 정보
            private Integer optionId; // 판매 옵션 ID
            private String valueName; // 판매 옵션 값 명
        }

        private Number salePrice;

        @Data
        @Builder
        public static class SmartstoreChannelProduct {
            private Boolean naverShoppingRegistration;
            private String channelProductDisplayStatusType;

        }

        @Data
        @Builder
        public static class Images {
            private RepresentativeImage representativeImage;
            private List<OptionalImages> optionalImages;

            @Data
            @Builder
            public static class RepresentativeImage {
                private String url;
            }

            @Data
            @Builder
            public static class OptionalImages {
                private String url;
            }
        }
    }


    @Data
    @Builder
    public static class SmartstoreGroupChannel {
        private Integer bbsSeq;
    }


    @Data
    @Builder
    public static class OriginAreaInfo
    {
        private String originAreaCode;
    }


}
