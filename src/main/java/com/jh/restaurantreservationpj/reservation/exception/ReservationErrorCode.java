package com.jh.restaurantreservationpj.reservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode {
    ALREADY_USED_RESERVATION(400, "이미 완료(매장 방문)된 예약입니다."),
    ALREADY_CANCELED_RESERVATION(400, "이미 취소된 예약입니다."),
    ALREADY_DENIED_RESERVATION(400, "이미 예약 거절되었습니다."),
    IMPOSSIBLE_CANCEL(400, "예약 취소는 예약한 시간 1시간 전까지만 가능합니다."),
    DIFF_RESERVATION_MEMBER(400, "예약한 회원과 다른 회원입니다."),
    IMPOSSIBLE_RESERVATION(400, "해당 시간에 예약할 수 없습니다."),
    NOT_FOUND_RESERVATION(400, "예약 정보가 없습니다.");

    private final int status;
    private final String message;
}
