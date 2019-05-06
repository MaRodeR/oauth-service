package org.bp.labs.oauthservice.services;


import org.bp.labs.oauthservice.domain.User;
import org.bp.labs.oauthservice.services.exceptions.HasDetails;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UserService {

    void checkUniqueness(User user) throws WrongUserDataException;

    User createUser(User user) throws WrongUserDataException;

    User findUser(String userName);

    User updateUser(User user);

    List<String> updateUserRoles(String userName, List<String> roles);

    void updateUserPassword(String userName, String oldPassword, String newPassword) throws WrongPasswordException, WrongUserDataException;

    String restoreUserPassword(String userName) throws UserNotFoundException, MailService.MailServiceException;

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public class UserNotFoundException extends Exception {

        public UserNotFoundException(String userName) {
            super("User with Username " + userName + " not found");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class WrongPasswordException extends Exception{

        public WrongPasswordException() {
            super("User password is incorrect");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public class WrongUserDataException extends Exception implements HasDetails {

        private HashMap<String, String> fieldErrors;

        public WrongUserDataException(Map<String, String> fieldErrors) {
            this.fieldErrors = new HashMap<>(fieldErrors);
        }

        @Override
        public Serializable getErrorDetails() {
            return fieldErrors;
        }

        @Override
        public String getMessage() {
            return String.join(",", fieldErrors.values());
        }
    }
}
