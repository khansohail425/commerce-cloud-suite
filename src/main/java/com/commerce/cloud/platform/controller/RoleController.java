package com.commerce.cloud.platform.controller;

import com.commerce.cloud.platform.model.RoleBean;
import com.commerce.cloud.platform.outbound.wso2.client.WSO2RoleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("roles")
public class RoleController {

    @Autowired
    private WSO2RoleClient wso2RoleClient;

    @GetMapping
    public Flux<RoleBean> getAllRoles() {
        return wso2RoleClient.findAllRole();
    }

}
