package com.jh.restaurantreservationpj.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

// 회원가입 시 dto
public class MemberSignUpDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Builder(toBuilder = true)
    public static class Request {

        @NotBlank(message = "아이디를 입력해주세요.")
        private String userId; // 아이디

        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password; // 비밀번호

        private List<String> roles; // 권한
    }
}
