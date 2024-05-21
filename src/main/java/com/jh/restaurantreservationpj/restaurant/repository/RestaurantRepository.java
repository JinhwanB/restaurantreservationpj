package com.jh.restaurantreservationpj.restaurant.repository;

import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    boolean existsByName(String name); // 매장 이름이 존재하는지 여부 확인 쿼리 메소드

    Optional<Restaurant> findByName(String name); // 매장 이름으로 엔티티 찾기
}
