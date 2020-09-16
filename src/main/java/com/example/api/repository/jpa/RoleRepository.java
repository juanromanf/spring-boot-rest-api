package com.example.api.repository.jpa;

import com.example.api.repository.jpa.entity.RoleEntity;
import com.example.api.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    
    Optional<RoleEntity> findByName(RoleName name);
    
}
