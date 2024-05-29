package com.jh.restaurantreservationpj.validation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @JsonCreator를 실행하는 어노테이션
// 존재하는 Enum값이 들어왔다면 Enum 없다면 null
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside // Jackson 관련 어노테이션을 상속시킬 경우 필요
@JsonDeserialize(using = CustomEnumDeserializer.class) // 사용하고자 하는 Deserializer를 지정
public @interface EnumClass {
}
