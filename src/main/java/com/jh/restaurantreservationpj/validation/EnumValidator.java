package com.jh.restaurantreservationpj.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<Enum, java.lang.Enum> {

    @Override
    public boolean isValid(java.lang.Enum value, ConstraintValidatorContext context) {

        if (value == null) { // null 허용 안함
            return false;
        }

        Class<?> reflectionEnumClass = value.getDeclaringClass();

        return Arrays.asList(reflectionEnumClass.getEnumConstants()).contains(value);
    }
}
