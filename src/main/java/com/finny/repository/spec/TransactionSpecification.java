package com.finny.repository.spec;

import com.finny.domain.Transaction;
import com.finny.dto.TransactionFilterDto;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> withFilter(TransactionFilterDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter != null) {
                if (filter.getAccountId() != null && !filter.getAccountId().isBlank()) {
                    predicates.add(cb.equal(root.get("account").get("id"), filter.getAccountId()));
                }
                if (filter.getToAccountId() != null && !filter.getToAccountId().isBlank()) {
                    predicates.add(cb.equal(root.get("toAccount").get("id"), filter.getToAccountId()));
                }
                if (filter.getFromDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), filter.getFromDate()));
                }
                if (filter.getToDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), filter.getToDate()));
                }
                if (filter.getCategoryIds() != null && !filter.getCategoryIds().isEmpty()) {
                    predicates.add(root.get("category").get("id").in(filter.getCategoryIds()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
