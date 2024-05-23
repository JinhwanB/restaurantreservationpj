package com.jh.restaurantreservationpj.reservation.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.reservation.dto.CreateReservationDto;
import com.jh.restaurantreservationpj.reservation.exception.ReservationErrorCode;
import com.jh.restaurantreservationpj.reservation.exception.ReservationException;
import com.jh.restaurantreservationpj.reservation.repository.ReservationRepository;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReservationServiceTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationService reservationService;

    CreateReservationDto.Request createRequest;

    @BeforeEach
    void before() {
        Member manager = Member.builder()
                .userId("manager")
                .userPWD("12345")
                .build();
        memberRepository.save(manager);

        Member member = Member.builder()
                .userPWD("1234")
                .userId("test")
                .build();
        memberRepository.save(member);

        Restaurant restaurant = Restaurant.builder()
                .name("매장")
                .openTime("09")
                .description("설명")
                .totalAddress("주소")
                .manager(manager)
                .closeTime("22")
                .build();
        restaurantRepository.save(restaurant);

        createRequest = CreateReservationDto.Request.builder()
                .time("15")
                .restaurantName("매장")
                .build();
    }

    @Test
    @DisplayName("회원이 예약 생성하는 서비스")
    void createReservation() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);

        assertThat(reservation.getReservationNumber()).isEqualTo("10000000");
        assertThat(reservation.getReservationMemberId()).isEqualTo("test");
    }

    @Test
    @DisplayName("회원이 예약 생성하는 서비스 실패 - 없는 회원")
    void failCreateReservation1() {
        try {
            reservationService.createReservation("ttt", createRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("회원이 예약 생성하는 서비스 실패 - 없는 매장")
    void failCreateReservation2() {
        CreateReservationDto.Request badRequest = createRequest.toBuilder()
                .restaurantName("다른 매장")
                .build();

        try {
            reservationService.createReservation("test", badRequest);
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }

    @Test
    @DisplayName("회원이 예약 생성하는 서비스 실패 - 희망 예약 시간이 매장 오픈 이전이거나 마감 이후인 경우")
    void failCreateReservation3() {
        CreateReservationDto.Request badRequest = createRequest.toBuilder()
                .time("08")
                .build();

        try {
            reservationService.createReservation("test", badRequest);
        } catch (ReservationException e) {
            assertThat(e.getMessage()).isEqualTo(ReservationErrorCode.IMPOSSIBLE_RESERVATION.getMessage());
        }
    }
}