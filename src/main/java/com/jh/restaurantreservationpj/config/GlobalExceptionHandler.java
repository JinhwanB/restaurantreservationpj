package com.jh.restaurantreservationpj.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 유효성 검증 에러 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<List<GlobalResponse<?>>> handleValidException(MethodArgumentNotValidException e){
        log.error("request 유효성 검사 실패");

        List<GlobalResponse<?>> list = new ArrayList<>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError fieldError : fieldErrors) {
            GlobalResponse<?> response = GlobalResponse.toGlobalResponseFail(400, fieldError.getDefaultMessage());
            list.add(response);
        }

        return ResponseEntity.badRequest().body(list);
    }
}
