package com.jh.restaurantreservationpj.reservation.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.reservation.domain.Reservation;
import com.jh.restaurantreservationpj.reservation.dto.CheckForManagerReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.CreateReservationDto;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    static int reservationNum = 10000000;

    // 회원이 예약 생성하는 서비스
    public CreateReservationDto.Response createReservation(String memberId, CreateReservationDto.Request request) {
        String reservationNumber = makeReservationNumber(String.valueOf(reservationNum));

        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        String restaurantName = request.getRestaurantName().trim();
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));
        validReservationTime(restaurant, request.getTime().trim()); // 예약 시간이 가능한 시간인지 확인

        Reservation reservation = Reservation.builder()
                .reservationNumber(reservationNumber)
                .reservationMember(member)
                .reservationRestaurant(restaurant)
                .reservationTime(request.getTime().trim())
                .build();
        Reservation save = reservationRepository.save(reservation);

        return save.toCreateResponse();
    }

    // 점장이 매장 예약 목록을 확인하는 서비스
    // 페이징처리
    @Transactional(readOnly = true)
    public Page<CheckForManagerReservationDto.Response> checkReservation(String restaurantName, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        Page<Reservation> reservationList = reservationRepository.findAllByReservationRestaurantAndDelDate(restaurant, null, pageable);
        List<Reservation> content = reservationList.getContent();
        List<CheckForManagerReservationDto.Response> list = content.stream()
                .map(Reservation::toCheckForManagerResponse)
                .toList();

        return new PageImpl<>(list, pageable, list.size());
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
        int year = now.getYear();
        int month = now.getMonth().getValue();
        int day = now.getDayOfMonth();

        int openTime = Integer.parseInt(restaurant.getOpenTime());
        int closeTime = Integer.parseInt(restaurant.getCloseTime());
        LocalDateTime restaurantOpenTime = LocalDateTime.of(year, month, day, openTime, 0); // 매장 오픈 시간
        LocalDateTime restaurantCloseTime = LocalDateTime.of(year, month, day, closeTime, 0); // 매장 마감 시간

        int timeOfReservation = Integer.parseInt(reservationTime);
        LocalDateTime hopeTime = LocalDateTime.of(year, month, day, timeOfReservation, 0); // 희망 예약 시간

        // 예약하고자 하는 시간이 현재 시간보다 이전이거나 매장 오픈시간보다 이전인지 또는 매장 마감시간 이후인지 확인
        if (hopeTime.isBefore(now) || hopeTime.isBefore(restaurantOpenTime) || hopeTime.isAfter(restaurantCloseTime)) {
            throw new ReservationException(ReservationErrorCode.IMPOSSIBLE_RESERVATION);
        }
    }
}