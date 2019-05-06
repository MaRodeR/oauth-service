package org.bp.labs.oauthservice.services;

import org.apache.commons.lang3.tuple.Pair;
import org.bp.labs.oauthservice.client.MessageBirdClient;
import org.bp.labs.oauthservice.services.PhoneVerificationService.PhoneVerificationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.addMinutes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PhoneVerificationServiceImplTest {

    @Autowired
    private PhoneVerificationServiceImpl phoneVerificationService;
    @MockBean
    private MessageBirdClient messageBirdClient;

    @Before
    public void setUp() {
        phoneVerificationService.setVerificationEnabled(true);
        phoneVerificationService.setTestMode(false);
        phoneVerificationService.getSentCodes().clear();
    }

    @Test
    public void sendVerificationCode_valid() throws Exception {

        assertTrue(phoneVerificationService.getSentCodes().isEmpty());

        String result = phoneVerificationService.sendVerificationCode("777777");

        final Pair<String, Date> code = phoneVerificationService.getSentCodes().get("777777");
        assertNotNull(code);
        assertNotNull(code.getLeft());
        assertThat(code.getRight())
                .isCloseTo(new Date(), 10000);

        verify(messageBirdClient).sendSMS("777777", "Verification code: " + code.getLeft());

        assertEquals("Verification code has been sent", result);
    }

    @Test
    public void sendVerificationCode_testMode() throws Exception {
        phoneVerificationService.setTestMode(true);

        final String result = phoneVerificationService.sendVerificationCode("777777");

        final Pair<String, Date> code = phoneVerificationService.getSentCodes().get("777777");
        assertNotNull(code);
        assertNotNull(code.getLeft());
        assertThat(code.getRight())
                .isCloseTo(new Date(), 10000);

        verify(messageBirdClient).sendSMS("777777", "Verification code: " + code.getLeft());

        assertEquals("Verification code has been sent: "+code.getLeft(), result);
    }

    @Test
    public void sendVerificationCode_verificationDisabled() throws Exception {
        phoneVerificationService.setVerificationEnabled(false);

        final String result = phoneVerificationService.sendVerificationCode("777777");
        assertEquals("Verification of phone disabled", result);

        verify(messageBirdClient, never()).sendSMS(anyString(), anyString());
    }

    @Test(expected = PhoneVerificationException.class)
    public void sendVerificationCode_emptyArg() throws Exception {
        phoneVerificationService.sendVerificationCode("");
    }

    @Test
    public void sendVerificationCode_retryIntoInterval() {
        phoneVerificationService.saveVerificationCode("777777", "543543", new Date());

        assertThatThrownBy(
                () -> phoneVerificationService.sendVerificationCode("777777")
        ).isInstanceOf(PhoneVerificationException.class);

        verify(messageBirdClient, never()).sendSMS(anyString(), anyString());
    }

    @Test
    public void sendVerificationCode_retryAfterInterval() throws Exception {
        phoneVerificationService.saveVerificationCode("777777", "543543", addMinutes(new Date(), -2));

        phoneVerificationService.sendVerificationCode("777777");

        verify(messageBirdClient, times(1)).sendSMS(eq("777777"), anyString());
    }

    @Test
    public void verifyCode_validCode() throws Exception {
        phoneVerificationService.saveVerificationCode("777777", "543543", new Date());

        phoneVerificationService.verifyCode("777777", "543543");
    }

    @Test(expected = PhoneVerificationException.class)
    public void verifyCode_invalidCode() throws Exception {
        phoneVerificationService.saveVerificationCode("777777", "543543", new Date());

        phoneVerificationService.verifyCode("777777", "555555");
    }

    @Test(expected = PhoneVerificationException.class)
    public void verifyCode_missedCode() throws Exception {
        phoneVerificationService.verifyCode("777777", "543543");
    }

    @Test
    public void verifyCode_verificationDisabled() throws Exception {
        phoneVerificationService.setVerificationEnabled(false);

        phoneVerificationService.verifyCode("777777", "543543");
    }
}