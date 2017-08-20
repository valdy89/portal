package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Subtenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author dursik
 */
public interface SubtenantRepository extends JpaRepository<Subtenant, String> {
    List<Subtenant> findByTenant_UserId(int userId);
    List<Subtenant> findByTenantUid(String uid);
    Subtenant findByUid(String uid);
    Subtenant findByUsernameAndTenantUid(String username, String tenantUid);
    Subtenant findByUidAndTenantUid(String uid, String tenantUid);
}
