package com.jh.restaurantreservationpj.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    String managerJsonData;
    String memberJsonData;
    MemberSignInDto.Request signInRequest;

    @BeforeEach
    void before() {
        managerJsonData = "{\n" +
                "    \"userId\":\"manager\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"admin\"\n" +
                "    ]\n" +
                "}";

        memberJsonData = "{\n" +
                "    \"userId\":\"test\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"read\", \"write\"\n" +
                "    ]\n" +
                "}";

        signInRequest = MemberSignInDto.Request.builder()
                .userId("test")
                .password("1234")
                .build();
    }

    @Test
    @DisplayName("회원가입 컨트롤러")
    void register() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJsonData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerJsonData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 컨트롤러 실패 - 유효성 검증 실패1")
    void failRegister1() throws Exception {
        memberJsonData = "{\n" +
                "    \"userId\":\"\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"read\", \"write\"\n" +
                "    ]\n" +
                "}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJsonData))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 컨트롤러 실패 - 유효성 검증 실패2")
    void failRegister2() throws Exception {
        memberJsonData = "{\n" +
                "    \"userId\":\"test\",\n" +
                "    \"password\":\"\",\n" +
                "    \"roles\":[\n" +
                "        \"read\", \"write\"\n" +
                "    ]\n" +
                "}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJsonData))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 컨트롤러 실패 - 유효성 검증 실패3")
    void failRegister3() throws Exception {
        memberJsonData = "{\n" +
                "    \"userId\":\"test\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"\"\n" +
                "    ]\n" +
                "}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJsonData))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());

        String memberJsonData2 = "{\n" +
                "    \"userId\":\"test\",\n" +
                "    \"password\":\"1234\",\n" +
                "    \"roles\":[\n" +
                "        \"re\"\n" +
                "    ]\n" +
                "}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(memberJsonData2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 컨트롤러")
    void login() throws Exception {
        register();

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 컨트롤러 실패 - 유효성 검증 실패1")
    void failLogin1() throws Exception {
        register();

        MemberSignInDto.Request badRequest = signInRequest.toBuilder()
                .userId("")
                .build();

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 컨트롤러 실패 - 유효성 검증 실패2")
    void failLogin2() throws Exception {
        register();

        MemberSignInDto.Request badRequest = signInRequest.toBuilder()
                .password("")
                .build();

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].status").value(400))
                .andDo(print());
    }
}