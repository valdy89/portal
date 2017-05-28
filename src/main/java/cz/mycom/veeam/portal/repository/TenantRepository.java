package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dursik
 */
public interface TenantRepository extends JpaRepository<Tenant, String> {

}
