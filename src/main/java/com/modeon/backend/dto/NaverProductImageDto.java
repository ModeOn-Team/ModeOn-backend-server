package com.modeon.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class NaverProductImageDto {
    private List<Image> images;

    @Getter
    @Setter
    public static class Image {
        private String url;
    }
}
