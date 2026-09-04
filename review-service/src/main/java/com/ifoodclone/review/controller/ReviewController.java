package com.ifoodclone.review.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifoodclone.review.config.GatewayUserContext.UserContext;
import com.ifoodclone.review.dto.ReviewDto;
import com.ifoodclone.review.dto.ReviewDto.ApiResponse;
import com.ifoodclone.review.dto.ReviewDto.ReviewInfo;
import com.ifoodclone.review.entity.Review;
import com.ifoodclone.review.service.ReviewService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Reviews", description = "Avaliações de restaurantes")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewInfo>> create(@Valid @RequestBody ReviewDto.CreateRequest request) {
        try {
            Review review = reviewService.create(request, UserContext.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Avaliação registrada com sucesso", ReviewInfo.from(review)));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Falha ao registrar avaliação", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Falha ao registrar avaliação", ex.getMessage()));
        }
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<ReviewInfo>>> listByRestaurant(@PathVariable Long restaurantId) {
        List<ReviewInfo> reviews = reviewService.listByRestaurant(restaurantId).stream()
                .map(ReviewInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/restaurant/{restaurantId}/summary")
    public ResponseEntity<ApiResponse<ReviewDto.RestaurantRatingSummary>> getRestaurantRatingSummary(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getRestaurantRatingSummary(restaurantId)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReviewInfo>>> listMine() {
        List<ReviewInfo> reviews = reviewService.listMine(UserContext.getUserId()).stream()
                .map(ReviewInfo::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewInfo>> getById(@PathVariable String id) {
        try {
            Review review = reviewService.getById(id);
            return ResponseEntity.ok(ApiResponse.success(ReviewInfo.from(review)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            reviewService.delete(id, UserContext.getUserId(), UserContext.isAdmin());
            return ResponseEntity.ok(ApiResponse.success("Avaliação removida com sucesso", null));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }
}
