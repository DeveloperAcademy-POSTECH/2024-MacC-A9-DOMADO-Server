package com.onemorethink.domadosever.domain.user.repository;

import com.onemorethink.domadosever.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
