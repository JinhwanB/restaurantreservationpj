package com.jh.restaurantreservationpj.restaurant.dto;

import com.jh.restaurantreservationpj.validation.NotNullPattern;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 매장 수정 시 dto
public class ModifiedRestaurantDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "매장명은 필수입니다.")
        private String name; // 매장명

        @NotBlank(message = "주소는 필수값입니다.")
        private String totalAddress; // 주소

        private String description; // 매장 설명

        @NotNullPattern
        private String openTime; // 매장 오픈 시간

        @NotNullPattern
        private String closeTime; // 매장 마감 시간
    }
}
