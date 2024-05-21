package com.jh.restaurantreservationpj.restaurant.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RestaurantErrorCode {
    NOT_FOUND_RESTAURANT(400, "등록되지 않은 매장입니다."),
    ALREADY_EXIST_NAME(400, "이미 존재하는 매장입니다."),
    NOT_VALID_ARGS_OF_OPEN_TIME_AND_CLOSE_TIME(400, "오픈 시간과 마감 시간은 둘 다 null이거나 둘 다 유효한 값이 있어야 합니다.");

    private final int status;
    private final String message;
}
