package com.jh.restaurantreservationpj.member.controller;

import com.jh.restaurantreservationpj.auth.TokenProvider;
import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.member.dto.MemberSignInDto;
import com.jh.restaurantreservationpj.member.dto.MemberSignUpDto;
import com.jh.restaurantreservationpj.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    // 회원가입 컨트롤러
    @PostMapping("/signup")
    public ResponseEntity<GlobalResponse<String>> signUp(@Valid @RequestBody MemberSignUpDto.Request request) {
        String response = memberService.register(request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }

    // 로그인 컨트롤러
    @PostMapping("/signin")
    public ResponseEntity<GlobalResponse<String>> signIn(@Valid @RequestBody MemberSignInDto.Request request) {
        MemberSignInDto.Response loginMember = memberService.login(request);

        String userId = loginMember.getUserId();
        List<String> roles = loginMember.getRoles();
        String token = tokenProvider.generateToken(userId, roles);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(token));
    }
}
