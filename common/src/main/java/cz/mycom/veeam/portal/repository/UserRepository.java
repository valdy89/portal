package cz.mycom.veeam.portal.repository;

import cz.mycom.veeam.portal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author dursik
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
