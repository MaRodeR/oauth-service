package org.bp.labs.oauthservice.services;

import org.bp.labs.oauthservice.domain.User;
import org.bp.labs.oauthservice.domain.UserAuthority;
import org.bp.labs.oauthservice.repository.UserAuthorityRepository;
import org.bp.labs.oauthservice.repository.UserRepository;
import org.bp.labs.oauthservice.services.MailService.MailServiceException;
import org.bp.labs.oauthservice.services.UserService.UserNotFoundException;
import org.bp.labs.oauthservice.services.UserService.WrongPasswordException;
import org.bp.labs.oauthservice.services.UserService.WrongUserDataException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static java.lang.Long.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@SqlGroup(value = {
        @Sql(value = {"/create_test_users.sql"}),
        @Sql(value = {"/drop_test_users.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class UserServiceImplTest {

    private static final String EXISTS_EMAIL = "test@mail.ru";
    private static final String EXISTS_USERNAME = "test_user";

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    @Mock
    private UserAuthorityRepository userAuthorityRepositoryMock;
    @MockBean
    private MailService mailServiceMock;

    @Test
    public void createUser() throws Exception {
        User newUser = createTestUser();
        User createdUser = userService.createUser(newUser);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertNotNull(createdUser.getCreateDate());
        assertEquals(newUser.getAddress(), createdUser.getAddress());
        assertEquals(newUser.getAddressLine2(), createdUser.getAddressLine2());
        assertEquals(createdUser.getUserName(), createdUser.getEmail());
//        TODO: subscription logic
//        assertEquals(User.Status.Y, createdUser.getNewsletterStatus());
        assertNull(createdUser.getLastLoginDate());
        assertNotEquals("1111111", createdUser.getPassword());

//        TODO: subscription logic
//        verify(mailServiceMock).subscribeToNewsLetter(newUser.getEmail(),
//                newUser.getFirstName(), newUser.getLastName(), "");
    }

    @Test
    @DirtiesContext
    public void createUserWithRoles() throws Exception {

        userService.setUserAuthorityRepository(userAuthorityRepositoryMock);

        User newUser = createTestUser();
        newUser.setRoles(Arrays.asList("ROLE_USER", "ROLE_TEST"));
        userService.createUser(newUser);

        ArgumentCaptor<UserAuthority> authorityArgumentCaptor = ArgumentCaptor.forClass(UserAuthority.class);

        verify(userAuthorityRepositoryMock, times(2)).save(authorityArgumentCaptor.capture());
        assertThat(authorityArgumentCaptor.getAllValues())
                .hasSize(2)
                .extracting("role")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_TEST");
    }

    @Test
    @DirtiesContext
    public void createUserWithException() {
        when(userAuthorityRepositoryMock.save(any(UserAuthority.class))).thenThrow(new IllegalArgumentException("test exception"));

        userService.setUserAuthorityRepository(userAuthorityRepositoryMock);

        User testUser = createTestUser();

        assertThat(
                catchThrowable(() -> userService.createUser(testUser))
        ).isInstanceOf(IllegalArgumentException.class);

        assertNull(userRepository.findByEmail(testUser.getEmail()));
    }

    @Test(expected = WrongUserDataException.class)
    public void createUserWithExistEmail() throws Exception {
        User testUser = createTestUser();
        testUser.setEmail(EXISTS_EMAIL);
        userService.createUser(testUser);
    }

    @Test(expected = WrongUserDataException.class)
    public void createUserWithShortPassword() throws Exception {
        User testUser = createTestUser();
        testUser.setPassword("123");
        userService.createUser(testUser);
    }

    @Test
    public void createUserWithoutNewsSubscription() throws Exception {
        User newUser = createTestUser();
        //        TODO: subscription logic
//        newUser.setNewsletterStatus(User.Status.N);
        userService.createUser(newUser);

        verify(mailServiceMock, never()).subscribeToNewsLetter(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void createUserWithSubscriptionError() throws Exception {
        doThrow(new MailServiceException("test exception"))
                .when(mailServiceMock).subscribeToNewsLetter(anyString(), anyString(), anyString(), anyString());
        User testUser = createTestUser();

        User user = userService.createUser(testUser);
        assertNotNull(user);
        assertNotNull(user.getId());

        assertNotNull(userRepository.findByEmail(testUser.getEmail()));
    }

    @Test
    public void findUser() {
        User user = userService.findUser(EXISTS_USERNAME);
        assertEquals(valueOf(10001L), user.getId());
        assertEquals("test@mail.ru", user.getEmail());

        assertThat(user.getRoles()).containsOnly("ROLE_USER");
    }

    @Test
    public void updateUser() throws Exception {
        User user = userRepository.findById(10001L).get();
        String oldPassword = user.getPassword();

        user.setUserName(EXISTS_USERNAME);
        user.setEmail("ignored@mail.ru");
        user.setPassword("ignore");
        user.setCity("London");

        userService.updateUser(user);

        User updatedUser = userRepository.findById(10001L).get();
        assertEquals("London", updatedUser.getCity());

//        Assert that userName, email, password was not updated
        assertEquals(EXISTS_USERNAME, updatedUser.getUserName());
        assertEquals("test@mail.ru", updatedUser.getEmail());
        assertEquals(oldPassword, updatedUser.getPassword());

        List<UserAuthority> userAuthorities = userAuthorityRepository.findByUserName(EXISTS_USERNAME);

        assertThat(userAuthorities)
                .hasSize(1)
                .extracting("role")
                .contains("ROLE_USER");

        verify(mailServiceMock, never()).unsubscribeFromNewsLetters(anyString());
        verify(mailServiceMock, never()).subscribeToNewsLetter(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void updateUser_withUnsubscribe() {
        User user = userRepository.findById(10001L).get();
        user.setUserName(EXISTS_USERNAME);
//        TODO: subscription logic
//        user.setNewsletterStatus(User.Status.N);

        userService.updateUser(user);

//        TODO: subscription logic
//        verify(mailServiceMock).unsubscribeFromNewsLetters("test@mail.ru");
        userService.updateUser(user);
    }

    @Test
    public void updateUserPassword() throws Exception {
        User user = userRepository.findById(10001L).get();
        String oldPassword = user.getPassword();

        userService.updateUserPassword(EXISTS_USERNAME, "11111111", "22222222");
        user = userRepository.findById(10001L).get();
        assertNotEquals(oldPassword, user.getPassword());
    }

    @Test(expected = WrongPasswordException.class)
    public void updateUserPasswordWithWrongOldPassword() throws Exception {
        userService.updateUserPassword(EXISTS_USERNAME, "33333333", "22222222");
    }

    @Test(expected = WrongUserDataException.class)
    public void updateUserPasswordWithShortNewPassword() throws Exception {
        userService.updateUserPassword(EXISTS_USERNAME, "11111111", "2");
    }

    @Test
    public void updateUserRoles() {
        userService.updateUserRoles(EXISTS_USERNAME, Arrays.asList("ROLE_USER", "ROLE_PHOTOGRAPHER"));

        List<UserAuthority> userAuthorities = userAuthorityRepository.findByUserName(EXISTS_USERNAME);

        assertThat(userAuthorities)
                .hasSize(2)
                .extracting("role")
                .contains("ROLE_USER", "ROLE_PHOTOGRAPHER");
    }

    @Test
    public void restoreUserPassword() throws Exception {
        User user = userRepository.findByUserName(EXISTS_USERNAME);

        String newPassword = userService.restoreUserPassword(EXISTS_USERNAME);
        User updatedUser = userRepository.findByUserName(EXISTS_USERNAME);

        assertThat(newPassword)
                .isNotNull()
                .hasSize(8);

        assertNotEquals(user.getPassword(), updatedUser.getPassword());

        verify(mailServiceMock).sendRestorePasswordMail(user.getEmail(), newPassword);
    }

    @Test(expected = UserNotFoundException.class)
    public void restoreIncorrectUserPassword() throws Exception {
        userService.restoreUserPassword(EXISTS_USERNAME + "123");
    }

    @Test
    public void restoreUserPasswordWithMailError() throws Exception {
        doThrow(new MailServiceException("test")).when(mailServiceMock).sendRestorePasswordMail(anyString(), anyString());

        User user = userRepository.findByUserName(EXISTS_USERNAME);

        assertThat(
                catchThrowable(() -> userService.restoreUserPassword(EXISTS_USERNAME))
        ).isNotNull();

        User updatedUser = userRepository.findByUserName(EXISTS_USERNAME);
        assertEquals(user.getPassword(), updatedUser.getPassword());
    }

    private User createTestUser() {
        return new User()
                .setFirstName("Ivan")
                .setLastName("Ivanov")
                .setUserName("iivanov@mail.ru")
                .setPassword("11111111")
                .setEmail("iivanov@mail.ru")
                .setCity("Moscow")
                .setAddress("address")
                .setAddressLine2("address_line2")
                .setJobTitle("Designer");
//        TODO: subscription logic
//                .setNewsletterStatus(User.Status.Y)
    }

}