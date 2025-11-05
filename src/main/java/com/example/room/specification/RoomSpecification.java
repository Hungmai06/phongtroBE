package com.example.room.specification;

import com.example.room.model.Room;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class RoomSpecification {
    public static Specification<Room> filterRooms(String q, BigDecimal minPrice, BigDecimal maxPrice, Float minArea) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Lọc theo từ khóa chung
            if (q != null && !q.isEmpty()) {
                String likeQ = "%" + q.toLowerCase() + "%";
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeQ),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), likeQ),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likeQ)
                ));
            }

            // Lọc theo khoảng giá
            if (minPrice != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Lọc theo diện tích tối thiểu
            if (minArea != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("area"), minArea));
            }

            return predicate;
        };
    }
}