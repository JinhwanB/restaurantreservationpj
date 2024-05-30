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

    private static final String NOT_LOGIN = "로그인이 필요합니다.";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        setResponse(response);
    }

    private void setResponse(HttpServletResponse response) throws IOException {
        log.error("로그인 에러");

        response.setStatus(401);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");

        ObjectMapper objectMapper = new ObjectMapper();
        GlobalResponse<Object> result = GlobalResponse.toGlobalResponseFail(401, NOT_LOGIN);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
