package org.bp.labs.oauthservice.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface PhoneVerificationService {

    public String sendVerificationCode(String phone) throws PhoneVerificationException;

    public void verifyCode(String phone, String code) throws PhoneVerificationException;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class PhoneVerificationException extends Exception {

        public PhoneVerificationException(String message) {
            super(message);
        }
    }
}
