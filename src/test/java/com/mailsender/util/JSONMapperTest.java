package com.mailsender.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.util.exceptions.JSONMapperException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JSONMapperTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private JSONMapper jsonMapper;

    @Test
    public void whenObjectMapperSucceedsReturnObject() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        setupJSONMapper(mapper);

        RomCharmEmail expected = new RomCharmEmail("email", "first", "second", true, 5);

        String string = mapper.writeValueAsString(expected);

        RomCharmEmail result = jsonMapper.getObjectFromJSONString(string, RomCharmEmail.class);

        assertThat(result, is(expected));
    }

    @Test
    public void whenObjectMapperFailsExpectJsonMapperException() throws IOException {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        setupJSONMapper(objectMapper);

        when(objectMapper.readValue(anyString(), eq(RomCharmEmail.class))).thenThrow(new IOException(""));

        exception.expect(JSONMapperException.class);

        jsonMapper.getObjectFromJSONString("", RomCharmEmail.class);
    }

    private void setupJSONMapper(ObjectMapper objectMapper) {
        jsonMapper = new JSONMapper(objectMapper);
    }
}