package com.mailsender.factories;

import com.mailsender.defaults.AppType;
import com.mailsender.mail.MailSenderService;
import com.mailsender.mail.RomCharmMailService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class MailSenderServiceProducerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private MailSenderServiceProducer mailSenderServiceProducer;

    @Test
    public void whenAppTypeIsROMCHARMReturnTheRomCharmMailService() {
        MailSenderService service = mailSenderServiceProducer.getMailSenderService(AppType.ROMCHARM);
        assertThat(service, is(instanceOf(RomCharmMailService.class)));
    }

    @Test
    public void whenAppTypeIsUnknownExpectIlleaglArgumentException() {
        exception.expect(IllegalArgumentException.class);
        mailSenderServiceProducer.getMailSenderService(null);
    }
}