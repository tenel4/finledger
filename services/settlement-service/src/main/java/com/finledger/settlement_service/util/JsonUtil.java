package com.finledger.settlement_service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finledger.settlement_service.common.exception.JsonDeserializationException;
import com.finledger.settlement_service.common.exception.JsonSerializationException;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    private final ObjectMapper mapper;

    public JsonUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String toJson(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot serialize null value to JSON");
        }
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException("JSON serialization failed", e);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Cannot deserialize from blank JSON string");
        }
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException("JSON deserialization failed", e);
        }
    }
}
