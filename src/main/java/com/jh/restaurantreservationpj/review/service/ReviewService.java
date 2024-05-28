package com.jh.restaurantreservationpj.review.service;

import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.domain.Restaurant;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import com.jh.restaurantreservationpj.restaurant.repository.RestaurantRepository;
import com.jh.restaurantreservationpj.review.domain.Review;
import com.jh.restaurantreservationpj.review.dto.CheckReviewDto;
import com.jh.restaurantreservationpj.review.dto.CreateReviewDto;
import com.jh.restaurantreservationpj.review.dto.ModifyReviewDto;
import com.jh.restaurantreservationpj.review.exception.ReviewErrorCode;
import com.jh.restaurantreservationpj.review.exception.ReviewException;
import com.jh.restaurantreservationpj.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final MemberRepository memberRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    // 리뷰 생성 서비스
    public CreateReviewDto.Response createReview(String memberId, CreateReviewDto.Request request) {

        String restaurantName = request.getRestaurantName().trim();

        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        Restaurant restaurant = restaurantRepository.findByName(restaurantName).orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND_RESTAURANT));

        Review review = Review.builder()
                .member(member)
                .restaurant(restaurant)
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        Review savedReview = reviewRepository.save(review);

        return savedReview.toCreateResponse();
    }

    // 리뷰 수정 서비스
    public ModifyReviewDto.Response modifyReview(Long id, String memberId, ModifyReviewDto.Request request) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ReviewException(ReviewErrorCode.NOT_FOUND_REVIEW));

        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        if (review.getMember() != member) { // 작성자가 다른 경우
            throw new ReviewException(ReviewErrorCode.DIFF_MEMBER);
        }

        Review modify = review.toBuilder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();
        Review modifiedReview = reviewRepository.save(modify);

        return modifiedReview.toModifyResponse();
    }

    // 리뷰 삭제 서비스
    public Long deleteReview(Long id, String memberId) {

        Member member = memberRepository.findByUserId(memberId).orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        Review review = reviewRepository.findById(id).orElseThrow(() -> new ReviewException(ReviewErrorCode.NOT_FOUND_REVIEW));

        // todo: 관리자가 아닌 경우에만 실행하도록 추가 조건 필요
        if (review.getMember() != member) { // 관리자가 아닐 때 리뷰를 작성한 회원이 아닌 경우
            throw new ReviewException(ReviewErrorCode.DIFF_MEMBER);
        }

        reviewRepository.delete(review);

        return id;
    }

    // 리뷰 상세 조회 서비스
    public CheckReviewDto.Response checkReview(Long id) {

        Review review = reviewRepository.findById(id).orElseThrow(() -> new ReviewException(ReviewErrorCode.NOT_FOUND_REVIEW));

        return review.toCheckResponse();
    }
}
