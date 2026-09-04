package com.ifoodclone.review.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ifoodclone.review.client.RestaurantClient;
import com.ifoodclone.review.dto.ReviewDto;
import com.ifoodclone.review.entity.Review;
import com.ifoodclone.review.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantClient restaurantClient;

    public ReviewService(ReviewRepository reviewRepository, RestaurantClient restaurantClient) {
        this.reviewRepository = reviewRepository;
        this.restaurantClient = restaurantClient;
    }

    public Review create(ReviewDto.CreateRequest request, Long userId) {
        restaurantClient.getActiveRestaurant(request.getRestaurantId());

        if (request.getOrderId() != null && reviewRepository.existsByRestaurantIdAndUserIdAndOrderId(
                request.getRestaurantId(), userId, request.getOrderId())) {
            throw new IllegalStateException("Você já avaliou este pedido");
        }

        Review review = Review.builder()
                .restaurantId(request.getRestaurantId())
                .userId(userId)
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> listByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    public List<Review> listMine(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Review getById(String id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));
    }

    public void delete(String id, Long userId, boolean isAdmin) {
        Review review = getById(id);
        if (!review.getUserId().equals(userId) && !isAdmin) {
            throw new SecurityException("Você não tem permissão para remover esta avaliação");
        }
        reviewRepository.delete(review);
    }

    public ReviewDto.RestaurantRatingSummary getRestaurantRatingSummary(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return ReviewDto.RestaurantRatingSummary.builder()
                .restaurantId(restaurantId)
                .averageRating(Math.round(average * 10.0) / 10.0)
                .totalReviews((long) reviews.size())
                .build();
    }
}
