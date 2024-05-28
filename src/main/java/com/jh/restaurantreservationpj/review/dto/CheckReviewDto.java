package com.jh.restaurantreservationpj.review.dto;

import lombok.*;

// 리뷰 조회 시 dto
public class CheckReviewDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Response {

        private String memberId; // 작성자
        private String restaurantName; // 매장 이름
        private String title; // 제목
        private String content; // 내용
    }
}
