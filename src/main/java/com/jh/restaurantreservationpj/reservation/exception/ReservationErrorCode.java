package com.jh.restaurantreservationpj.reservation.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode {
    NOT_FOUND_RESERVATION(400, "예약 정보가 없습니다.");
    
    private final int status;
    private final String message;
}
