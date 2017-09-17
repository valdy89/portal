package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.CloseDay;
import cz.mycom.veeam.portal.model.Config;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dursik
 */
public interface CloseDayRepository extends JpaRepository<CloseDay, Integer> {
}
