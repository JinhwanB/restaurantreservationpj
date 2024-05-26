package com.jh.restaurantreservationpj.reservation.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.reservation.domain.Reservation;
import com.jh.restaurantreservationpj.reservation.dto.*;
import com.jh.restaurantreservationpj.reservation.exception.ReservationErrorCode;
import com.jh.restaurantreservationpj.reservation.exception.ReservationException;
import com.jh.restaurantreservationpj.reservation.repository.ReservationRepository;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    static int reservationNum = 10000000;

    static final String AUTO_CANCEL_MESSAGE = "예약 시간이 지나 자동 취소처리 되었습니다.";

    // 회원이 예약 생성하는 서비스
    // 예약은 당일 예약만 가능
    public CreateReservationDto.Response createReservation(String memberId, CreateReservationDto.Request request) {
        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String restaurantName = request.getRestaurantName().trim();
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        // 이미 같은 매장에 진행 중인 예약이 있는 경우
        if (reservationRepository.existsByReservationMemberAndReservationRestaurantAndDelDate(member, restaurant, null)) {
            throw new ReservationException(ReservationErrorCode.ALREADY_EXIST_RESERVATION);
        }

        validReservationTime(restaurant, request.getTime().trim()); // 예약 시간이 가능한 시간인지 확인

        String reservationNumber = makeReservationNumber(String.valueOf(reservationNum)); // 예약 번호

        Reservation reservation = Reservation.builder()
                .reservationNumber(reservationNumber)
                .reservationMember(member)
                .reservationRestaurant(restaurant)
                .reservationTime(request.getTime().trim())
                .isCancel(false)
                .isVisit(false)
                .build();
        Reservation save = reservationRepository.save(reservation);

        return save.toCreateResponse();
    }

    /*
    회원이 예약 취소하는 서비스
    예약 수정은 불가 대신 취소 가능
    취소는 예약 시간 1시간 전까지만 가능
     */
    public String cancelReservation(String memberId, CancelReservationDto.Request request) {
        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String reservationNumber = request.getReservationNumber();
        Reservation reservation = reservationRepository.findByReservationNumberAndDelDate(reservationNumber, null).orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND_RESERVATION));

        if (validUsefulReservation(reservation)) { // 이미 방문 인증 시간이 지난 경우 자동 취소 처리
            throw new ReservationException(ReservationErrorCode.AUTO_CANCEL);
        }

        Member reservationMember = reservation.getReservationMember();
        if (member != reservationMember) { // 예약 정보의 회원과 다른 경우
            throw new ReservationException(ReservationErrorCode.DIFF_RESERVATION_MEMBER);
        }

        // 현재 예약 한 시간 전을 넘겼는지 확인
        LocalDateTime now = LocalDateTime.now(); // 현재 시간
        LocalDateTime reservationTime = stringToTodayLocalDateTime(reservation.getReservationTime()); // 예약 시간
        LocalDateTime beforeOneHour = reservationTime.minusHours(1); // 예약 1시간 전
        if (now.isAfter(beforeOneHour)) {
            throw new ReservationException(ReservationErrorCode.IMPOSSIBLE_CANCEL);
        }

        String reason = request.getReason().trim();
        Reservation canceledReservation = reservation.toBuilder()
                .isCancel(true)
                .isAccept(false)
                .deniedMessage(reason)
                .delDate(LocalDateTime.now())
                .build();
        reservationRepository.save(canceledReservation);

        return canceledReservation.getReservationNumber();
    }

    // 예약 승인 서비스
    public String acceptReservation(String managerId, String reservationNumber) {
        Member manager = memberRepository.findByUserId(managerId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        Reservation reservation = reservationRepository.findByReservationNumberAndDelDate(reservationNumber, null).orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND_RESERVATION));

        if (reservation.getReservationRestaurant().getManager() != manager) { // 예약한 매장의 관리자가 아닌 경우
            throw new ReservationException(ReservationErrorCode.DIFF_RESERVATION_MANAGER);
        }

        if (validUsefulReservation(reservation)) { // 이미 예약한 회원이 방문하지 않아 취소된 경우
            throw new ReservationException(ReservationErrorCode.AUTO_CANCEL);
        }

        Reservation acceptedReservation = reservation.toBuilder()
                .isAccept(true)
                .delDate(LocalDateTime.now())
                .build();
        reservationRepository.save(acceptedReservation);

        return acceptedReservation.getReservationNumber();
    }

    // 예약 거절 서비스
    public String denyReservation(String managerId, DenyReservationDto.Request request) {
        Member manager = memberRepository.findByUserId(managerId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String reservationNumber = request.getReservationNumber();
        Reservation reservation = reservationRepository.findByReservationNumberAndDelDate(reservationNumber, null).orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND_RESERVATION));

        if (reservation.getReservationRestaurant().getManager() != manager) { // 예약한 매장의 관리자가 아닌 경우
            throw new ReservationException(ReservationErrorCode.DIFF_RESERVATION_MANAGER);
        }

        if (validUsefulReservation(reservation)) { // 이미 예약한 회원이 방문하지 않아 취소된 경우
            throw new ReservationException(ReservationErrorCode.AUTO_CANCEL);
        }

        String deniedMessage = request.getReason().trim();
        Reservation deniedReservation = reservation.toBuilder()
                .isAccept(false)
                .deniedMessage(deniedMessage)
                .delDate(LocalDateTime.now())
                .build();
        reservationRepository.save(deniedReservation);

        return deniedReservation.getReservationNumber();
    }

    // 예약 방문 인증 서비스
    public String useReservation(String memberId, UseReservationDto.Request request) {
        String reservationNumber = request.getReservationNumber();
        String restaurantName = request.getRestaurantName().trim();

        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber).orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND_RESERVATION));

        if (validUsefulReservation(reservation)) { // 이미 방문 인증 시간이 지난 경우
            throw new ReservationException(ReservationErrorCode.AUTO_CANCEL);
        }

        if (!Boolean.TRUE.equals(reservation.getIsAccept())) { // 승인된 예약이 아닌 경우
            throw new ReservationException(ReservationErrorCode.IMPOSSIBLE_VISIT);
        }

        if (reservation.getReservationMember() != member) { // 예약한 회원이 아닌 경우
            throw new ReservationException(ReservationErrorCode.DIFF_RESERVATION_MEMBER);
        }

        if (!reservation.getReservationRestaurant().getName().equals(restaurantName)) { // 예약한 매장이 아닌 경우
            throw new ReservationException(ReservationErrorCode.DIFF_RESERVATION_RESTAURANT);
        }

        Reservation visitedReservation = reservation.toBuilder()
                .isVisit(true)
                .build();
        reservationRepository.save(visitedReservation);

        // todo: 회원에게 리뷰 작성 권한 부여하는 로직 필요

        return reservationNumber;
    }

    // 예약 상세 조회 서비스
    public CheckForMemberReservationDto.Response checkReservation(String reservationNumber) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber).orElseThrow(() -> new ReservationException(ReservationErrorCode.NOT_FOUND_RESERVATION));

        return checkForReservation(reservation);
    }

    // 점장이 매장 예약 목록을 확인하는 서비스
    // 페이징처리
    @Transactional(readOnly = true)
    public Page<CheckForManagerReservationDto.Response> checkForManagerReservation(String restaurantName, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        Page<Reservation> reservationList = reservationRepository.findAllByReservationRestaurantAndDelDate(restaurant, null, pageable);
        List<Reservation> content = reservationList.getContent();
        List<CheckForManagerReservationDto.Response> list = content.stream()
                .map(Reservation::toCheckForManagerResponse)
                .toList();

        return new PageImpl<>(list, pageable, list.size());
    }

    /*
    회원이 예약 목록을 조회하는 서비스
    페이징 처리
     */
    public Page<CheckForMemberReservationDto.Response> checkForMemberReservation(String memberId, Pageable pageable) {
        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        Page<Reservation> reservationList = reservationRepository.findAllByReservationMember(member, pageable);
        List<CheckForMemberReservationDto.Response> newList = new ArrayList<>();
        List<Reservation> content = reservationList.getContent();
        for (Reservation reservation : content) {
            CheckForMemberReservationDto.Response response = checkForReservation(reservation);
            newList.add(response);
        }

        return new PageImpl<>(newList, pageable, newList.size());
    }

    // 혹시 모를 예약번호 중복 확인
    private String makeReservationNumber(String reservationNumber) {
        reservationNum++;
        if (reservationNum > 99999999) {
            reservationNum = 10000000;
        }

        // 중복 확인
        if (reservationRepository.existsByReservationNumberAndDelDate(reservationNumber, null)) {
            makeReservationNumber(String.valueOf(reservationNum));
        }

        return reservationNumber;
    }

    // 매장의 오픈 시간과 마감 시간 사이의 시간으로 예약을 했는지 확인
    private void validReservationTime(Restaurant restaurant, String reservationTime) {
        LocalDateTime now = LocalDateTime.now();

        int openTime = Integer.parseInt(restaurant.getOpenTime());
        int closeTime = Integer.parseInt(restaurant.getCloseTime());

        LocalDateTime restaurantOpenTime = stringToTodayLocalDateTime(restaurant.getOpenTime()); // 매장 오픈 시간
        LocalDateTime restaurantCloseTime = openTime > closeTime ? stringToTomorrowLocalDateTime(restaurant.getCloseTime()) : stringToTodayLocalDateTime(restaurant.getCloseTime()); // 매장 마감 시간

        LocalDateTime hopeTime = stringToTodayLocalDateTime(reservationTime); // 희망 예약 시간

        // 예약하고자 하는 시간이 현재 시간보다 이전이거나 매장 오픈시간보다 이전인지 또는 매장 마감시간 이후인지 확인
        if (hopeTime.isBefore(now) || hopeTime.isBefore(restaurantOpenTime) || hopeTime.isAfter(restaurantCloseTime)) {
            throw new ReservationException(ReservationErrorCode.IMPOSSIBLE_RESERVATION);
        }
    }

    // 문자열 시간을 오늘 LocalDateTime으로 변경
    private LocalDateTime stringToTodayLocalDateTime(String time) {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonth().getValue();
        int day = now.getDayOfMonth();

        int stringToTime = Integer.parseInt(time);

        return LocalDateTime.of(year, month, day, stringToTime, 0);
    }

    // 문자열 시간을 다음날 LocalDateTime으로 변경 (13시부터 03시까지 운영의 경우)
    private LocalDateTime stringToTomorrowLocalDateTime(String time) {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        int year = tomorrow.getYear();
        int month = tomorrow.getMonth().getValue();
        int day = tomorrow.getDayOfMonth();

        int stringToTime = Integer.parseInt(time);

        return LocalDateTime.of(year, month, day, stringToTime, 0);
    }

    // 예약이 방문 인증 시간을 이미 지난 예약인지 확인
    private boolean validUsefulReservation(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationTime = stringToTodayLocalDateTime(reservation.getReservationTime());
        LocalDateTime visitTime = reservationTime.minusMinutes(10);

        if (Boolean.TRUE.equals(reservation.getIsAccept()) && !reservation.isVisit() && now.isAfter(visitTime)) { // 이미 방문 인증 시간이 지난 경우 자동 취소 처리
            Reservation autoCancel = reservation.toBuilder()
                    .isAccept(false)
                    .isVisit(false)
                    .isCancel(true)
                    .deniedMessage(AUTO_CANCEL_MESSAGE)
                    .delDate(reservation.getDelDate() != null ? reservation.getDelDate() : LocalDateTime.now())
                    .build();
            reservationRepository.save(autoCancel);

            return true;
        }

        return false;
    }

    // CheckFroMemberReservationDtoResponse로 변경하는 메소드
    private CheckForMemberReservationDto.Response checkForReservation(Reservation reservation) {
        CheckForMemberReservationDto.Response response = reservation.toCheckForMemberResponse();
        CheckForMemberReservationDto.Response result = null;

        if (validUsefulReservation(reservation)) { // 사용 가능한 예약인지 확인
            return response.toBuilder()
                    .detailMessage(CheckForMemberReservationDto.DetailMessage.CANCEL.getMessage())
                    .deniedMessage(AUTO_CANCEL_MESSAGE)
                    .build();
        }

        if (reservation.getDelDate() == null) { // 예약 신청만 되어있는 상태(예약 승인 대기중)
            result = response.toBuilder()
                    .detailMessage(CheckForMemberReservationDto.DetailMessage.WAIT.getMessage())
                    .build();
        } else { // 예약이 처리 결과가 있는 경우 (예: 예약 승인 또는 거절, 취소, 방문 완료 등)
            if (Boolean.TRUE.equals(reservation.getIsAccept())) { // 승인된 예약
                result = response.toBuilder()
                        .detailMessage(CheckForMemberReservationDto.DetailMessage.ACCEPT.getMessage())
                        .build();
            } else if (Boolean.FALSE.equals(reservation.getIsAccept())) { // 거절된 예약
                result = response.toBuilder()
                        .deniedMessage(CheckForMemberReservationDto.DetailMessage.DENY.getMessage())
                        .deniedMessage(reservation.getDeniedMessage())
                        .build();
            } else if (reservation.isVisit()) { // 방문 완료한 예약
                result = response.toBuilder()
                        .detailMessage(CheckForMemberReservationDto.DetailMessage.VISIT.getMessage())
                        .build();
            } else if (reservation.isCancel()) { // 취소된 예약
                result = response.toBuilder()
                        .detailMessage(CheckForMemberReservationDto.DetailMessage.CANCEL.getMessage())
                        .deniedMessage(reservation.getDeniedMessage())
                        .build();
            }
        }

        return result;
    }
}
