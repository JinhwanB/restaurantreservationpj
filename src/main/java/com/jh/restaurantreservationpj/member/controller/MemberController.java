package com.jh.restaurantreservationpj.member.controller;

import com.jh.restaurantreservationpj.auth.TokenProvider;
import com.jh.restaurantreservationpj.config.GlobalResponse;
import com.jh.restaurantreservationpj.member.dto.MemberSignUpDto;
import com.jh.restaurantreservationpj.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    // 회원가입 컨트롤러
    @PostMapping("/signup")
    public ResponseEntity<GlobalResponse<String>> signUp(MemberSignUpDto.Request request) {
        String response = memberService.register(request);

        return ResponseEntity.ok(GlobalResponse.toGlobalResponse(response));
    }
}
