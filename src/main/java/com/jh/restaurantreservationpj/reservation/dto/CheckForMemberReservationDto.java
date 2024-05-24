package com.jh.restaurantreservationpj.reservation.dto;

import lombok.*;

// 회원이 예약 조회 시 관련된 dto
public class CheckForMemberReservationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Response {

        private String reservationNumber; // 예약 번호
        private String restaurantName; // 매장 이름
        private String reservationTime; // 예약 시간
        private String detailMessage; // 예약 진행 상황 메시지
        private String deniedMessage; // 취소 또는 승인 거절 이유
    }

    @Getter
    @AllArgsConstructor
    public enum DetailMessage {
        VISIT("매장에 방문하셨습니다. 후기 작성이 가능합니다."),
        CANCEL("취소된 예약입니다."),
        DENY("예약이 거절되었습니다."),
        ACCEPT("예약이 승인되었습니다. 예약 시간 10분전까지 매장에 도착하여 방문인증을 진행해 주세요.");

        private final String message;
    }
}
