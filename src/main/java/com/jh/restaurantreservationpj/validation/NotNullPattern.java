package com.jh.restaurantreservationpj.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotNullPatternValidator.class)
public @interface NotNullPattern { // null을 허용하면서 null값이 아닌 입력이 들어온 경우는 검증하는 커스텀 어노테이션
    String message() default "시간은 두자리 숫자로 표현해주세요. 예: 01, 13";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String pattern() default "\\d{2}"; // 기본 패턴은 00
}
