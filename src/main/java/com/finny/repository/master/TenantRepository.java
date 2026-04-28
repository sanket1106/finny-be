package com.finny.repository.master;

import com.finny.domain.master.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByName(String name);
    Optional<Tenant> findByDbName(String dbName);
}
