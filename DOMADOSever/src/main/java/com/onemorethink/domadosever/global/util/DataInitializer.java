package com.onemorethink.domadosever.global.util;

import com.onemorethink.domadosever.domain.user.entity.Role;
import com.onemorethink.domadosever.domain.user.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        // 역할이 없을 경우에만 생성
        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = Role.builder()
                    .name("USER")
                    .build();
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .build();
            roleRepository.save(adminRole);
        }
    }
}