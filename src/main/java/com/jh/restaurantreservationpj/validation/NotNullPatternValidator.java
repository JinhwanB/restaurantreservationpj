package com.jh.restaurantreservationpj.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NotNullPatternValidator implements ConstraintValidator<NotNullPattern, String> {

    private Pattern pattern;

    @Override
    public void initialize(NotNullPattern constraintAnnotation) {
        this.pattern = Pattern.compile(constraintAnnotation.pattern()); // 패턴 초기화
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null){ // null인 경우 허용
            return true;
        }

        return pattern.matcher(value).matches();
    }
}
