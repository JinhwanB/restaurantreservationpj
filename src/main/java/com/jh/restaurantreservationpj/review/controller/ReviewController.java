package com.jh.restaurantreservationpj.review.controller;

import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.review.dto.CreateReviewDto;
import com.jh.restaurantreservationpj.review.dto.ModifyReviewDto;
import com.jh.restaurantreservationpj.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Validated
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

    // 리뷰 수정 컨트롤러
    @PutMapping("/review/{id}")
    public ResponseEntity<GlobalResponse<ModifyReviewDto.Response>> modify(@NotBlank(message = "리뷰 pk를 입력해주세요") @Positive(message = "pk에 음수는 허용되지 않습니다.") @PathVariable Long id, @Valid @RequestBody ModifyReviewDto.Request request) {

        String memberId = null;
        ModifyReviewDto.Response response = reviewService.modifyReview(id, memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }
}
