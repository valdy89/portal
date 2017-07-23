package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Subtenant;
import cz.mycom.veeam.portal.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author dursik
 */
public interface SubtenantRepository extends JpaRepository<Subtenant, String> {
    List<Subtenant> findByTenant_Id(int tenantId);
}
