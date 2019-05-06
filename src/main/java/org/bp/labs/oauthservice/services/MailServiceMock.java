package org.bp.labs.oauthservice.services;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MailServiceMock implements MailService {

    @Override
    public void sendRestorePasswordMail(String email, String newPassword) throws MailServiceException {

    }

    @Override
    public void sendRegistrationMail(String email) throws MailServiceException {

    }

    @Override
    public void subscribeToNewsLetter(String email, String firstName, String lastName, String country) throws MailServiceException {

    }

    @Override
    public void unsubscribeFromNewsLetters(String email) throws MailServiceException {

    }
}
