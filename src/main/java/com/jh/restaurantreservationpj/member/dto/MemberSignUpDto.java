package com.jh.restaurantreservationpj.member.dto;

import com.jh.restaurantreservationpj.member.domain.Role;
import com.jh.restaurantreservationpj.validation.Enum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

        @NotEmpty(message = "회원의 권한을 입력해주세요.")
        private List<@Enum(message = "올바른 권한을 입력해주세요.") Role> roles; // 권한
    }
}
