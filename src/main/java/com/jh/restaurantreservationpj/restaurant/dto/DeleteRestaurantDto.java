package com.jh.restaurantreservationpj.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class DeleteRestaurantDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank
        private String userId; // 회원 아이디

        @NotBlank
        private String name; // 매장 이름
    }
}
