package com.billingplatformapplication.users.repository;


import com.billingplatformapplication.users.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM UserEntity u " +
            "LEFT JOIN FETCH u.roles r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE LOWER(u.email) = LOWER(:email)")
    Optional<UserEntity> findByEmailWithRolesAndPermissions(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u WHERE " +
            "(:search IS NULL " +
            " OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR u.status = :status)")
    Page<UserEntity> findAllWithFilters(
            @Param("search") String search,
            @Param("status") UserEntity.UserStatus status,
            Pageable pageable);
}

