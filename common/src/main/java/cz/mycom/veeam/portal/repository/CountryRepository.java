package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dursik
 */
public interface CountryRepository extends JpaRepository<Country, String> {
}
