package com.commerce.cloud.platform.outbound.wso2.client;

import com.commerce.cloud.platform.model.RoleBean;
import com.commerce.cloud.platform.outbound.wso2.client.exception.WSO2ClientException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WSO2RoleClient {

    @Autowired
    @Qualifier("wso2WebClient")
    private WebClient webClient;

    @Value("${wso2.role.api.context:/Roles}")  // Default value in case the property is not set
    private String roleAPIContext;

    /**
     * Fetches all roles from the WSO2 Identity Server SCIM API
     *
     * @return Flux<RoleBean> A reactive stream containing all roles
     */
    public Flux<RoleBean> findAllRole() {
        return webClient.get()
                .uri(roleAPIContext)
                .exchangeToFlux(clientResponse -> {
                    log.debug("Received response with status: {}", clientResponse.statusCode());
                    if (clientResponse.statusCode() == HttpStatus.OK) {
                        return clientResponse.bodyToMono(Map.class)
                                .doOnNext(responseMap -> log.debug("Response map received: {}", responseMap))
                                .flatMapMany(responseMap -> {
                                    List<Map<String, Object>> resources = (List<Map<String, Object>>) responseMap.get("Resources");
                                    if (resources == null || resources.isEmpty()) {
                                        log.warn("No roles found in response");
                                    }
                                    assert resources != null;
                                    return Flux.fromIterable(resources)
                                            .map(resource -> {
                                                RoleBean roleBean = new RoleBean();
                                                roleBean.setDisplayName((String) resource.get("displayName"));
                                                roleBean.setId((String) resource.get("id"));
                                                return roleBean;
                                            });
                                });
                    } else {
                        log.error("Error while fetching roles, status: {}", clientResponse.statusCode());
                        return Flux.error(new WSO2ClientException("Failed to fetch roles"));
                    }
                })
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("No roles found, returning empty Flux");
                    return Flux.empty();  // Return an empty Flux if no roles are found
                }))
                .onErrorResume(throwable -> {
                    log.error("Error in @WSO2RoleClient #findAllRole cause: {}", ExceptionUtils.getStackTrace(throwable));
                    return Flux.error(new WSO2ClientException(throwable.getLocalizedMessage(), throwable));
                });  // This will also log the flux stages
    }
}
