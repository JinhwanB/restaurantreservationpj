package com.jh.restaurantreservationpj.restaurant.repository;

import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    boolean existsByName(String name); // 매장 이름이 존재하는지 여부 확인 쿼리 메소드

    Optional<Restaurant> findByName(String name); // 매장 이름으로 엔티티 찾기

    Page<Restaurant> findAllByNameStartingWithIgnoreCaseOrNameContainingIgnoreCaseOrderByNameAsc(String prefix1, String prefix2); // 검색한 문자로 시작하거나 검색한 문자를 포함하는 매장을 오름차순으로 정렬하여 가져오는 쿼리 메소드
}
