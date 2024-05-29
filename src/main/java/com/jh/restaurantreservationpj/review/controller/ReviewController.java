package com.jh.restaurantreservationpj.review.controller;

import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.review.dto.CreateReviewDto;
import com.jh.restaurantreservationpj.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
// todo: memberId부분 헤더의 토큰 정보로 가져오는 로직 추가
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성 컨트롤러
    @PostMapping("/review")
    public ResponseEntity<GlobalResponse<CreateReviewDto.Response>> create(@Valid @RequestBody CreateReviewDto.Request request) {
        
        String memberId = null;
        CreateReviewDto.Response response = reviewService.createReview(memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }
}
