package com.workhub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long tenantId;

    public JwtResponse(String token, Long tenantId) {
        this.token = token;
        this.tenantId = tenantId;
    }
}
