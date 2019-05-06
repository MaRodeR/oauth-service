package org.bp.labs.oauthservice.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class MessageBirdClient {

    private Logger logger = LoggerFactory.getLogger(MessageBirdClient.class);

    @Value("${messagebird.api-key:}")
    private String apiKey;

    public void sendSMS(String phoneNumber, String text) {
        logger.info("Sending message '" + text + "' to phone " + phoneNumber + ".");
        try {
            final HttpResponse<JsonNode> response = Unirest.post("https://rest.messagebird.com/messages")
                    .header("Authorization", "AccessKey " + apiKey)
                    .field("recipients", phoneNumber)
                    .field("originator", "Maground")
                    .field("body", text)
                    .asJson();

            if (!HttpStatus.valueOf(response.getStatus()).is2xxSuccessful()) {
                logger.error("Error sending sms to " + phoneNumber + ". Response code " + response.getStatus());
                logger.error("ResponseBody: " + response.getBody().toString());
                throw new SendSMSException("Error sending sms to " + phoneNumber);
            }
            logger.info("Sms to " + phoneNumber + " was sent");
        } catch (UnirestException e) {
            throw new SendSMSException("Error sending sms to " + phoneNumber, e);
        }
    }

    private class SendSMSException extends RuntimeException {

        SendSMSException(String message) {
            super(message);
        }

        SendSMSException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
