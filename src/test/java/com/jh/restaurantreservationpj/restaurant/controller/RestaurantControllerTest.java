package com.jh.restaurantreservationpj.restaurant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.restaurantreservationpj.member.domain.Member;
import com.jh.restaurantreservationpj.member.domain.MemberRole;
import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import com.jh.restaurantreservationpj.restaurant.dto.CreateRestaurantDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class RestaurantControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    CreateRestaurantDto.Request request;

    @BeforeEach
    void before(){
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

        request = CreateRestaurantDto.Request.builder()
                .userId("test")
                .name("매장 이름")
                .description("설명")
                .openTime("08")
                .closeTime("22")
                .totalAddress("주소")
                .build();
    }

    @Test
    @DisplayName("매장 등록 컨트롤러")
    void register() throws Exception {
        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("매장 등록 컨트롤러 실패 - 유효성 검증 실패 1")
    void failRegister1() throws Exception {
        CreateRestaurantDto.Request modifiedRequest = request.toBuilder()
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
        CreateRestaurantDto.Request modifiedRequest = request.toBuilder()
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
        CreateRestaurantDto.Request modifiedRequest = request.toBuilder()
                .openTime(null)
                .build();

        mockMvc.perform(post("/restaurants/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(modifiedRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}