package cz.mycom.veeam.portal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.util.Arrays;

/**
 * @author dursik
 */
@Slf4j
@Configuration
@EnableOAuth2Client
public class IDokladClientConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public OAuth2RestTemplate iDokladRestTemplate(OAuth2ClientContext oauth2ClientContext) {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(iDoklad(), oauth2ClientContext);
        for (HttpMessageConverter messageConverter : restTemplate.getMessageConverters()) {
            if (messageConverter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) messageConverter).setObjectMapper(objectMapper);
            }
        }
        return restTemplate;
    }

    private OAuth2ProtectedResourceDetails iDoklad() {
        ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
        resourceDetails.setClientId("6008ec7c-73ab-40b7-858e-0ba20b430f78");
        resourceDetails.setClientSecret("48aef37e-8de6-4db0-8dfa-442c7d14a068");
        resourceDetails.setScope(Arrays.asList("idoklad_api"));
        resourceDetails.setAccessTokenUri("https://app.idoklad.cz/identity/server/connect/token");
        resourceDetails.setTokenName("my_token");
        return resourceDetails;
    }
}
