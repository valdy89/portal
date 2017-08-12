package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.HistoryPK;
import cz.mycom.veeam.portal.model.TenantHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author dursik
 */
public interface TenantHistoryRepository extends JpaRepository<TenantHistory, HistoryPK> {
    @Query("from TenantHistory where uid = :uid and modifier = :modifier and dateCreated >= DATE(NOW()) and dateCreated < DATE(ADDDATE(NOW(), 1))")
    TenantHistory getTodayByModifier(@Param("uid") String uid, @Param("modifier") String modifier);
    @Query("select max(quota) from TenantHistory where uid = :uid and dateCreated >= DATE(NOW()) and dateCreated < DATE(ADDDATE(NOW(), 1))")
    Integer getTodayMaxQuota(@Param("uid") String uid);
}
