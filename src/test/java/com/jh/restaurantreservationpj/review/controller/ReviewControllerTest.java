package com.jh.restaurantreservationpj.review.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.service.RestaurantService;
import com.jh.restaurantreservationpj.review.dto.CreateReviewDto;
import com.jh.restaurantreservationpj.review.dto.ModifyReviewDto;
import com.jh.restaurantreservationpj.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    String userToken;
    String managerToken;
    String commonUserToken;
    CreateReviewDto.Request createRequest;
    ModifyReviewDto.Request modifyRequest;

    @BeforeEach
    void before() throws Exception {
        String managerJsonData = "{\n" +
                "    \"userId\":\"manager\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"admin\"\n" +
                "    ]\n" +
                "}";

        String userJsonData = "{\n" +
                "    \"userId\":\"test\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"read\", \"write\"\n" +
                "    ]\n" +
                "}";

        String commonUserJsonData = "{\n" +
                "    \"userId\":\"test1\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"read\"\n" +
                "    ]\n" +
                "}";

        MemberSignInDto.Request loginRequest = MemberSignInDto.Request.builder()
                .userId("manager")
                .password("1234")
                .build();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerJsonData))
                .andExpect(status().isOk());

        MvcResult managerMvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJsonData))
                .andExpect(status().isOk());

        MemberSignInDto.Request userRequest = loginRequest.toBuilder()
                .userId("test")
                .build();

        MvcResult userMvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commonUserJsonData))
                .andExpect(status().isOk());

        MemberSignInDto.Request commonUserRequest = loginRequest.toBuilder()
                .userId("test1")
                .build();

        MvcResult commonUserMvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commonUserRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = managerMvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString);
        managerToken = jsonNode.get("data").asText();

        String contentAsString2 = userMvcResult.getResponse().getContentAsString();
        JsonNode jsonNode2 = objectMapper.readTree(contentAsString2);
        userToken = jsonNode2.get("data").asText();

        String contentAsString3 = commonUserMvcResult.getResponse().getContentAsString();
        JsonNode jsonNode3 = objectMapper.readTree(contentAsString3);
        commonUserToken = jsonNode3.get("data").asText();

        CreateRestaurantDto.Request restaurantCreateRequest = CreateRestaurantDto.Request.builder()
                .name("매장")
                .description("설명")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

        restaurantService.createRestaurant("manager", restaurantCreateRequest);

        createRequest = CreateReviewDto.Request.builder()
                .title("제목")
                .content("내용")
                .restaurantName("매장")
                .build();

        modifyRequest = ModifyReviewDto.Request.builder()
                .content("내용2")
                .title("제목2")
                .build();
    }

    @Test
    @DisplayName("리뷰 생성 컨트롤러")
    void create() throws Exception {
        mockMvc.perform(post("/reviews/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("리뷰 생성 컨트롤러 실패 - 유효성 검증 실패1")
    void failCreate1() throws Exception {
        CreateReviewDto.Request badRequest = createRequest.toBuilder()
                .title("")
                .build();

        mockMvc.perform(post("/reviews/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 생성 컨트롤러 실패 - 유효성 검증 실패2")
    void failCreate2() throws Exception {
        CreateReviewDto.Request badRequest = createRequest.toBuilder()
                .content("")
                .build();

        mockMvc.perform(post("/reviews/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 생성 컨트롤러 실패 - 유효성 검증 실패3")
    void failCreate3() throws Exception {
        CreateReviewDto.Request badRequest = createRequest.toBuilder()
                .restaurantName("")
                .build();

        mockMvc.perform(post("/reviews/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 생성 컨트롤러 실패 - 로그인 x")
    void failCreate4() throws Exception {
        mockMvc.perform(post("/reviews/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("리뷰 생성 컨트롤러 실패 - 권한 없음")
    void failCreate5() throws Exception {
        mockMvc.perform(post("/reviews/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("리뷰 수정 컨트롤러")
    void modify() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(put("/reviews/review/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("리뷰 수정 컨트롤러 실패 - PathVariable 입력 x (올바르지 않은 경로)")
    void failModify1() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(put("/reviews/review/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("리뷰 수정 컨트롤러 실패 - 유효성 검증 실패1")
    void failModify2() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(put("/reviews/review/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 수정 컨트롤러 실패 - 유효성 검증 실패2")
    void failModify3() throws Exception {
        reviewService.createReview("test", createRequest);

        ModifyReviewDto.Request badRequest = modifyRequest.toBuilder()
                .title("")
                .build();

        mockMvc.perform(put("/reviews/review/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 수정 컨트롤러 실패 - 유효성 검증 실패3")
    void failModify4() throws Exception {
        reviewService.createReview("test", createRequest);

        ModifyReviewDto.Request badRequest = modifyRequest.toBuilder()
                .content("")
                .build();

        mockMvc.perform(put("/reviews/review/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 수정 컨트롤러 실패 - 로그인 x")
    void failModify5() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(put("/reviews/review/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("리뷰 수정 컨트롤러 실패 - 권한 없음")
    void failModify6() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(put("/reviews/review/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("회원이 리뷰 삭제 컨트롤러")
    void del() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(delete("/reviews/review/1")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("관리자가 리뷰 삭제 컨트롤러")
    void del2() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(delete("/reviews/review/1")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("리뷰 삭제 컨트롤러 실패 - id 입력 안함 (올바르지 않은 경로)")
    void failDel1() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(delete("/reviews/review/")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("리뷰 삭제 컨트롤러 실패 - 유효성 검증 실패")
    void failDel2() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(delete("/reviews/review/0")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 삭제 컨트롤러 실패 - 로그인 x")
    void failDel3() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(delete("/reviews/review/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("리뷰 삭제 컨트롤러 실패 - 권한 없음")
    void failDel4() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(delete("/reviews/review/1")
                        .header("Authorization", "Bearer " + commonUserToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("리뷰 상세 조회 컨트롤러")
    void detail() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(get("/reviews/review/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("리뷰 상세 조회 컨트롤러 실패 - id 입력 안함 (올바르지 않은 경로)")
    void failDetail1() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(get("/reviews/search/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("리뷰 상세 조회 컨트롤러 실패 - 유효성 검증 실패")
    void failDetail2() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(get("/reviews/search/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("리뷰 전체 리스트 조회 컨트롤러")
    void all() throws Exception {
        reviewService.createReview("test", createRequest);

        mockMvc.perform(get("/reviews/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
}