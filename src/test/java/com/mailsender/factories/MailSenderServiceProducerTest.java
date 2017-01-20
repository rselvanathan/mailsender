package com.mailsender.factories;

import com.mailsender.defaults.AppType;
import com.mailsender.mail.MailSenderService;
import com.mailsender.mail.RomCharmMailService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.mailsender.defaults.AppType.DEFAULT;
import static com.mailsender.defaults.AppType.ROMCHARM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class MailSenderServiceProducerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private MailSenderProducer mailSenderProducer;

    @InjectMocks
    private MailSenderServiceProducer mailSenderServiceProducer;

    @Test
    public void whenAppTypeIsROMCHARMReturnTheRomCharmMailService() {
        MailSenderService service = mailSenderServiceProducer.getMailSenderService(ROMCHARM);
        assertThat(service, is(instanceOf(RomCharmMailService.class)));
    }

    @Test
    public void whenAppTypeIsUnknownExpectIlleaglArgumentException() {
        exception.expect(IllegalArgumentException.class);
        mailSenderServiceProducer.getMailSenderService(DEFAULT);
    }
}