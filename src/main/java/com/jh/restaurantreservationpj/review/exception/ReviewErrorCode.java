package com.jh.restaurantreservationpj.review.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode {
    DIFF_MEMBER(400, "리뷰의 작성자가 아닙니다."),
    DIFF_MANAGER(400, "리뷰가 작성된 매장의 관리자가 아닙니다."),
    NOT_FOUND_REVIEW(400, "존재하지 않는 리뷰입니다.");

    private final int status;
    private final String message;
}
