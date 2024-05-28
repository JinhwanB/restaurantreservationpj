package com.jh.restaurantreservationpj.review.repository;

import com.jh.restaurantreservationpj.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
}
