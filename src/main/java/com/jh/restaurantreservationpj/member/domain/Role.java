package com.jh.restaurantreservationpj.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 회원 권한 (admin : 관리자(점장), read : 일반 회원(매장 이용자), write : 리뷰 권한)
@Getter
@AllArgsConstructor
public enum Role {
    ROLE_ADMIN("ADMIN"),
    ROLE_READ("READ"),
    ROLE_WRITE("WRITE");

    private final String type;
}
