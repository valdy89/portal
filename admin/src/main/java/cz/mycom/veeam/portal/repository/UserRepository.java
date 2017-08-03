package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author dursik
 */
public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
