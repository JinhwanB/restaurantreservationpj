package com.jh.restaurantreservationpj.reservation.controller;

import com.jh.restaurantreservationpj.auth.TokenProvider;
import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.reservation.dto.*;
import com.jh.restaurantreservationpj.reservation.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {

    private final ReservationService reservationService;
    private final TokenProvider tokenProvider;

    // 예약 생성 컨트롤러
    @PostMapping("/reservation")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<GlobalResponse<CreateReservationDto.Response>> create(@Valid @RequestBody CreateReservationDto.Request request, HttpServletRequest servletRequest) {
        String memberId = tokenProvider.getUserId(servletRequest);

        CreateReservationDto.Response response = reservationService.createReservation(memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 회원이 예약 취소하는 컨트롤러
    @DeleteMapping("/reservation")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<GlobalResponse<String>> cancel(@Valid @RequestBody CancelReservationDto.Request request, HttpServletRequest servletRequest) {
        String memberId = tokenProvider.getUserId(servletRequest);

        String response = reservationService.cancelReservation(memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 예약 승인 컨트롤러
    @PutMapping("/reservation/{reservationNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<String>> accept(@NotBlank(message = "예약 번호를 입력해주세요.") @Pattern(regexp = "\\d{8}", message = "예약 번호는 8자리의 숫자입니다.") @PathVariable String reservationNumber, HttpServletRequest servletRequest) {
        String managerId = tokenProvider.getUserId(servletRequest);

        String response = reservationService.acceptReservation(managerId, reservationNumber);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 예약 거절 컨트롤러
    @PutMapping("/reservation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<String>> deny(@Valid @RequestBody DenyReservationDto.Request request, HttpServletRequest servletRequest) {
        String managerId = tokenProvider.getUserId(servletRequest);

        String response = reservationService.denyReservation(managerId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 방문 인증 컨트롤러
    @PutMapping("/reservation/visit")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<GlobalResponse<String>> visit(@Valid @RequestBody UseReservationDto.Request request, HttpServletRequest servletRequest) {
        String memberId = tokenProvider.getUserId(servletRequest);

        String response = reservationService.useReservation(memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 예약 상세 조회 컨트롤러
    @GetMapping("/reservation/{reservationNumber}")
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<GlobalResponse<CheckForMemberReservationDto.Response>> check(@NotBlank(message = "예약 번호를 입력해주세요.") @Pattern(regexp = "\\d{8}", message = "예약 번호는 8자리 숫자입니다.") @PathVariable String reservationNumber) {
        CheckForMemberReservationDto.Response response = reservationService.checkReservation(reservationNumber);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 점장이 매장 예약 목록을 조회하는 컨트롤러
    @GetMapping("/{restaurantName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GlobalResponse<Page<CheckForManagerReservationDto.Response>>> checkForManager(@NotBlank(message = "매장 이름을 입력해주세요.") @PathVariable String restaurantName, @PageableDefault(sort = "regDate", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CheckForManagerReservationDto.Response> response = reservationService.checkForManagerReservation(restaurantName.trim(), pageable);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 회원이 예약 목록을 조회하는 컨트롤러
    @GetMapping
    @PreAuthorize("hasRole('READ')")
    public ResponseEntity<GlobalResponse<Page<CheckForMemberReservationDto.Response>>> checkForMember(@PageableDefault(sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable, HttpServletRequest servletRequest) {
        String memberId = tokenProvider.getUserId(servletRequest);

        Page<CheckForMemberReservationDto.Response> response = reservationService.checkForMemberReservation(memberId, pageable);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }
}
