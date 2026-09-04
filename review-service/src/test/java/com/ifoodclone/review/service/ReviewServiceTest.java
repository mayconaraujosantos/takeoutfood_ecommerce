package com.ifoodclone.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ifoodclone.review.client.RestaurantClient;
import com.ifoodclone.review.client.RestaurantSummary;
import com.ifoodclone.review.dto.ReviewDto;
import com.ifoodclone.review.entity.Review;
import com.ifoodclone.review.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Review Service Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RestaurantClient restaurantClient;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, restaurantClient);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("Should create review when restaurant is active")
        void shouldCreateReview() {
            RestaurantSummary restaurant = new RestaurantSummary();
            restaurant.setId(1L);
            restaurant.setActive(true);
            when(restaurantClient.getActiveRestaurant(1L)).thenReturn(restaurant);
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

            ReviewDto.CreateRequest request = ReviewDto.CreateRequest.builder()
                    .restaurantId(1L)
                    .rating(5)
                    .comment("Ótimo!")
                    .build();

            Review result = reviewService.create(request, 42L);

            assertThat(result.getRestaurantId()).isEqualTo(1L);
            assertThat(result.getUserId()).isEqualTo(42L);
            assertThat(result.getRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should reject when restaurant client throws")
        void shouldRejectWhenRestaurantInvalid() {
            when(restaurantClient.getActiveRestaurant(1L))
                    .thenThrow(new RuntimeException("Restaurante não encontrado ou inativo"));

            ReviewDto.CreateRequest request = ReviewDto.CreateRequest.builder()
                    .restaurantId(1L)
                    .rating(5)
                    .build();

            assertThatThrownBy(() -> reviewService.create(request, 42L))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should reject duplicate review for the same order")
        void shouldRejectDuplicateReviewForSameOrder() {
            RestaurantSummary restaurant = new RestaurantSummary();
            restaurant.setId(1L);
            restaurant.setActive(true);
            when(restaurantClient.getActiveRestaurant(1L)).thenReturn(restaurant);
            when(reviewRepository.existsByRestaurantIdAndUserIdAndOrderId(1L, 42L, 100L)).thenReturn(true);

            ReviewDto.CreateRequest request = ReviewDto.CreateRequest.builder()
                    .restaurantId(1L)
                    .orderId(100L)
                    .rating(4)
                    .build();

            assertThatThrownBy(() -> reviewService.create(request, 42L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("já avaliou");
        }
    }

    @Nested
    @DisplayName("delete")
    class DeleteTests {

        @Test
        @DisplayName("Should allow the author to delete their own review")
        void shouldAllowAuthorToDelete() {
            Review review = Review.builder().id("r1").userId(42L).build();
            when(reviewRepository.findById("r1")).thenReturn(Optional.of(review));

            reviewService.delete("r1", 42L, false);

            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("Should allow an admin to delete someone else's review")
        void shouldAllowAdminToDelete() {
            Review review = Review.builder().id("r1").userId(42L).build();
            when(reviewRepository.findById("r1")).thenReturn(Optional.of(review));

            reviewService.delete("r1", 999L, true);

            verify(reviewRepository).delete(review);
        }

        @Test
        @DisplayName("Should reject deletion from a non-author, non-admin user")
        void shouldRejectDeleteFromNonAuthor() {
            Review review = Review.builder().id("r1").userId(42L).build();
            when(reviewRepository.findById("r1")).thenReturn(Optional.of(review));

            assertThatThrownBy(() -> reviewService.delete("r1", 999L, false))
                    .isInstanceOf(SecurityException.class);
        }
    }

    @Nested
    @DisplayName("getRestaurantRatingSummary")
    class SummaryTests {

        @Test
        @DisplayName("Should compute average rating and total count")
        void shouldComputeAverage() {
            when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(
                    Review.builder().rating(5).build(),
                    Review.builder().rating(3).build()));

            ReviewDto.RestaurantRatingSummary summary = reviewService.getRestaurantRatingSummary(1L);

            assertThat(summary.getTotalReviews()).isEqualTo(2L);
            assertThat(summary.getAverageRating()).isEqualTo(4.0);
        }

        @Test
        @DisplayName("Should return zero average when there are no reviews")
        void shouldReturnZeroWhenNoReviews() {
            when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

            ReviewDto.RestaurantRatingSummary summary = reviewService.getRestaurantRatingSummary(1L);

            assertThat(summary.getTotalReviews()).isEqualTo(0L);
            assertThat(summary.getAverageRating()).isEqualTo(0.0);
        }
    }
}
