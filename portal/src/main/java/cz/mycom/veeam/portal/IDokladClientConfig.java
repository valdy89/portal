package cz.mycom.veeam.portal;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        restTemplate.setInterceptors(new ArrayList<>());
        //restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
        for (HttpMessageConverter messageConverter : restTemplate.getMessageConverters()) {
            if (messageConverter instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) messageConverter).setObjectMapper(objectMapper);
                objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
                objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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

    private static class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {

            traceRequest(request, body);
            ClientHttpResponse clientHttpResponse = execution.execute(request, body);
            //traceResponse(clientHttpResponse);

            return clientHttpResponse;
        }

        private void traceRequest(HttpRequest request, byte[] body) throws IOException {
            log.debug("request URI : " + request.getURI());
            log.debug("request method : " + request.getMethod());
            log.debug("request body : " + getRequestBody(body));
        }

        private String getRequestBody(byte[] body) throws UnsupportedEncodingException {
            if (body != null && body.length > 0) {
                return (new String(body, "UTF-8"));
            } else {
                return null;
            }
        }


        private void traceResponse(ClientHttpResponse response) throws IOException {
            String body = getBodyString(response);
            log.debug("response status code: " + response.getStatusCode());
            log.debug("response status text: " + response.getStatusText());
            log.debug("response body : " + body);
        }

        private String getBodyString(ClientHttpResponse response) {
            try {
                if (response != null && response.getBody() != null) {// &&
                    // isReadableResponse(response))
                    // {
                    StringBuilder inputStringBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
                    String line = bufferedReader.readLine();
                    while (line != null) {
                        inputStringBuilder.append(line);
                        inputStringBuilder.append('\n');
                        line = bufferedReader.readLine();
                    }
                    return inputStringBuilder.toString();
                } else {
                    return null;
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }
    }
}
