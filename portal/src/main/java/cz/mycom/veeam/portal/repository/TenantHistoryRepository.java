package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.TenantHistory;
import cz.mycom.veeam.portal.model.HistoryPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

/**
 * @author dursik
 */
public interface TenantHistoryRepository extends JpaRepository<TenantHistory, HistoryPK> {
    @Query("select max(quota) from TenantHistory where uid = :uid and dateCreated >= DATE(NOW()) and dateCreated < DATE(ADDDATE(NOW(), 1))")
    Integer getTodayMaxQuota(@Param("uid") String uid);
}
