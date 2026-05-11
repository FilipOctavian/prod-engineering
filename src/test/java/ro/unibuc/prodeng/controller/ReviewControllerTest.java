package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.request.CreateReviewRequest;
import ro.unibuc.prodeng.response.ReviewResponse;
import ro.unibuc.prodeng.service.ReviewService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final ReviewResponse review1 = new ReviewResponse("r-1", "mech-1", "order-1", 5, "Great work", LocalDateTime.of(2026, 1, 1, 10, 0));
    private final ReviewResponse review2 = new ReviewResponse("r-2", "mech-1", "order-2", 4, "Good job", LocalDateTime.of(2026, 1, 2, 10, 0));
    private final CreateReviewRequest createRequest = new CreateReviewRequest("mech-1", "order-1", 5, "Great work");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    }

    @Test
    void testCreateReview_validRequestProvided_returnsCreatedReview() throws Exception {
        when(reviewService.createReview(any(CreateReviewRequest.class))).thenReturn(review1);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("r-1")))
                .andExpect(jsonPath("$.mechanicId", is("mech-1")))
                .andExpect(jsonPath("$.rating", is(5)))
                .andExpect(jsonPath("$.comment", is("Great work")));

        verify(reviewService, times(1)).createReview(any(CreateReviewRequest.class));
    }

    @Test
    void testGetReviewsForMechanic_mechanicWithReviews_returnsList() throws Exception {
        List<ReviewResponse> reviews = Arrays.asList(review1, review2);
        when(reviewService.getReviewsForMechanic("mech-1")).thenReturn(reviews);

        mockMvc.perform(get("/api/reviews/mechanic/{mechanicId}", "mech-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("r-1")))
                .andExpect(jsonPath("$[0].rating", is(5)))
                .andExpect(jsonPath("$[1].id", is("r-2")))
                .andExpect(jsonPath("$[1].rating", is(4)));

        verify(reviewService, times(1)).getReviewsForMechanic("mech-1");
    }

    @Test
    void testGetReviewsForMechanic_mechanicNotFound_returnsNotFound() throws Exception {
        when(reviewService.getReviewsForMechanic("unknown"))
                .thenThrow(new EntityNotFoundException("Mechanic with id: unknown"));

        mockMvc.perform(get("/api/reviews/mechanic/{mechanicId}", "unknown")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).getReviewsForMechanic("unknown");
    }

    @Test
    void testGetAverageRating_mechanicWithRating_returnsScore() throws Exception {
        when(reviewService.getAverageRating("mech-1")).thenReturn(4.5);

        mockMvc.perform(get("/api/reviews/mechanic/{mechanicId}/average", "mech-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(4.5)));

        verify(reviewService, times(1)).getAverageRating("mech-1");
    }

    @Test
    void testDeleteReview_existingReviewRequested_returnsNoContent() throws Exception {
        doNothing().when(reviewService).deleteReview("r-1");

        mockMvc.perform(delete("/api/reviews/{id}", "r-1"))
                .andExpect(status().isNoContent());

        verify(reviewService, times(1)).deleteReview("r-1");
    }

    @Test
    void testDeleteReview_nonExistingReviewRequested_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Review with id: missing"))
                .when(reviewService).deleteReview("missing");

        mockMvc.perform(delete("/api/reviews/{id}", "missing"))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).deleteReview("missing");
    }
}
