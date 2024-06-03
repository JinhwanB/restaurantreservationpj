package com.jh.restaurantreservationpj.reservation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import com.jh.restaurantreservationpj.reservation.dto.CancelReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.CreateReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.DenyReservationDto;
import com.jh.restaurantreservationpj.reservation.dto.UseReservationDto;
import com.jh.restaurantreservationpj.reservation.service.ReservationService;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.service.RestaurantService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class ReservationControllerTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    String userToken;
    String managerToken;
    CreateReservationDto.Request createRequest;
    CancelReservationDto.Request cancelRequest;
    DenyReservationDto.Request denyRequest;
    UseReservationDto.Request useRequest;

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

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerJsonData))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJsonData))
                .andExpect(status().isOk());

        MemberSignInDto.Request loginRequest = MemberSignInDto.Request.builder()
                .userId("manager")
                .password("1234")
                .build();

        MvcResult managerMvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        MemberSignInDto.Request userRequest = loginRequest.toBuilder()
                .userId("test")
                .build();

        MvcResult userMvcResult = mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = managerMvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString);
        managerToken = jsonNode.get("data").asText();

        String contentAsString2 = userMvcResult.getResponse().getContentAsString();
        JsonNode jsonNode2 = objectMapper.readTree(contentAsString2);
        userToken = jsonNode2.get("data").asText();

        CreateRestaurantDto.Request restaurantCreateRequest = CreateRestaurantDto.Request.builder()
                .name("매장")
                .description("설명")
                .totalAddress("주소")
                .openTime("09")
                .closeTime("23")
                .build();

        restaurantService.createRestaurant("manager", restaurantCreateRequest);

        createRequest = CreateReservationDto.Request.builder()
                .time("19")
                .restaurantName("매장")
                .build();

        CreateReservationDto.Response reservation = reservationService.createReservation("test", createRequest);

        cancelRequest = CancelReservationDto.Request.builder()
                .reservationNumber(reservation.getReservationNumber())
                .reason("취소 이유")
                .build();

        denyRequest = DenyReservationDto.Request.builder()
                .reservationNumber(reservation.getReservationNumber())
                .reason("거절 이유")
                .build();

        useRequest = UseReservationDto.Request.builder()
                .userId("test")
                .reservationNumber(reservation.getReservationNumber())
                .restaurantName("매장")
                .build();
    }

    @Test
    @DisplayName("예약 생성 컨트롤러")
    void create() throws Exception {
        CreateRestaurantDto.Request restaurantCreateRequest = CreateRestaurantDto.Request.builder()
                .name("매장2")
                .description("설명")
                .totalAddress("주소")
                .openTime("09")
                .closeTime("23")
                .build();
        restaurantService.createRestaurant("manager", restaurantCreateRequest);

        CreateReservationDto.Request newCreateRequest = createRequest.toBuilder()
                .restaurantName("매장2")
                .build();

        mockMvc.perform(post("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCreateRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("예약 생성 컨트롤러 실패 - 유효성 검증 실패1")
    void failCreate1() throws Exception {
        CreateReservationDto.Request newCreateRequest = createRequest.toBuilder()
                .restaurantName("")
                .build();

        mockMvc.perform(post("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCreateRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("예약 생성 컨트롤러 실패 - 유효성 검증 실패2")
    void failCreate2() throws Exception {
        CreateReservationDto.Request newCreateRequest = createRequest.toBuilder()
                .time("")
                .build();

        mockMvc.perform(post("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCreateRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("예약 생성 컨트롤러 실패 - 로그인 x")
    void failCreate3() throws Exception {
        CreateRestaurantDto.Request restaurantCreateRequest = CreateRestaurantDto.Request.builder()
                .name("매장2")
                .description("설명")
                .totalAddress("주소")
                .openTime("09")
                .closeTime("23")
                .build();
        restaurantService.createRestaurant("manager", restaurantCreateRequest);

        CreateReservationDto.Request newCreateRequest = createRequest.toBuilder()
                .restaurantName("매장2")
                .build();

        mockMvc.perform(post("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCreateRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("예약 생성 컨트롤러 실패 - 권한 없음")
    void failCreate4() throws Exception {
        CreateRestaurantDto.Request restaurantCreateRequest = CreateRestaurantDto.Request.builder()
                .name("매장2")
                .description("설명")
                .totalAddress("주소")
                .openTime("09")
                .closeTime("23")
                .build();
        restaurantService.createRestaurant("manager", restaurantCreateRequest);

        CreateReservationDto.Request newCreateRequest = createRequest.toBuilder()
                .restaurantName("매장2")
                .build();

        mockMvc.perform(post("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCreateRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("회원 예약 취소 컨트롤러")
    void cancel() throws Exception {
        mockMvc.perform(delete("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("회원 예약 취소 컨트롤러 실패 - 유효성 검증 실패1")
    void failCancel1() throws Exception {
        CancelReservationDto.Request badRequest = cancelRequest.toBuilder()
                .reservationNumber("")
                .build();

        mockMvc.perform(delete("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());

        CancelReservationDto.Request badRequest2 = badRequest.toBuilder()
                .reservationNumber("10")
                .build();

        mockMvc.perform(delete("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest2))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 예약 취소 컨트롤러 실패 - 유효성 검증 실패2")
    void failCancel2() throws Exception {
        CancelReservationDto.Request badRequest = cancelRequest.toBuilder()
                .reason("")
                .build();

        mockMvc.perform(delete("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 예약 취소 컨트롤러 실패 - 로그인 x")
    void failCancel3() throws Exception {
        mockMvc.perform(delete("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 예약 취소 컨트롤러 실패 - 권한 없음")
    void failCancel4() throws Exception {
        mockMvc.perform(delete("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 승인 컨트롤러")
    void accept() throws Exception {
        mockMvc.perform(put("/reservations/reservation/10000000")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("예약 승인 컨트롤러 실패 - 예약 번호 입력 x (올바르지 않은 경로)")
    void failAccept1() throws Exception {
        mockMvc.perform(put("/reservations/reservation/")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 승인 컨트롤러 실패 - 유효성 검증 실패")
    void failAccept2() throws Exception {
        mockMvc.perform(put("/reservations/reservation/ ")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());

        mockMvc.perform(put("/reservations/reservation/10")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 승인 컨트롤러 실패 - 로그인 x")
    void failAccept3() throws Exception {
        mockMvc.perform(put("/reservations/reservation/10000000"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 승인 컨트롤러 실패 - 권한 없음")
    void failAccept4() throws Exception {
        mockMvc.perform(put("/reservations/reservation/10000000")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 거절 컨트롤러")
    void deny() throws Exception {
        mockMvc.perform(put("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("예약 거절 컨트롤러 실패 - 유효성 검증 실패1")
    void failDeny1() throws Exception {
        DenyReservationDto.Request badRequest = denyRequest.toBuilder()
                .reservationNumber("")
                .build();

        mockMvc.perform(put("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());

        DenyReservationDto.Request badRequest2 = denyRequest.toBuilder()
                .reservationNumber("10")
                .build();

        mockMvc.perform(put("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest2))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 거절 컨트롤러 실패 - 유효성 검증 실패2")
    void failDeny2() throws Exception {
        DenyReservationDto.Request badRequest = denyRequest.toBuilder()
                .reason("")
                .build();

        mockMvc.perform(put("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 거절 컨트롤러 실패 - 로그인 x")
    void failDeny3() throws Exception {
        mockMvc.perform(put("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 거절 컨트롤러 실패 - 권한 없음")
    void failDeny4() throws Exception {
        mockMvc.perform(put("/reservations/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(denyRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andDo(print());
    }

    @Test
    @DisplayName("방문 인증 컨트롤러")
    void visit() throws Exception {
        reservationService.acceptReservation("manager", "10000000");

        mockMvc.perform(put("/reservations/reservation/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(useRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("방문 인증 컨트롤러 실패 - 유효성 검증 실패1")
    void failVisit1() throws Exception {
        reservationService.acceptReservation("manager", "10000000");

        UseReservationDto.Request badRequest = useRequest.toBuilder()
                .restaurantName("")
                .build();
        mockMvc.perform(put("/reservations/reservation/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("방문 인증 컨트롤러 실패 - 유효성 검증 실패2")
    void failVisit2() throws Exception {
        reservationService.acceptReservation("manager", "10000000");

        UseReservationDto.Request badRequest = useRequest.toBuilder()
                .reservationNumber("")
                .build();
        mockMvc.perform(put("/reservations/reservation/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());

        UseReservationDto.Request badRequest2 = useRequest.toBuilder()
                .reservationNumber("10")
                .build();
        mockMvc.perform(put("/reservations/reservation/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("방문 인증 컨트롤러 실패 - 유효성 검증 실패3")
    void failVisit3() throws Exception {
        reservationService.acceptReservation("manager", "10000000");

        UseReservationDto.Request badRequest = useRequest.toBuilder()
                .userId("")
                .build();
        mockMvc.perform(put("/reservations/reservation/visit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 상세 조회 컨트롤러")
    void detail() throws Exception {
        mockMvc.perform(get("/reservations/reservation/search/10000000")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());

        mockMvc.perform(get("/reservations/reservation/search/10000000")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("예약 상세 조회 컨트롤러 실패 - 예약 번호 입력 x (올바르지 않은 경로)")
    void failDetail1() throws Exception {
        mockMvc.perform(get("/reservations/reservation/search/")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 상세 조회 컨트롤러 실패 - 유효성 검증 실패")
    void failDetail2() throws Exception {
        mockMvc.perform(get("/reservations/reservation/search/ ")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());

        mockMvc.perform(get("/reservations/reservation/search/10")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("예약 상세 조회 컨트롤러 실패 - 로그인 x")
    void failDetail3() throws Exception {
        mockMvc.perform(get("/reservations/reservation/search/10000000"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andDo(print());
    }

    @Test
    @DisplayName("점장이 매장 예약 목록 조회하는 컨트롤러")
    void managerReservations() throws Exception {
        mockMvc.perform(get("/reservations/search/매장")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("점장이 매장 예약 목록 조회하는 컨트롤러 실패 - 매장 이름 입력 x (올바르지 않은 경로)")
    void failManagerReservations1() throws Exception {
        mockMvc.perform(get("/reservations/search/")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andDo(print());
    }

    @Test
    @DisplayName("점장이 매장 예약 목록 조회하는 컨트롤러 실패 - 유효성 검증 실패")
    void failManagerReservations2() throws Exception {
        mockMvc.perform(get("/reservations/search/ ")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("점장이 매장 예약 목록 조회하는 컨트롤러 실패 - 로그인 x")
    void failManagerReservations3() throws Exception {
        mockMvc.perform(get("/reservations/search/매장"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andDo(print());
    }

    @Test
    @DisplayName("점장이 매장 예약 목록 조회하는 컨트롤러 실패 - 권한 없음")
    void failManagerReservations4() throws Exception {
        mockMvc.perform(get("/reservations/search/매장")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andDo(print());
    }

    @Test
    @DisplayName("회원이 예약 목록 조회하는 컨트롤러")
    void userReservations() throws Exception {
        mockMvc.perform(get("/reservations/search")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("회원이 예약 목록 조회하는 컨트롤러 실패 - 로그인 x")
    void failUserReservations1() throws Exception {
        mockMvc.perform(get("/reservations/search"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andDo(print());
    }

    @Test
    @DisplayName("회원이 예약 목록 조회하는 컨트롤러 실패 - 권한 없음")
    void failUserReservations2() throws Exception {
        mockMvc.perform(get("/reservations/search")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andDo(print());
    }
}