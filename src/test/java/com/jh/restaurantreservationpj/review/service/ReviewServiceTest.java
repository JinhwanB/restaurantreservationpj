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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    CreateReviewDto.Request createRequest;
    ModifyReviewDto.Request modifyRequest;
    Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "regDate");

    @BeforeEach
    void before() {
        Member member = Member.builder()
                .userPWD("1234")
                .userId("test")
                .build();
        memberRepository.save(member);

        Member manager = Member.builder()
                .userPWD("1234")
                .userId("manager")
                .build();
        Member savedManager = memberRepository.save(manager);

        Restaurant restaurant = Restaurant.builder()
                .name("매장")
                .closeTime("22")
                .description("설명")
                .manager(savedManager)
                .totalAddress("주소")
                .openTime("09")
                .build();
        restaurantRepository.save(restaurant);

        createRequest = CreateReviewDto.Request.builder()
                .title("제목")
                .content("내용")
                .restaurantName("매장")
                .build();

        modifyRequest = ModifyReviewDto.Request.builder()
                .title("제목2")
                .content("내용2")
                .build();
    }

    @Test
    @DisplayName("리뷰 생성 서비스")
    void create() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        assertThat(review.getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("리뷰 생성 서비스 실패 - 없는 회원")
    void failCreate1() {
        try {
            reviewService.createReview("ttt", createRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 생성 서비스 실패 - 없는 매장")
    void failCreate2() {
        CreateReviewDto.Request badRequest = createRequest.toBuilder()
                .restaurantName("매")
                .build();

        try {
            reviewService.createReview("test", badRequest);
        } catch (RestaurantException e) {
            assertThat(e.getMessage()).isEqualTo(RestaurantErrorCode.NOT_FOUND_RESTAURANT.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 수정 서비스")
    void modify() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        ModifyReviewDto.Response modifyReview = reviewService.modifyReview(review.getId(), "test", modifyRequest);

        assertThat(modifyReview.getContent()).isEqualTo("내용2");
    }

    @Test
    @DisplayName("리뷰 수정 서비스 실패 - 없는 리뷰")
    void failModify1() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        try {
            reviewService.modifyReview(review.getId() + 1, "test", modifyRequest);
        } catch (ReviewException e) {
            assertThat(e.getMessage()).isEqualTo(ReviewErrorCode.NOT_FOUND_REVIEW.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 수정 서비스 실패 - 없는 회원")
    void failModify2() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        try {
            reviewService.modifyReview(review.getId(), "ttt", modifyRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 수정 서비스 실패 - 리뷰 작성자가 아닌 경우")
    void failModify3() {
        Member newMember = Member.builder()
                .userId("ttt")
                .userPWD("12345")
                .build();
        memberRepository.save(newMember);

        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        try {
            reviewService.modifyReview(review.getId(), "ttt", modifyRequest);
        } catch (ReviewException e) {
            assertThat(e.getMessage()).isEqualTo(ReviewErrorCode.DIFF_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 삭제 서비스")
    void delete() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        Long id = reviewService.deleteReview(review.getId(), "test");
        Review deleted = reviewRepository.findById(review.getId()).orElse(null);

        assertThat(id).isEqualTo(review.getId());
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("리뷰 삭제 서지스 실패 - 없는 회원")
    void failDelete1() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        try {
            reviewService.deleteReview(review.getId(), "ttt");
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 삭제 서지스 실패 - 없는 리뷰")
    void failDelete2() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        try {
            reviewService.deleteReview(review.getId() + 1, "test");
        } catch (ReviewException e) {
            assertThat(e.getMessage()).isEqualTo(ReviewErrorCode.NOT_FOUND_REVIEW.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 삭제 서지스 실패 - 리뷰 작성자가 아닌 경우")
    void failDelete3() {
        Member newMember = Member.builder()
                .userPWD("12345")
                .userId("ttt")
                .build();
        memberRepository.save(newMember);

        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        try {
            reviewService.deleteReview(review.getId(), "ttt");
        } catch (ReviewException e) {
            assertThat(e.getMessage()).isEqualTo(ReviewErrorCode.DIFF_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("리뷰 상세 조회 서비스")
    void detail() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        CheckReviewDto.Response checked = reviewService.checkReview(review.getId());

        assertThat(checked.getContent()).isEqualTo(review.getContent());
    }

    @Test
    @DisplayName("리뷰 상세 조회 서비스 - 없는 리뷰")
    void failDetail() {
        CreateReviewDto.Response review = reviewService.createReview("test", createRequest);

        try {
            reviewService.checkReview(review.getId() + 1);
        } catch (ReviewException e) {
            assertThat(e.getMessage()).isEqualTo(ReviewErrorCode.NOT_FOUND_REVIEW.getMessage());
        }
    }
}