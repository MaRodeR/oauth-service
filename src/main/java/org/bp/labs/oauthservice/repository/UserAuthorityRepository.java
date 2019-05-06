package org.bp.labs.oauthservice.repository;

import org.bp.labs.oauthservice.domain.UserAuthority;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserAuthorityRepository extends CrudRepository<UserAuthority, UserAuthority> {

    List<UserAuthority> findByUserName(String userName);

    void deleteByUserName(String userName);
}
