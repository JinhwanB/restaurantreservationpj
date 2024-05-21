package com.jh.restaurantreservationpj.restaurant.dto;

import lombok.*;

// 매장 조회 시 dto
public class CheckRestaurantDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Response{
        private String name; // 매장명
        private String totalAddress; // 주소
        private String description; // 매장 설명
        private String openTime; // 오픈 시간
        private String closeTime; // 마감 시간
    }
}
