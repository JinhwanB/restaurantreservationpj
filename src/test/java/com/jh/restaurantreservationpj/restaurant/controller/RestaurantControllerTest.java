package com.jh.restaurantreservationpj.restaurant.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.ModifiedRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.exception.RestaurantErrorCode;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class RestaurantControllerTest {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    CreateRestaurantDto.Request createRequest;
    ModifiedRestaurantDto.Request modifyRequest;
    String managerToken;
    String userToken;

    @BeforeEach
    void before() throws Exception {
        createRequest = CreateRestaurantDto.Request.builder()
                .name("매장 이름")
                .description("설명")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

        modifyRequest = ModifiedRestaurantDto.Request.builder()
                .name("매장 이름2")
                .description("설명2")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

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

        String contentAsString = managerMvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString);
        managerToken = jsonNode.get("data").asText();

        String contentAsString2 = userMvcResult.getResponse().getContentAsString();
        JsonNode jsonNode2 = objectMapper.readTree(contentAsString2);
        userToken = jsonNode2.get("data").asText();
    }

    @Test
    @DisplayName("매장 등록 컨트롤러")
    void register() throws Exception {
        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("매장 등록 컨트롤러 실패 - 유효성 검증 실패 1")
    void failRegister1() throws Exception {
        CreateRestaurantDto.Request modifiedRequest = createRequest.toBuilder()
                .name(null)
                .build();

        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifiedRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 등록 컨트롤러 실패 - 유효성 검증 실패 2")
    void failRegister2() throws Exception {
        CreateRestaurantDto.Request modifiedRequest = createRequest.toBuilder()
                .openTime("088")
                .build();

        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifiedRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 등록 컨트롤러 실패 - 유효성 검증 실패 3")
    void failRegister3() throws Exception {
        CreateRestaurantDto.Request modifiedRequest = createRequest.toBuilder()
                .openTime(null)
                .build();

        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifiedRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("매장 등록 컨트롤러 실패 - 권한 없음(로그인 x)")
    void failRegister4() throws Exception {
        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("매장 등록 컨트롤러 실패 - 권한 없음(회원)")
    void failRegister5() throws Exception {
        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러")
    void modify() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 유효성 검증 실패 1")
    void failModify1() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(put("/restaurants/restaurant/ ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 유효성 검증 실패 2")
    void failModify2() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        ModifiedRestaurantDto.Request badRequest = modifyRequest.toBuilder()
                .name(null)
                .build();

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 유효성 검증 실패 3")
    void failModify3() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        ModifiedRestaurantDto.Request badRequest = modifyRequest.toBuilder()
                .openTime(null)
                .build();

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest))
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(RestaurantErrorCode.NOT_VALID_ARGS_OF_OPEN_TIME_AND_CLOSE_TIME.getMessage()));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 권한 없음(로그인 x)")
    void failModify4() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 권한 없음(로그인 o)")
    void failModify5() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("매장 삭제 컨트롤러")
    void del() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(delete("/restaurants/restaurant/매장 이름")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("매장 삭제 컨트롤러 실패 - 유효성 검증 실패")
    void failDelete1() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(delete("/restaurants/restaurant/ ")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 삭제 컨트롤러 실패 - 권한 없음(로그인 x)")
    void failDelete2() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(delete("/restaurants/restaurant/매장 이름"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("매장 삭제 컨트롤러 실패 - 권한 없음(로그인 o)")
    void failDelete3() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(delete("/restaurants/restaurant/매장 이름")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("매장 검색 컨트롤러")
    void search() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        CreateRestaurantDto.Request secondRequest = createRequest.toBuilder()
                .name("매가")
                .build();
        restaurantService.createRestaurant("manager", secondRequest);

        mockMvc.perform(get("/restaurants/search?word=매"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data.content[0].name").value("매가"));
    }

    @Test
    @DisplayName("매장 검색 컨트롤러 실패 - 검색어 입력 안함")
    void failSearch() throws Exception {
        mockMvc.perform(get("/restaurants/search?word= "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 상세 조회 컨트롤러")
    void check() throws Exception {
        restaurantService.createRestaurant("manager", createRequest);

        mockMvc.perform(get("/restaurants/restaurant/매장 이름"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("매장 상세 조회 컨트롤러 실패 - 유효성 검증 실패")
    void failCheck() throws Exception {
        mockMvc.perform(get("/restaurants/restaurant/ "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }
}