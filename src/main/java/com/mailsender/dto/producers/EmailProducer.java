package com.mailsender.dto.producers;

import com.mailsender.defaults.AppType;
import com.mailsender.dto.EmailMessage;
import com.mailsender.dto.RomCharmEmail;
import com.mailsender.util.JSONMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Will Generate an {@link com.mailsender.dto.EmailMessage} object based on the {@link com.mailsender.defaults.AppType}
 */
@Component
public class EmailProducer {

    private final JSONMapper jsonMapper;

    @Autowired
    public EmailProducer(JSONMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * Get the {@link EmailMessage} from the raw SNS Message body. The {@link EmailMessage} returned will depend on the {@link AppType}
     * @param jsonMessageBody Raw SNS Message Body
     * @param appType {@link AppType}
     * @return {@link EmailMessage}
     */
    public EmailMessage getEmailMessage(AppType appType, String jsonMessageBody) {
        if(appType.equals(AppType.ROMCHARM)) {
            return jsonMapper.getObjectFromJSONString(jsonMessageBody, RomCharmEmail.class);
        } else {
            throw new IllegalArgumentException("The App type has not been recognised");
        }
    }
}
