package com.jh.restaurantreservationpj.review.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode {
    NOT_FOUND_REVIEW(400, "존재하지 않는 리뷰입니다.");

    private final int status;
    private final String message;
}
