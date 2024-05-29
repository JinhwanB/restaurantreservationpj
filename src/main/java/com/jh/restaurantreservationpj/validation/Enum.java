package com.jh.restaurantreservationpj.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Enum타입을 검증하기 위한 커스텀 어노테이션
@Constraint(validatedBy = {EnumValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Enum {

    String message() default "Invalid Enum Value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
