package com.jh.restaurantreservationpj.reservation.dto;

import lombok.*;

public class CheckReservationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Response {
        private String reservationNumber; // 예약 번호
        private String memberId; // 예약한 회원 아이디
        private String restaurantName; // 예약한 식당
        private String reservationTime; // 희망 예약 시간
    }
}
