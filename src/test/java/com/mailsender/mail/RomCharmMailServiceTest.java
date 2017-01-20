package com.mailsender.mail;

import com.mailsender.dto.RomCharmEmail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

@RunWith(MockitoJUnitRunner.class)
public class RomCharmMailServiceTest {

    private static final String SIRNAME = "SIRNAME";

    private static final String FIRSTNAME = "FIRSTNAME";

    private static final String EMAIL = "EMAIL";

    @Mock
    private MailSender mailSenderMock;

    @InjectMocks
    private RomCharmMailService romCharmMailService;

    @Test
    public void whenUserIsAttendingAddIsAttendingStringToMessage() {
        int numberAttending = 2;
        boolean attending = true;
        RomCharmEmail romCharmEmail = new RomCharmEmail(EMAIL, FIRSTNAME, SIRNAME, attending, numberAttending);

        romCharmMailService.sendMail(romCharmEmail);

        SimpleMailMessage expectedMailMessage = getExpectedMailMessage("Yes", numberAttending);

        Mockito.verify(mailSenderMock).send(expectedMailMessage);
    }

    @Test
    public void whenUserIsNotAttendingAddIsNotAttendingStringToMessage() {
        int numberAttending = 0;
        boolean attending = false;
        RomCharmEmail romCharmEmail = new RomCharmEmail(EMAIL, FIRSTNAME, SIRNAME, attending, numberAttending);

        romCharmMailService.sendMail(romCharmEmail);

        SimpleMailMessage expectedMailMessage = getExpectedMailMessage("No", numberAttending);

        Mockito.verify(mailSenderMock).send(expectedMailMessage);
    }

    private SimpleMailMessage getExpectedMailMessage(String expectedAttendanceString, int expectedNumberAttending) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("romeshselvan@hotmail.co.uk");
        message.setTo(EMAIL);
        message.setSubject("RSVP Confirmation - Romesh & Charmikha");

        StringBuilder builder = new StringBuilder();
        builder.append("Hey ").append(FIRSTNAME).append("\n");
        builder.append("\n");
        builder.append("Thank you for letting us know whether you are coming to the reception.").append("\n");
        builder.append("\n");
        builder.append("Attending : ").append(expectedAttendanceString).append("\n");
        builder.append("Number of people Attending : ").append(expectedNumberAttending).append("\n");
        builder.append("\n");
        builder.append("If you would like to make any changes, please contact us as soon as possible.").append("\n");
        builder.append("\n");
        builder.append("Kind Regards,").append("\n");
        builder.append("\n");
        builder.append("Romesh & Charmikha");

        message.setText(builder.toString());

        return message;
    }
}