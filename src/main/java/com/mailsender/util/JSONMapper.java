package com.mailsender.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsender.util.exceptions.JSONMapperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JSONMapper {
    private final ObjectMapper objectMapper;

    @Autowired
    public JSONMapper(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    public <T>T getObjectFromJSONString(String jsonString, Class<T> tClass) {
        try {
            return objectMapper.readValue(jsonString, tClass);
        } catch (IOException e) {
            throw new JSONMapperException(e);
        }
    }

    public JsonNode getJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new JSONMapperException(e);
        }
    }

    public <T>T convertValue(Object from, Class<T> to) {
        return objectMapper.convertValue(from, to);
    }
}
