package com.workhub;

import com.workhub.domain.Tenant;
import com.workhub.domain.User;
import com.workhub.repository.TenantRepository;
import com.workhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (tenantRepository.count() == 0) {
            Tenant tenantA = tenantRepository.save(Tenant.builder()
                    .name("Tenant A")
                    .plan(Tenant.Plan.FREE)
                    .build());
            Tenant tenantB = tenantRepository.save(Tenant.builder()
                    .name("Tenant B")
                    .plan(Tenant.Plan.PREMIUM)
                    .build());

            userRepository.save(User.builder()
                    .email("admin@tenantA.com")
                    .password(passwordEncoder.encode("password"))
                    .roles(Set.of(User.Role.TENANT_ADMIN))
                    .tenantId(tenantA.getId())
                    .build());

            userRepository.save(User.builder()
                    .email("user@tenantA.com")
                    .password(passwordEncoder.encode("password"))
                    .roles(Set.of(User.Role.TENANT_USER))
                    .tenantId(tenantA.getId())
                    .build());

            userRepository.save(User.builder()
                    .email("admin@tenantB.com")
                    .password(passwordEncoder.encode("password"))
                    .roles(Set.of(User.Role.TENANT_ADMIN))
                    .tenantId(tenantB.getId())
                    .build());

            System.out.println("Initial data seeded for Tenant A and Tenant B");
        }
    }
}
