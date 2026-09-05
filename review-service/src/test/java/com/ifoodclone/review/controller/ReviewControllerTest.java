package com.ifoodclone.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ifoodclone.review.config.GatewayUserContext.UserContext;
import com.ifoodclone.review.config.TestConfig;
import com.ifoodclone.review.entity.Review;
import com.ifoodclone.review.service.ReviewService;

@WebMvcTest(controllers = ReviewController.class)
@Import(TestConfig.class)
@DisplayName("Review Controller Tests")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        UserContext.clear();
    }

    @Test
    @DisplayName("POST /api/v1/reviews without gateway headers returns 401")
    void shouldRejectWithoutAuthHeaders() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":1,\"rating\":5}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/reviews creates a review")
    void shouldCreateReview() throws Exception {
        Review saved = Review.builder().id("r1").restaurantId(1L).userId(1L).rating(5).build();
        when(reviewService.create(any(), anyLong())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/reviews")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"restaurantId\":1,\"rating\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/reviews/restaurant/{id} returns the list from the service")
    void shouldListByRestaurant() throws Exception {
        when(reviewService.listByRestaurant(1L))
                .thenReturn(java.util.List.of(Review.builder().id("r1").restaurantId(1L).rating(4).build()));

        mockMvc.perform(get("/api/v1/reviews/restaurant/1")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].rating").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/reviews/{id} returns 404 when not found")
    void shouldReturn404WhenMissing() throws Exception {
        when(reviewService.getById("missing")).thenThrow(new RuntimeException("Avaliação não encontrada"));

        mockMvc.perform(get("/api/v1/reviews/missing")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "1")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/reviews/{id} returns 403 when requester is not the author")
    void shouldRejectDeleteFromNonAuthor() throws Exception {
        org.mockito.Mockito.doThrow(new SecurityException("Você não tem permissão para remover esta avaliação"))
                .when(reviewService).delete("r1", 999L, false);

        mockMvc.perform(delete("/api/v1/reviews/r1")
                .header("X-Authenticated", "true")
                .header("X-User-Id", "999")
                .header("X-User-Roles", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }
}
