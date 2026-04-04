package com.workhub.dto;

import com.workhub.domain.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private Set<User.Role> roles;
    private Long tenantId;
}
