package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dursik
 */
public interface ConfigRepository extends JpaRepository<Config, String> {
}
