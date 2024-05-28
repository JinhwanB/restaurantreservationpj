package com.jh.restaurantreservationpj.reservation.controller;

import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.reservation.dto.CreateReservationDto;
import com.jh.restaurantreservationpj.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
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
}
