package com.workhub.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // look for the project-tenant-id header in the incoming request
        String tenantId = request.getHeader(TENANT_HEADER);

        // if the user sent one drop it into the bucket
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setTenantId(tenantId);
        }

        try {
            // let the request  go to the Controller
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}