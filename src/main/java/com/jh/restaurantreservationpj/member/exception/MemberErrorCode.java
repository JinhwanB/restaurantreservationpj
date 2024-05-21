package com.jh.restaurantreservationpj.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberErrorCode {
    NOT_FOUND_MEMBER(400, "회원으로 등록된 아이디가 아닙니다.");

    private final int status;
    private final String message;
}
