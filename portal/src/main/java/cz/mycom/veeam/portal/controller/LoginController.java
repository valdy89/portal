package cz.mycom.veeam.portal.controller;

import com.veeam.ent.v1.CloudTenant;
import com.veeam.ent.v1.CreateCloudTenantSpec;
import com.veeam.ent.v1.LogonSession;
import cz.mycom.veeam.portal.model.Tenant;
import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.TenantRepository;
import cz.mycom.veeam.portal.repository.UserRepository;
import cz.mycom.veeam.portal.service.MailService;
import cz.mycom.veeam.portal.service.VeeamService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dursik
 */
@Slf4j
@RestController
@Transactional
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private MailService mailService;
    @Autowired
    private VeeamService veeamService;
    @Autowired
    private TenantRepository tenantRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName());
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public void save(@RequestBody User user, Principal principal) {
        User userEntity = userRepository.findByUsername(principal.getName());
        userEntity.setCity(user.getCity());
        userEntity.setCountry("CZ");
        userEntity.setDic(user.getDic());
        userEntity.setIco(user.getIco());
        userEntity.setName(user.getName());
        userEntity.setPhone(user.getPhone());
        userEntity.setPostalCode(user.getPostalCode());
        userEntity.setStreet(user.getStreet());
        userEntity.setEmail(user.getEmail());
    }

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public AuthResponse token(Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        return new AuthResponse(user);
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public AuthResponse login(@RequestBody AuthRequest authRequest) {
        User user = userRepository.findByUsername(authRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("Uživatelské jméno neexistuje");
        }
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Nesprávne jméno nebo heslo");
        }
        if (!user.isEnabled()) {
            throw new RuntimeException("Uživatel je zablokován");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username);
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
            throw new RuntimeException("Uživatel nemá přístup do portálu");
        }

        return new AuthResponse(user);
    }

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    public void forgotPassword(@RequestBody AuthRequest authRequest, HttpServletRequest request) {
        try {
            User user = userRepository.findByUsername(authRequest.getUsername());
            String password = RandomStringUtils.randomAlphanumeric(10);
            user.setEnabled(false);
            String text = "https://" + request.getServerName() + "/verify?code=" + Base64.encodeBase64URLSafeString((user.getUsername() + ":" + password).getBytes("UTF-8"));
            mailService.sendMail(user.getEmail(), "Zapomenuté heslo", text);
            user.setPassword(passwordEncoder.encode(password));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public void changePassword(@RequestBody AuthRequest authRequest, Principal principal) {
        User user = userRepository.findByUsername(principal.getName());
        if (!passwordEncoder.matches(authRequest.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Nesprávne původní heslo");
        }
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        LogonSession logonSession = veeamService.logonSystem();
        try {
            String tenantUid = user.getTenant().getUid();
            CloudTenant cloudTenant = veeamService.getTenant(tenantUid);
            cloudTenant.setPassword(authRequest.getPassword());
            veeamService.saveTenant(tenantUid, cloudTenant);
        } catch (Exception e) {
            veeamService.logout(logonSession);
        }
    }

    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    public AuthResponse verify(@RequestBody AuthRequest authRequest) {
        try {
            String pomCode = new String(Base64.decodeBase64(authRequest.getCode()), "UTF-8");
            String username = StringUtils.substringBefore(pomCode, ":");
            String password = StringUtils.substringAfter(pomCode, ":");
            User user = userRepository.findByUsername(username);
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Nesprávne jméno nebo heslo");
            }
            user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
            user.setEnabled(true);
            user.setEmail(user.getUsername());

            if (user.getTenant() == null) {

                LogonSession logonSession = veeamService.logonSystem();
                try {
                    List<String> descriptionList = new ArrayList<>();
                    descriptionList.add("Email: " + user.getEmail());
                    descriptionList.add("Portal tenant - " + DateFormatUtils.format(new Date(), "dd.MM.yyyy HH:mm:ss"));

                    Tenant tenant = new Tenant();
                    tenant.setUser(user);
                    tenant.setDateCreated(new Date());
                    tenant.setEnabled(true);

                    String name = StringUtils.substringBefore(username, "@");
                    name += RandomStringUtils.randomNumeric(5);

                    tenant.setUsername(name);

                    CreateCloudTenantSpec cloudTenant = new CreateCloudTenantSpec();
                    cloudTenant.setName(name);
                    cloudTenant.setDescription(StringUtils.join(descriptionList, IOUtils.LINE_SEPARATOR_WINDOWS));

                    cloudTenant.setPassword(authRequest.getPassword());
                    cloudTenant.setEnabled(true);
                    cloudTenant.setThrottlingEnabled(false);
                    cloudTenant.setMaxConcurrentTasks(5);
                    cloudTenant.setBackupServerUid(veeamService.getBackupServerUUID());

                    CloudTenant saveTenant = veeamService.createTenant(cloudTenant);
                    tenant.setUid(StringUtils.substringAfterLast(saveTenant.getUID(), ":"));
                    tenantRepository.save(tenant);
                } finally {
                    veeamService.logout(logonSession);
                }
            }
            return new AuthResponse(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@RequestBody User user, HttpServletRequest request) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Uživatelské jméno již existuje");
        }
        user.setPassword(RandomStringUtils.randomAlphanumeric(10));
        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .disabled(true)
                .password(passwordEncoder.encode(user.getPassword()))
                .authorities("ROLE_USER")
                .build();
        ((JdbcUserDetailsManager) userDetailsService).createUser(userDetails);
        try {

            String text = "https://" + request.getServerName() + "/verify?code=" + Base64.encodeBase64URLSafeString((user.getUsername() + ":" + user.getPassword()).getBytes("UTF-8"));
            mailService.sendMail(user.getUsername(), "Potvrzení registrace", text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    public static class AuthRequest {
        String username;
        String password;
        String oldPassword;
        String code;
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
