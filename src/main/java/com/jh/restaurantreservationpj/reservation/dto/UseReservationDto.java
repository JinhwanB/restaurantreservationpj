package com.jh.restaurantreservationpj.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

// 방문 인증 시 dto
public class UseReservationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    static public class Request {

        @NotBlank(message = "예약 번호를 입력해주세요.")
        @Pattern(regexp = "\\d{8}", message = "예약 번호는 8자리 숫자입니다.")
        private String reservationNumber; // 예약 번호

        @NotBlank(message = "식당 이름을 입력해주세요.")
        private String restaurantName; // 예약한 식당 이름
    }
}
