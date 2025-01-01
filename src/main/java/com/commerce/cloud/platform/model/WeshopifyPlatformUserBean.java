package com.commerce.cloud.platform.model;


import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.Serializable;

@Data
@Builder
public class WeshopifyPlatformUserBean implements Serializable {
    private int userId;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String role;
    private boolean status;
    private File photos;
}
