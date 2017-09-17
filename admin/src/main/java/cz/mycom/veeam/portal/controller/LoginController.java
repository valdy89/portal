package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dursik
 */
@RestController
@Transactional
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @RequestMapping(method = RequestMethod.GET)
    public AuthResponse login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName());
        return new AuthResponse(user);
    }

    @RequestMapping(method = RequestMethod.POST)
    public void post(@RequestBody String password) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName());
        user.setPassword(passwordEncoder.encode(password));
    }

    @Data
    public static class AuthResponse {
        String name;
        String username;

        public AuthResponse(User user) {
            this.name = user.getName();
            this.username = user.getUsername();
        }
    }
}
