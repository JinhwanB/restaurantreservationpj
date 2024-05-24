package com.jh.restaurantreservationpj.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

// 예약 거절 관련 dto
public class DenyReservationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "예약 번호를 입력해주세요.")
        @Pattern(regexp = "\\d{8}", message = "예약 번호는 8자리 숫자입니다.")
        private String reservationNumber; // 예약 번호

        @NotBlank(message = "예약 거절 사유를 입력해주세요.")
        private String reason; // 예약 거절 사유
    }
}
