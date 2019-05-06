package org.bp.labs.oauthservice.repository;

import org.bp.labs.oauthservice.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends CrudRepository<User, Long> {

    int countByEmail(String email);

    int countByPhone(String phone);

    User findByEmail(String email);

    User findByUserName(String userName);

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    User findWithNewTransactionById(Long id);
}
