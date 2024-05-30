package com.jh.restaurantreservationpj.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.restaurantreservationpj.config.GlobalResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

// 403에러 핸들러
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String NOT_AUTH = "권한이 없습니다. 로그인 및 권한을 확인해주세요.";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        setResponse(response);
    }

    private void setResponse(HttpServletResponse response) throws IOException {
        log.error("인증 에러");

        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");

        ObjectMapper objectMapper = new ObjectMapper();
        GlobalResponse<Object> result = GlobalResponse.toGlobalResponseFail(403, NOT_AUTH);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
