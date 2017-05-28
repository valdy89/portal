package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.repository.TenantRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dursik
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private TenantRepository tenantRepository;

    @RequestMapping(method = RequestMethod.GET)
    public AuthResponse login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Tenant tenant = tenantRepository.getOne(authentication.getName());
        return new AuthResponse(tenant);
    }

    @Data
    public static class AuthResponse {
        String firstname;
        String surname;
        String username;

        public AuthResponse(Tenant tenant) {
            this.firstname = tenant.getFirstname();
            this.surname = tenant.getSurname();
            this.username = tenant.getUsername();
        }
    }
}
