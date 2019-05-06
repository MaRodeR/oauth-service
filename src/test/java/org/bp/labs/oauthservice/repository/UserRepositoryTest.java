package org.bp.labs.oauthservice.repository;

import org.bp.labs.oauthservice.domain.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Sql(value = "/create_test_users.sql")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void findByEmail() {
        User user = userRepository.findByEmail("test@mail.ru");

        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 10001L)
                .hasFieldOrPropertyWithValue("userName", "test_user");
    }

    @Test
    public void findByUserName() {
        User user = userRepository.findByUserName("test_user");

        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 10001L)
                .hasFieldOrPropertyWithValue("userName", "test_user");
    }

}