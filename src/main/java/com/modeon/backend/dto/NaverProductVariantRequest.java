package com.modeon.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class NaverProductVariantRequest {
    private List<OptionValue> options;
    private String guideId;
    private Integer stock;

    @Data
    public static class OptionValue {
        private String optionId;
        private String optionName;
        private String valueName;

        public OptionValue() {}

        public OptionValue(String optionId, String optionName, String valueName) {
            this.optionId = optionId;
            this.optionName = optionName;
            this.valueName = valueName;
        }
    }
}
