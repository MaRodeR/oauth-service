package org.bp.labs.oauthservice.services;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bp.labs.oauthservice.domain.User;
import org.bp.labs.oauthservice.domain.UserAuthority;
import org.bp.labs.oauthservice.repository.CountryRepository;
import org.bp.labs.oauthservice.repository.UserAuthorityRepository;
import org.bp.labs.oauthservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;


@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Service
public class UserServiceImpl implements UserService {

    //    TODO: move to properties
    private static final int MIN_PASSWORD_LENGTH = 6;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private MailService mailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    @Autowired
    private CountryRepository countryRepository;

    @Value("${user.verification.phone.enabled:false}")
    private boolean verifyPhone;

    void setUserAuthorityRepository(UserAuthorityRepository userAuthorityRepository) {
        this.userAuthorityRepository = userAuthorityRepository;
    }

    @Override
    public void checkUniqueness(User user) throws WrongUserDataException {
        int countByEmail = userRepository.countByEmail(user.getEmail());

        Map<String, String> fieldErrors = new HashMap<>();
        if (countByEmail != 0) {
            fieldErrors.put("email", "User with this email already exists");
        }

        if (verifyPhone) {
            int countByPhone = userRepository.countByPhone(user.getPhone());
            if (countByPhone != 0) {
                fieldErrors.put("phone", "User with this phone already exists");
            }
        }

        if (!fieldErrors.isEmpty()) {
            throw new WrongUserDataException(fieldErrors);
        }
    }

    @Override
    @Transactional
    public User createUser(User user) throws WrongUserDataException {
        user.setUserName(user.getEmail());
        validateUser(user);
        encodePassword(user);

        user = userRepository.save(user);
        updateUserRoles(user.getUserName(), isEmpty(user.getRoles()) ? Collections.singletonList("ROLE_USER") : user.getRoles());

//        TODO: subscription logic
//        if (user.getNewsletterStatus() == User.Status.Y) {
//            changeSubscriptionStatus(user);
//        }

        logger.info("User with email: " + user.getEmail() + " is created");
        return user;
    }

    @Override
    public User findUser(String userName) {
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            user = userRepository.findByEmail(userName);
        }
        if (user != null) {
            List<String> roleNames = getRolesFor(user.getUserName());
            user.setRoles(roleNames);
        }
        return user;
    }

    private List<String> getRolesFor(String userName) {
        List<UserAuthority> roles = userAuthorityRepository.findByUserName(userName);

        return roles.stream().map(UserAuthority::getRole).collect(toList());
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        User savedUser = userRepository.findWithNewTransactionById(user.getId());
//        TODO: subscription logic
//        User.Status savedNewsletterStatus = savedUser.getNewsletterStatus();
        user.setPassword(savedUser.getPassword());
        user.setUserName(savedUser.getUserName());
        user.setEmail(savedUser.getEmail());
        user = userRepository.save(user);

//        TODO: subscription logic
//        if (user.getNewsletterStatus() != savedNewsletterStatus) {
//            changeSubscriptionStatus(user);
//        }

        return user;
    }

    @Override
    @Transactional
    public List<String> updateUserRoles(String userName, List<String> roles) {
        if (!isEmpty(roles)) {
            userAuthorityRepository.deleteByUserName(userName);

            roles.stream().distinct().filter(Objects::nonNull).forEach(role -> {
                UserAuthority userAuthority = new UserAuthority()
                        .setUserName(userName)
                        .setRole(role);
                userAuthorityRepository.save(userAuthority);
            });
        }
        return getRolesFor(userName);
    }

    @Override
    public void updateUserPassword(String userName, String oldPassword, String newPassword) throws WrongPasswordException, WrongUserDataException {
        User user = userRepository.findByUserName(userName);

        validatePasswordLength(newPassword);
        checkPassword(user.getPassword(), oldPassword);

        user.setPassword(newPassword);
        encodePassword(user);

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackOn = Throwable.class)
    public String restoreUserPassword(String userName) throws UserNotFoundException, MailService.MailServiceException {
        User user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UserNotFoundException(userName);
        }
        String newPassword = RandomStringUtils.randomAlphanumeric(8);

        user.setPassword(newPassword);
        encodePassword(user);
        userRepository.save(user);

        mailService.sendRestorePasswordMail(user.getEmail(), newPassword);

        return newPassword;
    }

    private void validateUser(User user) throws WrongUserDataException {
        validatePasswordLength(user.getPassword());
        checkUniqueness(user);
    }

    private void validatePasswordLength(String password) throws WrongUserDataException {
        if (StringUtils.length(password) < MIN_PASSWORD_LENGTH) {
            throw new WrongUserDataException(Collections.singletonMap("password",
                    "Password must contain at least " + MIN_PASSWORD_LENGTH + " symbols"));
        }
    }

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    private void checkPassword(String preEncodedPassword, String password) throws WrongPasswordException {
        if (!passwordEncoder.matches(password, preEncodedPassword)) {
            throw new WrongPasswordException();
        }
    }

//        TODO: subscription logic
//    private void changeSubscriptionStatus(User user) {
//        try {
//            if (user.getNewsletterStatus() == User.Status.Y) {
//                String countryName = getCountryNameFor(user);
//                mailService.subscribeToNewsLetter(user.getEmail(), user.getFirstName(), user.getLastName(), countryName);
//            } else {
//                mailService.unsubscribeFromNewsLetters(user.getEmail());
//            }
//        } catch (Exception e) {
//            logger.error("Change subscription status error for user " + user.getEmail(), e);
//        }
//    }
//
//    private String getCountryNameFor(User user) {
//        String countryName = "";
//        if (user.getCountryId() != null) {
//            Optional<Country> countryOptional = countryRepository.findById(user.getCountryId().longValue());
//
//            countryName = countryOptional
//                    .map(Country::getEnglishName)
//                    .orElse("");
//        }
//        return countryName;
//    }

}
