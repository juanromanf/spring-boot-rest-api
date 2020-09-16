package com.example.api.repository.jpa;

import com.example.api.repository.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.roles r WHERE u.username = :username")
    Optional<UserEntity> findByUsername(String username);
    
    Boolean existsByUsername(String username);
}
