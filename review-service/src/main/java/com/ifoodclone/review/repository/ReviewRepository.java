package com.ifoodclone.review.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.ifoodclone.review.entity.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByRestaurantIdAndUserIdAndOrderId(Long restaurantId, Long userId, Long orderId);
}
