package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Subtenant;
import cz.mycom.veeam.portal.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author dursik
 */
public interface TenantRepository extends JpaRepository<Tenant, Integer> {
    List<Tenant> findByRepositoryUid(String uid);
    Tenant findBySubtenantsEquals(Subtenant subtenant);
    Tenant findByUid(String uid);
}
