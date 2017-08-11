package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Subtenant;
import cz.mycom.veeam.portal.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author dursik
 */
public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    List<Tenant> findByRepositoryUid(String uid);
    Tenant findBySubtenantsEquals(Subtenant subtenant);
    Tenant findByUid(String uid);
    @Query("select sum(quota) from Tenant where repositoryUid = :repositoryUid and uid <> :uid")
    Integer sumQuota(@Param("repositoryUid") String repositoryUid, @Param("uid") String uid);
}
