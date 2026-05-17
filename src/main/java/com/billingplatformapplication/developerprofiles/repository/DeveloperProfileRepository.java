package com.billingplatformapplication.developerprofiles.repository;

import com.billingplatformapplication.developerprofiles.entity.DeveloperProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeveloperProfileRepository extends JpaRepository<DeveloperProfileEntity, UUID> {

    boolean existsByName(String name);
    List<DeveloperProfileEntity> findByActiveTrue();

    @Query("SELECT dp FROM DeveloperProfileEntity dp WHERE dp.active = true AND " +
            "(:search IS NULL OR LOWER(dp.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DeveloperProfileEntity> searchActive(@Param("search") String search, Pageable pageable);
}
