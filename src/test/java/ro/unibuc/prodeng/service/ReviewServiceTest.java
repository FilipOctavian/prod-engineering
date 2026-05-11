package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.MechanicEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.model.ReviewEntity;
import ro.unibuc.prodeng.model.ServiceOrderEntity;
import ro.unibuc.prodeng.repository.MechanicRepository;
import ro.unibuc.prodeng.repository.ReviewRepository;
import ro.unibuc.prodeng.repository.ServiceOrderRepository;
import ro.unibuc.prodeng.request.CreateReviewRequest;
import ro.unibuc.prodeng.response.ReviewResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MechanicRepository mechanicRepository;

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private final MechanicEntity mechanic = new MechanicEntity("mech-1", "Ion", "Popescu", "0711111111", 0.0, 0);
    private final ServiceOrderEntity completedOrder = new ServiceOrderEntity(
            "order-1", "car-1", "mech-1", "Oil Change", "Replace oil",
            BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.valueOf(150),
            List.of(), LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0),
            OrderStatus.COMPLETED
    );

    @Test
    void testCreateReview_validRequest_createsAndReturnsReview() {
        CreateReviewRequest request = new CreateReviewRequest("mech-1", "order-1", 5, "Excellent work");
        ReviewEntity saved = new ReviewEntity("review-1", "mech-1", "order-1", 5, "Excellent work", LocalDateTime.now());

        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(serviceOrderRepository.findById("order-1")).thenReturn(Optional.of(completedOrder));
        when(reviewRepository.findByServiceOrderId("order-1")).thenReturn(Optional.empty());
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(saved);
        when(mechanicRepository.save(any(MechanicEntity.class))).thenReturn(mechanic.withNewReview(5));

        ReviewResponse result = reviewService.createReview(request);

        assertNotNull(result);
        assertEquals("review-1", result.id());
        assertEquals("mech-1", result.mechanicId());
        assertEquals(5, result.rating());
        assertEquals("Excellent work", result.comment());
        verify(reviewRepository, times(1)).save(any(ReviewEntity.class));
        verify(mechanicRepository, times(1)).save(any(MechanicEntity.class));
    }

    @Test
    void testCreateReview_mechanicNotFound_throwsEntityNotFoundException() {
        CreateReviewRequest request = new CreateReviewRequest("unknown-mech", "order-1", 4, "Good");

        when(mechanicRepository.findById("unknown-mech")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.createReview(request));
    }

    @Test
    void testCreateReview_orderNotFound_throwsEntityNotFoundException() {
        CreateReviewRequest request = new CreateReviewRequest("mech-1", "unknown-order", 4, "Good");

        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(serviceOrderRepository.findById("unknown-order")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.createReview(request));
    }

    @Test
    void testCreateReview_orderNotCompleted_throwsIllegalArgumentException() {
        ServiceOrderEntity inProgressOrder = new ServiceOrderEntity(
                "order-2", "car-1", "mech-1", "Brake Check", "Check brakes",
                BigDecimal.valueOf(80), BigDecimal.ZERO, BigDecimal.valueOf(80),
                List.of(), LocalDateTime.of(2026, 1, 1, 10, 0), null,
                OrderStatus.IN_PROGRESS
        );
        CreateReviewRequest request = new CreateReviewRequest("mech-1", "order-2", 3, "OK");

        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(serviceOrderRepository.findById("order-2")).thenReturn(Optional.of(inProgressOrder));

        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(request));
    }

    @Test
    void testCreateReview_mechanicMismatch_throwsIllegalArgumentException() {
        ServiceOrderEntity orderByOtherMechanic = new ServiceOrderEntity(
                "order-1", "car-1", "other-mech", "Oil Change", "Replace oil",
                BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.valueOf(150),
                List.of(), LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0),
                OrderStatus.COMPLETED
        );
        CreateReviewRequest request = new CreateReviewRequest("mech-1", "order-1", 5, "Great");

        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(serviceOrderRepository.findById("order-1")).thenReturn(Optional.of(orderByOtherMechanic));

        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(request));
    }

    @Test
    void testCreateReview_duplicateReview_throwsIllegalArgumentException() {
        ReviewEntity existing = new ReviewEntity("review-1", "mech-1", "order-1", 5, "Great", LocalDateTime.now());
        CreateReviewRequest request = new CreateReviewRequest("mech-1", "order-1", 4, "Also great");

        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(serviceOrderRepository.findById("order-1")).thenReturn(Optional.of(completedOrder));
        when(reviewRepository.findByServiceOrderId("order-1")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> reviewService.createReview(request));
    }

    @Test
    void testGetReviewsForMechanic_mechanicWithReviews_returnsAllReviews() {
        List<ReviewEntity> reviews = Arrays.asList(
                new ReviewEntity("r-1", "mech-1", "order-1", 5, "Great", LocalDateTime.now()),
                new ReviewEntity("r-2", "mech-1", "order-2", 4, "Good", LocalDateTime.now())
        );

        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(mechanic));
        when(reviewRepository.findByMechanicId("mech-1")).thenReturn(reviews);

        List<ReviewResponse> result = reviewService.getReviewsForMechanic("mech-1");

        assertEquals(2, result.size());
        assertEquals("r-1", result.get(0).id());
        assertEquals("r-2", result.get(1).id());
    }

    @Test
    void testGetReviewsForMechanic_mechanicNotFound_throwsEntityNotFoundException() {
        when(mechanicRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.getReviewsForMechanic("unknown"));
    }

    @Test
    void testGetAverageRating_mechanicWithScore_returnsScore() {
        MechanicEntity ratedMechanic = new MechanicEntity("mech-1", "Ion", "Popescu", "0711111111", 4.5, 2);
        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(ratedMechanic));

        double result = reviewService.getAverageRating("mech-1");

        assertEquals(4.5, result);
    }

    @Test
    void testGetAverageRating_mechanicNotFound_throwsEntityNotFoundException() {
        when(mechanicRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.getAverageRating("unknown"));
    }

    @Test
    void testDeleteReview_existingReview_deletesAndUpdatesMechanicScore() {
        ReviewEntity review = new ReviewEntity("review-1", "mech-1", "order-1", 5, "Great", LocalDateTime.now());
        MechanicEntity ratedMechanic = new MechanicEntity("mech-1", "Ion", "Popescu", "0711111111", 5.0, 1);

        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review));
        when(mechanicRepository.findById("mech-1")).thenReturn(Optional.of(ratedMechanic));

        reviewService.deleteReview("review-1");

        verify(reviewRepository, times(1)).deleteById("review-1");
        verify(mechanicRepository, times(1)).save(any(MechanicEntity.class));
    }

    @Test
    void testDeleteReview_reviewNotFound_throwsEntityNotFoundException() {
        when(reviewRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reviewService.deleteReview("missing"));
    }
}
