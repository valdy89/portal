package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Config;
import cz.mycom.veeam.portal.model.Subtenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author dursik
 */
public interface ConfigRepository extends JpaRepository<Config, String> {
}
