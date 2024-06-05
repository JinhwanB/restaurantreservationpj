package com.jh.restaurantreservationpj.reservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode {
    AUTO_CANCEL(400, "예약한 시간에 매장에 방문하지 않아 자동 취소처리 되었습니다."),
    ALREADY_EXIST_RESERVATION(400, "이미 대기중인 예약이 있습니다. 다른 시간으로 예약하시려면 기존 예약 취소 후 이용해주세요."),
    ALREADY_USED_RESERVATION(400, "이미 완료(매장 방문)된 예약입니다."),
    ALREADY_CANCELED_RESERVATION(400, "이미 취소된 예약입니다."),
    ALREADY_DENIED_RESERVATION(400, "이미 예약 거절되었습니다."),
    DIFF_RESERVATION_MANAGER(400, "예약한 식당의 관리자와 다른 관리자입니다."),
    DIFF_RESERVATION_MEMBER(400, "예약한 회원과 다른 회원입니다."),
    DIFF_RESERVATION_RESTAURANT(400, "예약한 매장과 다른 매장입니다."),
    IMPOSSIBLE_RESERVATION(400, "해당 시간에 예약할 수 없습니다."),
    IMPOSSIBLE_RESERVATION_FOR_DENIED(400, "이미 거절된 예약 시간입니다."),
    IMPOSSIBLE_CANCEL(400, "예약 취소는 예약한 시간 1시간 전까지만 가능합니다."),
    IMPOSSIBLE_VISIT(400, "현재 예약이 승인되지 않았거나 취소된 예약입니다."),
    NOT_FOUND_RESERVATION(400, "예약 정보가 없습니다.");

    private final int status;
    private final String message;
}
