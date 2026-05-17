package com.billingplatformapplication.roles.repository;

import com.billingplatformapplication.roles.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);

    List<RoleEntity> findByActiveTrue();

    @Query("SELECT r FROM RoleEntity r LEFT JOIN FETCH r.permissions WHERE r.name = :name")
    Optional<RoleEntity> findByNameWithPermissions(@Param("name") String name);
}
