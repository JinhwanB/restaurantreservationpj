package com.jh.restaurantreservationpj.reservation.service;

import com.jh.restaurantreservationpj.reservation.domain.Reservation;
import com.jh.restaurantreservationpj.reservation.dto.CheckReservationDto;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;

    // 점장이 매장 예약 목록을 확인하는 서비스
    // 페이징처리
    @Transactional(readOnly = true)
    public Page<CheckReservationDto.Response> checkReservation(String restaurantName, Pageable pageable) {
        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        Page<Reservation> reservationList = reservationRepository.findAllByReservationRestaurantAndDelDate(restaurant, null, pageable);
        List<Reservation> content = reservationList.getContent();
        List<CheckReservationDto.Response> list = content.stream()
                .map(Reservation::toCheckResponse)
                .toList();

        return new PageImpl<>(list, pageable, list.size());
    }
}
