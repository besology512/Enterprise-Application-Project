package com.workhub.dto;

import com.workhub.model.User; // Updated to import from model
import lombok.Data;
import java.util.Set;

@Data
public class UserResponse {
    private String email;
    private Set<User.Role> roles;
    private Long tenantId;

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.roles = user.getRoles();
        this.tenantId = user.getTenantId();
    }
}
