package com.example.room.specification;


import com.example.room.model.Help;
import org.springframework.data.jpa.domain.Specification;

public class ContactMessageSpecification {

    public static Specification<Help> filter(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction(); // không filter gì cả
            }

            String likeQ = "%" + q.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), likeQ),
                    cb.like(cb.lower(root.get("email")), likeQ),
                    cb.like(cb.lower(root.get("subject")), likeQ)
            );
        };
    }
}