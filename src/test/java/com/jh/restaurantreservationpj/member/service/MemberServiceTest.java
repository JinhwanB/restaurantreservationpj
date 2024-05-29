package com.jh.restaurantreservationpj.member.service;

import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import com.jh.restaurantreservationpj.member.dto.MemberSignUpDto;
import com.jh.restaurantreservationpj.member.exception.MemberErrorCode;
import com.jh.restaurantreservationpj.member.exception.MemberException;
import com.jh.restaurantreservationpj.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    MemberSignUpDto.Request signUpRequest;
    MemberSignInDto.Request signInRequest;

    @BeforeEach
    void before() {
        List<Role> roleList = new ArrayList<>();
        roleList.add(Role.ROLE_ADMIN);

        signUpRequest = MemberSignUpDto.Request.builder()
                .userId("test")
                .password("1234")
                .roles(roleList)
                .build();

        signInRequest = MemberSignInDto.Request.builder()
                .userId("test")
                .password("1234")
                .build();
    }

    @Test
    @DisplayName("회원 가입 서비스")
    void register() {
        String userId = memberService.register(signUpRequest);

        assertThat(userId).isEqualTo("test");
    }

    @Test
    @DisplayName("회원 가입 서비스 실패 - 중복 아이디")
    void failRegister() {
        memberService.register(signUpRequest);

        try {
            memberService.register(signUpRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.ALREADY_EXIST_USERID.getMessage());
        }
    }

    @Test
    @DisplayName("로그인 서비스")
    void login() {
        memberService.register(signUpRequest);
        MemberSignInDto.Response login = memberService.login(signInRequest);

        assertThat(login.getUserId()).isEqualTo("test");
    }

    @Test
    @DisplayName("로그인 서비스 실패 - 없는 회원")
    void failLogin1() {
        memberService.register(signUpRequest);

        MemberSignInDto.Request badRequest = signInRequest.toBuilder()
                .userId("ttt")
                .build();

        try {
            memberService.login(badRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
        }
    }

    @Test
    @DisplayName("로그인 서비스 실패 - 비밀번호가 다름")
    void failLogin2() {
        memberService.register(signUpRequest);

        MemberSignInDto.Request badRequest = signInRequest.toBuilder()
                .password("12345")
                .build();

        try {
            memberService.login(badRequest);
        } catch (MemberException e) {
            assertThat(e.getMessage()).isEqualTo(MemberErrorCode.NOT_MATCH_PASSWORD.getMessage());
        }
    }
}