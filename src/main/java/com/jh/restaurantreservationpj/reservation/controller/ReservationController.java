package com.jh.restaurantreservationpj.reservation.controller;

import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.reservation.dto.CancelReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.CreateReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.DenyReservationDto;
import com.jh.restaurantreservationpj.reservation.service.ReservationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Validated
// todo: 헤더 정보의 토큰을 통해 회원 아이디를 가져오는 로직 필요
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성 컨트롤러
    @PostMapping("/reservation")
    public ResponseEntity<GlobalResponse<CreateReservationDto.Response>> create(@Valid @RequestBody CreateReservationDto.Request request) {
        String memberId = null;

        CreateReservationDto.Response response = reservationService.createReservation(memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 회원이 예약 취소하는 컨트롤러
    @DeleteMapping("/reservation")
    public ResponseEntity<GlobalResponse<String>> cancel(@Valid @RequestBody CancelReservationDto.Request request) {
        String memberId = null;

        String response = reservationService.cancelReservation(memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 예약 승인 컨트롤러
    @PutMapping("/reservation/{reservationNumber}")
    public ResponseEntity<GlobalResponse<String>> accept(@NotBlank(message = "예약 번호를 입력해주세요.") @Pattern(regexp = "\\d{8}", message = "예약 번호는 8자리의 숫자입니다.") @PathVariable String reservationNumber) {
        String managerId = null;

        String response = reservationService.acceptReservation(managerId, reservationNumber);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 예약 거절 컨트롤러
    @PutMapping("/reservation")
    public ResponseEntity<GlobalResponse<String>> deny(@Valid @RequestBody DenyReservationDto.Request request) {
        String managerId = null;

        String response = reservationService.denyReservation(managerId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }
}
