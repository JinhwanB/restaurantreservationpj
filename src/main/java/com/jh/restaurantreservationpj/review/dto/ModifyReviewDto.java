package com.jh.restaurantreservationpj.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 리뷰 수정 시 dto
public class ModifyReviewDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "리뷰 제목을 입력해주세요.")
        private String title; // 제목

        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        private String content; // 내용
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Response {

        private Long id; // 리뷰 pk
        private String title; // 제목
        private String content; // 내용
    }
}
