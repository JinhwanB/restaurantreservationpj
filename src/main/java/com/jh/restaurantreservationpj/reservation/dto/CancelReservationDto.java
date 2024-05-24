package com.jh.restaurantreservationpj.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

public class CancelReservationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "예약 번호는 필수 입력입니다.")
        @Pattern(regexp = "\\d{8}", message = "예약 번호는 8자리의 숫자입니다.")
        private String reservationNumber; // 예약 번호

        @NotBlank(message = "취소 사유를 입력해주세요.")
        private String reason; // 취소 사유
    }
}
