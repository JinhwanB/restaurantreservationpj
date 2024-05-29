package com.jh.restaurantreservationpj.member.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// 회원 권한 (admin : 관리자(점장), read : 일반 회원(매장 이용자), write : 리뷰 권한)
@Getter
@AllArgsConstructor
public enum Role {
    ROLE_ADMIN("ADMIN"),
    ROLE_READ("READ"),
    ROLE_WRITE("WRITE");

    private final String name;

    // Enum 검증을 위한 코드, Enum에 속하지 않으면 null 리턴
    @JsonCreator
    private static Role fromRole(String value) {
        return Arrays.stream(Role.values())
                .filter(r -> r.getName().equals(value.toUpperCase()))
                .findAny()
                .orElse(null);
    }
}
