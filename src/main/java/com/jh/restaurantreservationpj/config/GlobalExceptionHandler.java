package com.jh.restaurantreservationpj.config;

import com.jh.restaurantreservationpj.restaurant.exception.RestaurantException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 404에러 핸들러
    @ExceptionHandler(NoHandlerFoundException.class)
    private ResponseEntity<GlobalResponse<?>> handleNotFoundException(NoHandlerFoundException e){
        log.error("404 NotFound");

        return new ResponseEntity<>(GlobalResponse.toGlobalResponseFail(404, "요청하신 페이지를 찾을 수 없습니다."), HttpStatus.NOT_FOUND);
    }

    // 유효성 검증 에러 핸들러 -> 400 에러
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

    // 매장 관련 에러 핸들러 -> 400에러
    @ExceptionHandler(RestaurantException.class)
    private ResponseEntity<GlobalResponse<?>> handleRestaurantException(RestaurantException e){
        log.error("매장 관련 exception");

        return ResponseEntity.badRequest().body(GlobalResponse.toGlobalResponseFail(e.getRestaurantErrorCode().getStatus(), e.getRestaurantErrorCode().getMessage()));
    }
}
