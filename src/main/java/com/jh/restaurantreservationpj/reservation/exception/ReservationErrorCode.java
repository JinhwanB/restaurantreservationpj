package com.jh.restaurantreservationpj.reservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode {
    DIFF_RESERVATION_MEMBER(400, "예약한 회원과 다른 회원입니다."),
    IMPOSSIBLE_RESERVATION(400, "해당 시간에 예약할 수 없습니다."),
    NOT_FOUND_RESERVATION(400, "예약 정보가 없습니다.");

    private final int status;
    private final String message;
}
