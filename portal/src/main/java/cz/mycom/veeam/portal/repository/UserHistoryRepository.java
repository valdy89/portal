package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.HistoryPK;
import cz.mycom.veeam.portal.model.TenantHistory;
import cz.mycom.veeam.portal.model.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dursik
 */
public interface UserHistoryRepository extends JpaRepository<UserHistory, HistoryPK> {
}
