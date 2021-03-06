package com.mailsender.mail;

import com.mailsender.dto.EmailMessage;
import com.mailsender.dto.RomCharmEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * A Mail Sender specific to the "ROMCHARM" Application/Service. This will send an RSVP confirmation mail to the recipient.
 */
public class RomCharmMailService implements MailSenderService{
    private static final Logger logger = LoggerFactory.getLogger(RomCharmMailService.class);

    private final MailSender mailSender;

    public RomCharmMailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendMail(EmailMessage emailMessage) {
        RomCharmEmail romCharmEmail = (RomCharmEmail) emailMessage;

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom("romeshselvan@hotmail.co.uk");
        mailMessage.setTo(romCharmEmail.getEmail());
        mailMessage.setSubject("RSVP Confirmation - Romesh & Charmikha");

        StringBuilder builder = new StringBuilder();
        builder.append("Hey ").append(romCharmEmail.getFirstName()).append("\n");
        builder.append("\n");
        builder.append("Thank you for letting us know whether you are coming to the reception.").append("\n");
        builder.append("\n");
        builder.append("Attending : ").append(getAttendanceString(romCharmEmail.isAreAttending())).append("\n");
        builder.append("Number of people Attending : ").append(romCharmEmail.getNumberAttending()).append("\n");
        builder.append("\n");
        builder.append("If you would like to make any changes, please contact us as soon as possible.").append("\n");
        builder.append("\n");
        builder.append("Kind Regards,").append("\n");
        builder.append("\n");
        builder.append("Romesh & Charmikha");

        mailMessage.setText(builder.toString());

        logger.info("Sending mail to " + romCharmEmail.getEmail());
        mailSender.send(mailMessage);
    }

    private String getAttendanceString(boolean attending) {
        return attending ? "Yes" : "No";
    }
}
