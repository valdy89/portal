package cz.mycom.veeam.portal;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import cz.mycom.veeam.portal.service.SessionIdThreadLocal;

/**
 * @author dursik
 */
@Slf4j
@Configuration
public class VeeamClientConfig {
    @Bean
    public RestTemplate veeamRestTemplate() {
        HttpComponentsClientHttpRequestFactory requestFactory = null;
        try {
            Properties merchantProperties = new Properties();
            merchantProperties.load(new FileInputStream("c:/app/conf/merchant.properties"));
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    new SSLContextBuilder().loadTrustMaterial(new File(merchantProperties.getProperty("keystore.file")), merchantProperties.getProperty("keystore.password").toCharArray(),
                            new TrustSelfSignedStrategy()).build(), new NoopHostnameVerifier());

            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();
            requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
        } catch (Exception e) {
            log.error("SSL init exception: " + e.getMessage());
            throw new IllegalStateException(e);
        }

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setMessageConverters(Arrays.asList(new Jaxb2RootElementHttpMessageConverter()));
        restTemplate.getInterceptors().add((httpRequest, bytes, clientHttpRequestExecution) -> {
            httpRequest.getHeaders().add("X-RestSvcSessionId", SessionIdThreadLocal.getSessionId());
            return clientHttpRequestExecution.execute(httpRequest, bytes);
        });
        return restTemplate;
    }
}