package org.bp.labs.oauthservice.services;

public interface MailService {

    void sendRestorePasswordMail(String email, String newPassword) throws MailServiceException;

    void sendRegistrationMail(String email) throws MailServiceException;

    void subscribeToNewsLetter(String email, String firstName, String lastName, String country) throws MailServiceException;

    void unsubscribeFromNewsLetters(String email) throws MailServiceException;

    public class MailServiceException extends Exception {

        public MailServiceException(String message) {
            super(message);
        }

        public MailServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
