package org.bp.labs.oauthservice.services;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bp.labs.oauthservice.client.MessageBirdClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.time.DateUtils.addMinutes;

@Service
public class PhoneVerificationServiceImpl implements PhoneVerificationService {

    private Logger logger = LoggerFactory.getLogger(PhoneVerificationServiceImpl.class);

    private static final int RETRY_INTERVAL_MINUTES = 1;

    @Autowired
    private MessageBirdClient messageBirdClient;
    @Getter
    @Setter
    @Value("${user.verification.phone.enabled:false}")
    private boolean verificationEnabled;
    @Getter
    @Setter
    @Value("${user.verification.phone.test-mode:false}")
    private boolean testMode;

    @Getter(AccessLevel.PROTECTED)
    private Map<String, Pair<String, Date>> sentCodes = new HashMap<>();

    @Override
    public String sendVerificationCode(String phone) throws PhoneVerificationException {
        if (verificationEnabled) {
            if (isEmpty(phone)) {
                throw new PhoneVerificationException("Phone is empty");
            }
            if (isSentRecentlyTo(phone)) {
                throw new PhoneVerificationException("Verification code has been sent. Please try " + RETRY_INTERVAL_MINUTES + " minute later");
            }

            String code = RandomStringUtils.randomNumeric(6);

            messageBirdClient.sendSMS(phone, "Verification code: " + code);
            saveVerificationCode(phone, code, new Date());

            return "Verification code has been sent" + (testMode ? ": " + code : "");
        } else {
            logger.info("Verification of phone disabled");
            return "Verification of phone disabled";
        }
    }

    protected void saveVerificationCode(String phone, String code, Date date) {
        sentCodes.put(phone, Pair.of(code, date));
    }

    private boolean isSentRecentlyTo(String phone) {
        Pair<String, Date> previousCode = sentCodes.get(phone);
        return previousCode != null &&
                addMinutes(previousCode.getRight(), RETRY_INTERVAL_MINUTES).after(new Date());
    }

    public void verifyCode(String phone, String code) throws PhoneVerificationException {
        if (verificationEnabled) {
            if (isEmpty(phone)) {
                throw new PhoneVerificationException("Phone is empty");
            }
            final Pair<String, Date> savedCode = sentCodes.get(phone);
            if (savedCode == null || !StringUtils.equals(code, savedCode.getKey())) {
                throw new PhoneVerificationException("Verification code is incorrect");
            }
        } else {
            logger.info("Verification of phone disabled");
        }
    }
}
