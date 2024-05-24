package com.jh.restaurantreservationpj.reservation.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.reservation.dto.CancelReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.CheckForManagerReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.CreateReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.DenyReservationDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "regDate");

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
                .closeTime("03")
                .build();
        restaurantRepository.save(restaurant);

        createRequest = CreateReservationDto.Request.builder()
                .time("21")
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

    @Test
    @DisplayName("회원이 예약을 취소하는 서비스")
    void cancel() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);

        String reservationNumber = reservation.getReservationNumber();
        CancelReservationDto.Request cancelRequest = CancelReservationDto.Request.builder()
                .reservationNumber(reservationNumber)
                .reason("취소 이유")
                .build();
        String canceledReservationNumber = reservationService.cancelReservation("test", cancelRequest);

        assertThat(reservationNumber).isEqualTo(canceledReservationNumber);
    }

    @Test
    @DisplayName("회원이 예약을 취소하는 서비스 실패 - 없는 회원")
    void failCancel1() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);

        String reservationNumber = reservation.getReservationNumber();
        CancelReservationDto.Request cancelRequest = CancelReservationDto.Request.builder()
                .reservationNumber(reservationNumber)
                .reason("취소 이유")
                .build();
        try {
            reservationService.cancelReservation("tt", cancelRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("회원이 예약을 취소하는 서비스 실패 - 없는 예약")
    void failCancel2() {
        reservationService.createReservation("test", createRequest);

        CancelReservationDto.Request cancelRequest = CancelReservationDto.Request.builder()
                .reservationNumber("10000001")
                .reason("취소 이유")
                .build();
        try {
            reservationService.cancelReservation("test", cancelRequest);
        } catch (ReservationException e) {
            assertThat(e.getMessage()).isEqualTo(ReservationErrorCode.NOT_FOUND_RESERVATION.getMessage());
        }
    }

    @Test
    @DisplayName("회원이 예약을 취소하는 서비스 실패 - 회원 정보와 다름")
    void failCancel3() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);

        Member newMember = Member.builder()
                .userPWD("12345")
                .userId("ttt")
                .build();
        memberRepository.save(newMember);

        String reservationNumber = reservation.getReservationNumber();
        CancelReservationDto.Request cancelRequest = CancelReservationDto.Request.builder()
                .reservationNumber(reservationNumber)
                .reason("취소 이유")
                .build();
        try {
            reservationService.cancelReservation("ttt", cancelRequest);
        } catch (ReservationException e) {
            assertThat(e.getMessage()).isEqualTo(ReservationErrorCode.DIFF_RESERVATION_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("회원이 예약을 취소하는 서비스 실패 - 예약 취소 가능 시간을 넘긴 경우")
    void failCancel7() {
        CreateReservationDto.Request newCreateRequest = createRequest.toBuilder()
                .time("15")
                .build();
        CreateReservationDto.Response reservation = reservationService.createReservation("test", newCreateRequest);
        String reservationNumber = reservation.getReservationNumber();
        CancelReservationDto.Request cancelRequest = CancelReservationDto.Request.builder()
                .reservationNumber(reservationNumber)
                .reason("취소 이유")
                .build();

        try {
            reservationService.cancelReservation("test", cancelRequest);
        } catch (ReservationException e) {
            assertThat(e.getMessage()).isEqualTo(ReservationErrorCode.IMPOSSIBLE_CANCEL.getMessage());
        }
    }

    @Test
    @DisplayName("예약 거절 서비스")
    void deny() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);
        String reservationNumber = reservation.getReservationNumber();

        DenyReservationDto.Request denyRequest = DenyReservationDto.Request.builder()
                .reservationNumber(reservationNumber)
                .reason("예약 거절")
                .build();
        String denyReservationNumber = reservationService.denyReservation("manager", denyRequest);

        assertThat(denyReservationNumber).isEqualTo(reservationNumber);
    }

    @Test
    @DisplayName("예약 거절 서비스 실패 - 없는 관리자")
    void failDeny1() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);
        String reservationNumber = reservation.getReservationNumber();

        DenyReservationDto.Request denyRequest = DenyReservationDto.Request.builder()
                .reservationNumber(reservationNumber)
                .reason("예약 거절")
                .build();
        try {
            reservationService.denyReservation("man", denyRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("예약 거절 서비스 실패 - 없는 예약")
    void failDeny2() {
        reservationService.createReservation("test", createRequest);

        DenyReservationDto.Request denyRequest = DenyReservationDto.Request.builder()
                .reservationNumber("12341234")
                .reason("예약 거절")
                .build();
        try {
            reservationService.denyReservation("manager", denyRequest);
        } catch (ReservationException e) {
            assertThat(e.getMessage()).isEqualTo(ReservationErrorCode.NOT_FOUND_RESERVATION.getMessage());
        }
    }

    @Test
    @DisplayName("예약 거절 서비스 실패 - 예약한 매장의 관리자가 아닌 경우")
    void failDeny3() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);
        String reservationNumber = reservation.getReservationNumber();

        Member newManager = Member.builder()
                .userId("man")
                .userPWD("12345")
                .build();
        memberRepository.save(newManager);

        DenyReservationDto.Request denyRequest = DenyReservationDto.Request.builder()
                .reservationNumber(reservationNumber)
                .reason("예약 거절")
                .build();
        try {
            reservationService.denyReservation("man", denyRequest);
        } catch (ReservationException e) {
            assertThat(e.getMessage()).isEqualTo(ReservationErrorCode.DIFF_RESERVATION_MANAGER.getMessage());
        }
    }

    @Test
    @DisplayName("예약 승인 서비스")
    void accept() {
        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);
        String reservationNumber = reservation.getReservationNumber();

        String acceptedReservationNumber = reservationService.acceptReservation("manager", reservationNumber);

        assertThat(acceptedReservationNumber).isEqualTo(reservationNumber);
    }

    @Test
    @DisplayName("점장이 매장 예약 목록을 확인하는 서비스")
    void restaurantReservations() {
        reservationService.createReservation("test", createRequest);

        Member newMember = Member.builder()
                .userPWD("12345")
                .userId("ttt")
                .build();
        memberRepository.save(newMember);

        reservationService.createReservation("ttt", createRequest);

        Page<CheckForManagerReservationDto.Response> reservationList = reservationService.checkForManagerReservation("매장", pageable);

        assertThat(reservationList.getContent()).hasSize(2);
        assertThat(reservationList.getContent().get(0).getMemberId()).isEqualTo("test");
        assertThat(reservationList.getContent().get(1).getMemberId()).isEqualTo("ttt");
    }

    @Test
    @DisplayName("점장이 매장 예약 목록을 확인하는 서비스 실패 - 없는 매장")
    void failRestaurantReservations() {
        reservationService.createReservation("test", createRequest);

        Member newMember = Member.builder()
                .userPWD("12345")
                .userId("ttt")
                .build();
        memberRepository.save(newMember);

        reservationService.createReservation("ttt", createRequest);

        try {
            reservationService.checkForManagerReservation("매", pageable);
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }
}