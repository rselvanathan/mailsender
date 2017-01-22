package com.mailsender.dto.producers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailsender.defaults.AppType;
import com.mailsender.dto.EmailMessage;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.util.JSONMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailProducerTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final JSONMapper jsonMapper = new JSONMapper(mapper);

    private static final EmailProducer emailProducer = new EmailProducer(jsonMapper);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void whenAppTypeIsROMCHARMConvertJSONToRomCharmMailMessage() {
        String json = "{\"email\":\"romesh1305@googlemail.com\",\"firstName\":\"Romesh\",\"lastName\":\"Selvanathan\",\"areAttending\":true,\"numberAttending\":1}";
        RomCharmEmail expectedEmail = new RomCharmEmail("romesh1305@googlemail.com", "Romesh", "Selvanathan", true, 1);

        EmailMessage result = emailProducer.getEmailMessage(AppType.ROMCHARM, json);

        assertThat(result, is(expectedEmail));
    }

    @Test
    public void whenAppTypeIsNotRecognisedExpectIllegalArgumentException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The App type has not been recognised");

        emailProducer.getEmailMessage(AppType.DEFAULT, "");
    }
}