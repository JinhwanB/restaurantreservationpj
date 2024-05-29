package com.jh.restaurantreservationpj.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Arrays;

// EnumClass 어노테이션이 붙은 곳에서 실행될 커스텀 Deserializer
// 모든 Enum 인스턴스에 대해 적용하기 위해 Enum<?>로 적용
public class CustomEnumDeserializer extends StdDeserializer<java.lang.Enum<?>> implements ContextualDeserializer {

    public CustomEnumDeserializer() {
        this(null);
    }

    protected CustomEnumDeserializer(Class<?> vc) {
        super(vc);
    }

    @SuppressWarnings("unchecked") // Class<? extends Enum>으로 캐스팅할 때 unchecked 경고가 발생할 수 있으므로 이를 억제하기 위해 사용
    @Override
    public java.lang.Enum<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        JsonNode nameNode = jsonNode.get("name");

        if (nameNode == null) {
            return null;
        }

        String text = jsonNode.asText();
        Class<? extends java.lang.Enum> enumType = (Class<? extends java.lang.Enum>) this._valueClass;

        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.name().equals(text))
                .findAny()
                .orElse(null);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {

        return new CustomEnumDeserializer(beanProperty.getType().getRawClass());
    }
}
