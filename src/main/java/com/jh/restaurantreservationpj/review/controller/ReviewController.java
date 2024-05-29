package com.jh.restaurantreservationpj.review.controller;

import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.review.dto.CheckReviewDto;
import com.jh.restaurantreservationpj.review.dto.CreateReviewDto;
import com.jh.restaurantreservationpj.review.dto.ModifyReviewDto;
import com.jh.restaurantreservationpj.review.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<GlobalResponse<ModifyReviewDto.Response>> modify(@NotBlank(message = "수정할 리뷰 pk를 입력해주세요") @Positive(message = "pk에 음수는 허용되지 않습니다.") @PathVariable Long id, @Valid @RequestBody ModifyReviewDto.Request request) {

        String memberId = null;
        ModifyReviewDto.Response response = reviewService.modifyReview(id, memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 리뷰 삭제 컨트롤러
    @DeleteMapping("/review/{id}")
    public ResponseEntity<GlobalResponse<Long>> delete(@NotBlank(message = "삭제할 리뷰 pk를 입력해주세요.") @Positive(message = "pk에 음수는 허용되지 않습니다.") @PathVariable Long id) {

        String memberId = null;
        Long response = reviewService.deleteReview(id, memberId);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 리뷰 상세 조회 컨트롤러
    @GetMapping("/review/{id}")
    public ResponseEntity<GlobalResponse<CheckReviewDto.Response>> check(@NotBlank(message = "조회할 리뷰 pk를 입력해주세요.") @Positive(message = "pk에 음수는 허용되지 않습니다.") @PathVariable Long id) {

        CheckReviewDto.Response response = reviewService.checkReview(id);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 리뷰 전체 리스트 조회 컨트롤러
    @GetMapping
    public ResponseEntity<GlobalResponse<Page<CheckReviewDto.Response>>> list(@PageableDefault(sort = "regDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CheckReviewDto.Response> response = reviewService.checkReviewList(pageable);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }
}
