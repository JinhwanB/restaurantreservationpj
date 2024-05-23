package com.jh.restaurantreservationpj.reservation.repository;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.reservation.domain.Reservation;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
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
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MemberRepository memberRepository;

    Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "regDate"));

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

        Reservation reservation = Reservation.builder()
                .reservationNumber("12341234")
                .reservationMember(member)
                .reservationRestaurant(restaurant)
                .reservationTime("13")
                .build();
        reservationRepository.save(reservation);
    }

    @Test
    @DisplayName("예약 번호를 통해 예약 찾기")
    void findByReservationNumber() {
        Reservation reservation = reservationRepository.findByReservationNumber("12341234").orElse(null);

        assertThat(reservation.getReservationMember().getUserId()).isEqualTo("test");
    }

    @Test
    @DisplayName("매장에 해당하는 예약 리스트 중 삭제되지 않은 리스트를 페이징처리하여 가져오기")
    void reservationList() {
        Member secondMember = Member.builder()
                .userId("test2")
                .userPWD("3333")
                .build();
        Member second = memberRepository.save(secondMember);

        Restaurant restaurant1 = restaurantRepository.findByName("매장").orElse(null);

        Reservation reservation2 = Reservation.builder()
                .reservationNumber("12341235")
                .reservationMember(second)
                .reservationRestaurant(restaurant1)
                .reservationTime("15")
                .build();
        reservationRepository.save(reservation2);

        Page<Reservation> reservationList = reservationRepository.findAllByReservationRestaurantAndDelDate(restaurant1, null, pageable);

        assertThat(reservationList.getContent().get(0).getReservationNumber()).isEqualTo("12341234");
        assertThat(reservationList.getContent().get(1).getReservationNumber()).isEqualTo("12341235");
    }

    @Test
    @DisplayName("삭제되지 않은 예약 중 예약 번호 존재하는지 확인")
    void existReservationNumber() {
        assertThat(reservationRepository.existsByReservationNumberAndDelDate("12341234", null)).isEqualTo(true);
    }
}