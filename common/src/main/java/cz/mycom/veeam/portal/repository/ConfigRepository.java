package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author dursik
 */
public interface ConfigRepository extends JpaRepository<Config, String> {
    @Query("select c from Config c order by c.name")
    List<Config> findAllOrderByName();
}
