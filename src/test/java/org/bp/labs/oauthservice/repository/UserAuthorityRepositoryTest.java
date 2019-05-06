package org.bp.labs.oauthservice.repository;

import org.bp.labs.oauthservice.domain.UserAuthority;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserAuthorityRepositoryTest {

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Test
    public void save() throws Exception {
        UserAuthority userAuthority = new UserAuthority()
                .setUserName("test1")
                .setRole("USER_ROLE");

        userAuthorityRepository.save(userAuthority);

        Optional<UserAuthority> savedAuthority = userAuthorityRepository.findById(userAuthority);

        assertThat(savedAuthority.isPresent()).isTrue();
    }

}