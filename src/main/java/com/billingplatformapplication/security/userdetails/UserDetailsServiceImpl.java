package com.billingplatformapplication.security.userdetails;

import com.billingplatformapplication.users.entity.UserEntity;
import com.billingplatformapplication.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.userdetails.User;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailWithRolesAndPermissions(email)
                .orElseThrow(() -> {
                    log.warn("Auth attempt for unknown user: {}", email);
                    return new UsernameNotFoundException("Authentication failed");
                });
        return buildUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByUserId(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        return buildUserDetails(user);
    }

    private UserDetails buildUserDetails(UserEntity user) {
        // ROLE_ADMIN, ROLE_FINANCE, etc.
        Stream<SimpleGrantedAuthority> roleAuthorities = user.getRoles().stream()
                .filter(r -> r.isActive())
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()));

        // CREATE_CLIENT, READ_RATE, etc.
        Stream<SimpleGrantedAuthority> permissionAuthorities = user.getRoles().stream()
                .filter(r -> r.isActive())
                .flatMap(r -> r.getPermissions().stream())
                .filter(p -> p.isActive())
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .distinct();

        List<SimpleGrantedAuthority> authorities = Stream
                .concat(roleAuthorities, permissionAuthorities)
                .collect(Collectors.toList());

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.isActive())
                .accountLocked(user.isLocked())
                .accountExpired(false)
                .credentialsExpired(false)
                .authorities(authorities)
                .build();
    }
}
