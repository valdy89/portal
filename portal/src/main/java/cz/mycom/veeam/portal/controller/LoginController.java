package cz.mycom.veeam.portal.controller;

import cz.mycom.veeam.portal.model.User;
import cz.mycom.veeam.portal.repository.UserRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Properties;

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

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
    public void forgotPassword(@RequestBody AuthRequest authRequest) {
        try {
            User user = userRepository.findByUsername(authRequest.getUsername());
            String password = RandomStringUtils.randomAlphanumeric(10);
            user.setEnabled(false);
            try {
                sendMail("Zapomenuté heslo", user.getUsername(), password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            user.setPassword(passwordEncoder.encode(password));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public AuthResponse changePassword(@RequestBody AuthRequest authRequest) {
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
            return new AuthResponse(user);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@RequestBody User user) {
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
            sendMail("Potvrzení registrace", user.getUsername(), user.getPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        user.setPassword(userDetails.getPassword());
        user.setDateCreated(new Date());
        userRepository.save(user);
    }

    private void sendMail(String subject, String email, String tempPasswd) throws Exception {

        final String username = "veeam.portal@gmail.com";
        final String password = "iddqd_2017";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("veeam.portal@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("dursik@gmail.com"));
            message.setSubject(subject);
            String text = "<a href='https://portal.dursik.eu/verify?code=" + Base64.encodeBase64URLSafeString((email + ":" + tempPasswd).getBytes("UTF-8")) + "'>Ověřit</a>";
            log.debug(text);
            message.setText(text);

            //Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    @Data
    public static class AuthRequest {
        String username;
        String password;
        String code;
    }

    @Data
    public static class AuthResponse {
        String firstname;
        String surname;
        String username;

        public AuthResponse(User user) {
            this.firstname = user.getFirstname();
            this.surname = user.getSurname();
            this.username = user.getUsername();
        }
    }
}
