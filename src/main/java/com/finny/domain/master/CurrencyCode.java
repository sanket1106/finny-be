package com.finny.domain.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "currency_codes")
@Getter
@Setter
public class CurrencyCode {

    @Id
    @Column(length = 3, nullable = false)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;
}
