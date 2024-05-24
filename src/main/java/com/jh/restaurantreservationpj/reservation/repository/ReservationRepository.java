package com.jh.restaurantreservationpj.reservation.repository;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.reservation.domain.Reservation;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationNumber(String reservationNumber); // 예약 번호를 통해 예약 찾기

    Optional<Reservation> findByReservationNumberAndDelDate(String reservationNumber, LocalDateTime delDate); // 어떠한 경로로든 처리되지 않은 예약 찾기

    Page<Reservation> findAllByReservationRestaurantAndDelDate(Restaurant restaurant, LocalDateTime delDate, Pageable pageable); // 매장에 해당하는 예약 리스트 중 삭제되지 않은 리스트를 페이징처리하여 가져오기

    boolean existsByReservationNumberAndDelDate(String reservationNumber, LocalDateTime delDate); // 삭제되지 않은 예약 중 예약 번호의 중복 여부

    boolean existsByReservationMemberAndReservationRestaurantAndDelDate(Member reservationMember, Restaurant reservationRestaurant, LocalDateTime delDate); // 이미 진행 중인 예약이 있는지 확인하는 쿼리 메소드
}
