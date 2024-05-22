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
}