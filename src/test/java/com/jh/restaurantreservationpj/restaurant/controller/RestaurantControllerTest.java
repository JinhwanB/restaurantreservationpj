package com.jh.restaurantreservationpj.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.domain.MemberRole;
import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import com.jh.restaurantreservationpj.restaurant.dto.DeleteRestaurantDto;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class RestaurantControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    CreateRestaurantDto.Request createRequest;
    ModifiedRestaurantDto.Request modifyRequest;
    DeleteRestaurantDto.Request deleteRequest;

    @BeforeEach
    void before() {
        MemberRole memberRole = MemberRole.builder()
                .role(Role.ROLE_ADMIN)
                .build();

        List<MemberRole> list = new ArrayList<>();
        list.add(memberRole);

        Member member = Member.builder()
                .userId("test")
                .userPWD("1234")
                .memberRoles(list)
                .build();

        memberRepository.save(member);

        createRequest = CreateRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .description("설명")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

        modifyRequest = ModifiedRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .description("설명2")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();

        deleteRequest = DeleteRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .build();
    }

    @Test
    @DisplayName("매장 등록 컨트롤러")
    void register() throws Exception {
        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
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
                        .content(objectMapper.writeValueAsString(modifiedRequest)))
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
                        .content(objectMapper.writeValueAsString(modifiedRequest)))
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
                        .content(objectMapper.writeValueAsString(modifiedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러")
    void modify() throws Exception {
        restaurantService.createRestaurant(createRequest);

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 유효성 검증 실패 1")
    void failModify1() throws Exception {
        restaurantService.createRestaurant(createRequest);

        mockMvc.perform(put("/restaurants/restaurant/ ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 유효성 검증 실패 2")
    void failModify2() throws Exception {
        restaurantService.createRestaurant(createRequest);

        ModifiedRestaurantDto.Request badRequest = modifyRequest.toBuilder()
                .name(null)
                .build();

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 수정 컨트롤러 실패 - 유효성 검증 실패 3")
    void failModify3() throws Exception {
        restaurantService.createRestaurant(createRequest);

        ModifiedRestaurantDto.Request badRequest = modifyRequest.toBuilder()
                .openTime(null)
                .build();

        mockMvc.perform(put("/restaurants/restaurant/매장 이름")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(RestaurantErrorCode.NOT_VALID_ARGS_OF_OPEN_TIME_AND_CLOSE_TIME.getMessage()));
    }

    @Test
    @DisplayName("매장 삭제 컨트롤러")
    void delete() throws Exception {
        restaurantService.createRestaurant(createRequest);

        mockMvc.perform(MockMvcRequestBuilders.delete("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("매장 삭제 컨트롤러 실패 - 유효성 검증 실패")
    void failDelete() throws Exception {
        restaurantService.createRestaurant(createRequest);

        DeleteRestaurantDto.Request badRequest = deleteRequest.toBuilder()
                .name(null)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.delete("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400));
    }

    @Test
    @DisplayName("매장 검색 컨트롤러")
    void search() throws Exception {
        restaurantService.createRestaurant(createRequest);

        CreateRestaurantDto.Request secondRequest = createRequest.toBuilder()
                .name("매가")
                .build();
        restaurantService.createRestaurant(secondRequest);

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
}