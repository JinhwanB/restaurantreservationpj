package com.jh.restaurantreservationpj.reservation.repository;

import com.jh.restaurantreservationpj.reservation.domain.Reservation;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationNumber(String reservationNumber); // 예약 번호를 통해 예약 찾기

    List<Reservation> findAllByReservationRestaurant(Restaurant restaurant); // 매장에 해당하는 예약 리스트 가져오기
}
