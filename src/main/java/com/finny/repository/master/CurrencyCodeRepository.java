package com.finny.repository.master;

import com.finny.domain.master.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyCodeRepository extends JpaRepository<CurrencyCode, String> {
}
