package com.jh.restaurantreservationpj.config;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
// 전체 api 응답 형식 통일화
public class GlobalResponse<T> {
    private int status; // HTTP 응답 코드
    private String message; // 응답 관련 메시지
    private T data; // 응답으로 받은 데이터

    // 응답 성공 시
    public static <T> GlobalResponse<T> toGlobalResponse(T data){
        return GlobalResponse.<T>builder()
                .status(200)
                .message("성공")
                .data(data)
                .build();
    }

    // 응답 실패 시
    public static <T> GlobalResponse<T> toGlobalResponseFail(int status, String message){
        return GlobalResponse.<T>builder()
                .status(status)
                .message(message)
                .build();
    }
}
