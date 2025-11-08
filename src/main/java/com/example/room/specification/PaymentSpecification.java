package com.example.room.specification;

import com.example.room.model.Payment;
import com.example.room.utils.Enums.PaymentMethod;
import com.example.room.utils.Enums.PaymentStatus;
import com.example.room.utils.Enums.PaymentType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    public static Specification<Payment> filter(
            Long bookingId,
            PaymentType paymentType,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            LocalDateTime paymentDate,
            LocalDate paymentPeriod
    ) {

        if (bookingId == null && paymentType == null && paymentMethod == null && paymentStatus == null && paymentDate == null && paymentPeriod == null) {
            return null;
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookingId != null) {
                predicates.add(cb.equal(root.get("booking").get("id"), bookingId));
            }
            if (paymentType != null) {
                predicates.add(cb.equal(root.get("paymentType"), paymentType));
            }
            if (paymentMethod != null) {
                predicates.add(cb.equal(root.get("paymentMethod"), paymentMethod));
            }
            if (paymentStatus != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            }
            if (paymentDate != null) {
                predicates.add(cb.equal(root.get("paymentDate"), paymentDate));
            }
            if (paymentPeriod != null) {
                predicates.add(cb.equal(root.get("paymentPeriod"), paymentPeriod));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
