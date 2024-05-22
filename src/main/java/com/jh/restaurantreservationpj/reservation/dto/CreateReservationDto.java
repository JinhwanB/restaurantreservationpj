package com.jh.restaurantreservationpj.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

public class CreateReservationDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "매장 이름은 필수 입력입니다.")
        private String restaurantName; // 예약할 매장 이름

        @NotBlank(message = "희망 예약 시간은 필수 입력입니다.")
        @Pattern(regexp = "\\d{2}", message = "희망 예약 시간은 03, 13과 같은 형식으로 입력해주세요.")
        private String time; // 희망 예약 시간
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Response {

        private String reservationNumber; // 예약 번호
        private String reservationMemberId; // 예약한 회원 아이디
        private String reservationRestaurantName; // 예약한 매장 이름
        private String reservationTime; // 희망 예약 시간
    }
}
