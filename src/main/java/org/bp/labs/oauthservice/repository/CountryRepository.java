package org.bp.labs.oauthservice.repository;

import org.bp.labs.oauthservice.domain.Country;
import org.springframework.data.repository.CrudRepository;


public interface CountryRepository extends CrudRepository<Country, Long> {
}
