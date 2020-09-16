package com.example.api.service;

import com.example.api.domain.AuthenticationResult;
import com.example.api.domain.RoleName;
import com.example.api.domain.User;
import com.example.api.repository.jpa.RoleRepository;
import com.example.api.repository.jpa.UserRepository;
import com.example.api.repository.jpa.entity.RoleEntity;
import com.example.api.repository.jpa.entity.UserEntity;
import com.example.api.service.security.JwtManager;
import com.example.api.utils.ReactorUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsersService {
    
    private UserRepository userRepository;
    
    private RoleRepository roleRepository;
    
    private PasswordEncoder encoder;
    
    private JwtManager jwtManager;
    
    public UsersService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder encoder,
            JwtManager jwtManager) {
        
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.jwtManager = jwtManager;
    }
    
    public Mono<AuthenticationResult> authenticateUser(String username, String password) {
        
        return ReactorUtils.toMono(() -> userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("user not found")))
                .map(userEntity -> toDomainType(userEntity))
                .map(user -> {
                    if (encoder.matches(password, user.getPassword())) {
                        String token = jwtManager.generateToken(user);
                        return AuthenticationResult.builder()
                                .success(true)
                                .token(token)
                                .expiresOn(jwtManager.getExpirationDateFromToken(token))
                                .user(user)
                                .build();
                    }
                    return AuthenticationResult.builder()
                            .success(false)
                            .errorMessage("user or password not match")
                            .build();
                });
    }
    
    public Mono<User> registerUser(User user) {
        
        return ReactorUtils.toMono(() -> userRepository.existsByUsername(user.getUsername()))
                .filter(exists -> !exists)
                .map(ignore -> {
                    
                    Set<RoleEntity> userRoles = new HashSet<>();
                    if (Objects.isNull(user.getRoles())) {
                        
                        userRoles.add(findRoleEntity(RoleName.ROLE_USER));
                        
                    } else {
                        Set<RoleEntity> roleEntities = user.getRoles().stream()
                                .map(r -> {
                                    switch (r) {
                                        case "admin":
                                            return findRoleEntity(RoleName.ROLE_ADMIN);
                                        case "moderator":
                                            return findRoleEntity(RoleName.ROLE_MODERATOR);
                                        default:
                                            return findRoleEntity(RoleName.ROLE_USER);
                                    }
                                }).collect(Collectors.toSet());
                        
                        userRoles.addAll(roleEntities);
                    }
                    
                    return UserEntity.builder()
                            .username(user.getUsername())
                            .password(encoder.encode(user.getPassword()))
                            .roles(userRoles)
                            .createdAt(LocalDateTime.now())
                            .build();
                    
                })
                .map(userEntity -> userRepository.save(userEntity))
                .map(userEntity -> toDomainType(userEntity));
    }
    
    private RoleEntity findRoleEntity(RoleName roleName) {
        
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }
    
    private User toDomainType(UserEntity userEntity) {
        
        return User.builder()
                .id(userEntity.getId().toString())
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(userEntity.getRoles().stream()
                        .map(e -> e.getName().name()).collect(Collectors.toSet()))
                .build();
    }
    
}
