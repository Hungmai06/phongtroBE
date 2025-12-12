package com.example.room.specification;

import com.example.room.model.Room;
import com.example.room.utils.Enums.RoomStatus;
import com.example.room.utils.Enums.RoomType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class RoomSpecification {

    // Thêm ownerId vào đây 👇
    public static Specification<Room> filterRooms(String q,
                                                  BigDecimal minPrice,
                                                  BigDecimal maxPrice,
                                                  Float minArea,
                                                  String type,
                                                  String status,
                                                  Long ownerId) {

        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            // Từ khóa
            if (q != null && !q.isEmpty()) {
                String likeQ = "%" + q.toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("name")), likeQ),
                        cb.like(cb.lower(root.get("address")), likeQ),
                        cb.like(cb.lower(root.get("description")), likeQ)
                ));
            }

            // Loại phòng
            if (type != null && !type.isEmpty()) {
                RoomType roomType = RoomType.valueOf(type);
                predicate = cb.and(predicate, cb.equal(root.get("type"), roomType));
            }

            // Trạng thái phòng
            if (status != null && !status.isEmpty()) {
                RoomStatus roomStatus = RoomStatus.valueOf(status);
                predicate = cb.and(predicate, cb.equal(root.get("status"), roomStatus));
            }

            // Giá
            if (minPrice != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // Diện tích
            if (minArea != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("area"), minArea));
            }

            // ⭐ THÊM LỌC THEO OWNER ⭐
            if (ownerId != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("owner").get("id"), ownerId)
                );
            }

            return predicate;
        };
    }
}
