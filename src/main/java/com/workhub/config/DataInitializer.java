package com.workhub.config;

import com.workhub.model.Tenant;
import com.workhub.model.User;
import com.workhub.repository.TenantRepository;
import com.workhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initData(TenantRepository tenantRepository, UserRepository userRepository) {
        return args -> {
            if (tenantRepository.count() == 0) {
                Tenant tenantA = tenantRepository.save(Tenant.builder()
                        .name("Tenant A")
                        .plan(Tenant.Plan.FREE)
                        .build());

                userRepository.save(User.builder()
                        .email("admin@tenantA.com")
                        .password("password")
                        .roles(Set.of(User.Role.TENANT_ADMIN))
                        .tenantId(tenantA.getId())
                        .build());

                Tenant tenantB = tenantRepository.save(Tenant.builder()
                        .name("Tenant B")
                        .plan(Tenant.Plan.PREMIUM)
                        .build());

                userRepository.save(User.builder()
                        .email("admin@tenantB.com")
                        .password("password")
                        .roles(Set.of(User.Role.TENANT_ADMIN))
                        .tenantId(tenantB.getId())
                        .build());

                log.info("Initial data seeded for Tenant A and Tenant B");
            }
        };
    }
}
