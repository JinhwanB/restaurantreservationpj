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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ReviewServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MockMvc mockMvc;

    CreateReviewDto.Request createRequest;
    ModifyReviewDto.Request modifyRequest;
    Pageable pageable = PageRequest.of(0, 10, Sort.Direction.DESC, "regDate");

    @BeforeEach
    void before() throws Exception {
        String managerJsonData = "{\n" +
                "    \"userId\":\"manager\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"admin\"\n" +
                "    ]\n" +
                "}";

        String memberJsonData = "{\n" +
                "    \"userId\":\"test\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"read\", \"write\"\n" +
                "    ]\n" +
                "}";

        String anotherMemberJsonData = "{\n" +
                "    \"userId\":\"ttt\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"read\", \"write\"\n" +
                "    ]\n" +
                "}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerJsonData))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJsonData))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(anotherMemberJsonData))
                .andExpect(status().isOk());

        Member manager = memberRepository.findByUserId("manager").orElse(null);

        Restaurant restaurant = Restaurant.builder()
                .name("매장")
                .closeTime("22")
                .description("설명")
                .manager(manager)
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

    @Test
    @DisplayName("리뷰 전체 리스트 조회 서비스")
    void list() {
        CreateReviewDto.Request newCreateRequest = createRequest.toBuilder()
                .title("제목2")
                .content("내용2")
                .build();
        reviewService.createReview("test", createRequest);
        reviewService.createReview("test", newCreateRequest);

        Page<CheckReviewDto.Response> responses = reviewService.checkReviewList(pageable);

        assertThat(responses.getTotalElements()).isEqualTo(2);
        assertThat(responses.getContent().get(0).getContent()).isEqualTo("내용2");
    }
}