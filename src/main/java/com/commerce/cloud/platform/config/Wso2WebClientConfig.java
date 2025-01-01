package com.commerce.cloud.platform.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Base64;

@Configuration
@Slf4j
public class Wso2WebClientConfig {

    // Base URL and API Context to interact with the WSO2 Identity Server SCIM API
    @Value("${wso2.baseUrl:https://localhost:9443/scim2}")  // Default value if the property is not set
    private String baseUrl;

    @Value("${wso2.baseAuth.username:admin}")  // Default username if the property is not set
    private String username;

    @Value("${wso2.baseAuth.password:admin}")  // Corrected property for password
    private String password;

    // Truststore details (accessing nested properties)
    @Value("${wso2.webclient.ssl.trust-store}")
    private String trustStorePath;

    @Value("${wso2.webclient.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${wso2.webclient.ssl.trust-store-type}")
    private String trustStoreType;

    /**
     * WebClient bean to interact with WSO2 Identity Server SCIM API.
     * The client is configured with Basic Authentication headers.
     */
    @Bean
    public WebClient wso2WebClient() throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {

        // Encode the username and password as a Basic Auth string
        byte[] bytes = (username + ":" + password).getBytes();
        String basicAuth = Base64.getEncoder().encodeToString(bytes);

        // Log the Basic Auth header for debugging purposes (be cautious in production)
        log.debug("Configured WebClient with Basic Auth (username: {}): {}", username, basicAuth.substring(0, 10) + "...");

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(getHttpsClient()))
                .defaultHeaders(httpHeaders -> {
                    // Add the Authorization header to all requests
                    httpHeaders.add(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth);
                })
                .build();
    }

    private HttpClient getHttpsClient() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException {

        // Create an SSL context with the truststore
        KeyStore keyStore = KeyStore.getInstance(trustStoreType);
        try (var trustStoreStream = Files.newInputStream(Paths.get(trustStorePath))) {
            keyStore.load(trustStoreStream, trustStorePassword.toCharArray());
        }

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(trustManagerFactory)
                .build();

        // Load the custom truststore


        // Create an HttpClient with the custom SSL context
        return HttpClient.create().secure(t -> t.sslContext(sslContext));
    }
}
