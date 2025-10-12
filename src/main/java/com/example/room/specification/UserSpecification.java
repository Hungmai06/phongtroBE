package com.example.room.specification;

import com.example.room.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification  {
    public static Specification<User> filter ( String q ){
        return (root, query, criteriaBuilder) ->
        {
            Predicate predicate = criteriaBuilder.conjunction();

            if(q != null && !q.isEmpty()){
                String likeQ = "%"+q+ "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(root.get("fullName"),likeQ),
                        criteriaBuilder.like(root.get("email"),likeQ),
                        criteriaBuilder.like(root.get("phone"),likeQ),
                        criteriaBuilder.like(root.get("address"),likeQ),
                        criteriaBuilder.like(root.get("citizenId"),likeQ)
                );
                predicate = criteriaBuilder.and(predicate,searchPredicate);
            }

            return predicate;
        };
    }
}
