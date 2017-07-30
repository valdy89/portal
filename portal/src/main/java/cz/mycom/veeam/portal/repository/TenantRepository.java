package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author dursik
 */
public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    @Query("select sum(quota) from Tenant where repositoryUid = :repositoryUid and uid <> :uid")
    Integer sumQuota(@Param("repositoryUid") String repositoryUid, @Param("uid") String uid);
}
