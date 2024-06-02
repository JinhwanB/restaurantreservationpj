package com.jh.restaurantreservationpj.review.controller;

import com.jh.restaurantreservationpj.auth.TokenProvider;
import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.review.dto.CheckReviewDto;
import com.jh.restaurantreservationpj.review.dto.CreateReviewDto;
import com.jh.restaurantreservationpj.review.dto.ModifyReviewDto;
import com.jh.restaurantreservationpj.review.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {

    private final ReviewService reviewService;
    private final TokenProvider tokenProvider;

    // 리뷰 생성 컨트롤러
    @PostMapping("/review")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<GlobalResponse<CreateReviewDto.Response>> create(@Valid @RequestBody CreateReviewDto.Request request, HttpServletRequest servletRequest) {

        String memberId = tokenProvider.getUserId(servletRequest);
        CreateReviewDto.Response response = reviewService.createReview(memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 리뷰 수정 컨트롤러
    @PutMapping("/review/{id}")
    @PreAuthorize("hasRole('WRITE')")
    public ResponseEntity<GlobalResponse<ModifyReviewDto.Response>> modify(@Positive(message = "pk에 음수는 허용되지 않습니다.") @PathVariable Long id, @Valid @RequestBody ModifyReviewDto.Request request, HttpServletRequest servletRequest) {

        String memberId = tokenProvider.getUserId(servletRequest);
        ModifyReviewDto.Response response = reviewService.modifyReview(id, memberId, request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 리뷰 삭제 컨트롤러
    @DeleteMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WRITE')")
    public ResponseEntity<GlobalResponse<Long>> delete(@Positive(message = "pk에 음수는 허용되지 않습니다.") @PathVariable Long id, HttpServletRequest servletRequest) {

        String memberId = tokenProvider.getUserId(servletRequest);
        Long response = reviewService.deleteReview(id, memberId);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 리뷰 상세 조회 컨트롤러
    @GetMapping("/review/{id}")
    public ResponseEntity<GlobalResponse<CheckReviewDto.Response>> check(@Positive(message = "pk에 음수는 허용되지 않습니다.") @PathVariable Long id) {

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
