package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.metrics.AppMetrics;
import ro.unibuc.prodeng.model.MechanicEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.model.ReviewEntity;
import ro.unibuc.prodeng.model.ServiceOrderEntity;
import ro.unibuc.prodeng.repository.MechanicRepository;
import ro.unibuc.prodeng.repository.ReviewRepository;
import ro.unibuc.prodeng.repository.ServiceOrderRepository;
import ro.unibuc.prodeng.request.CreateReviewRequest;
import ro.unibuc.prodeng.response.ReviewResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final AppMetrics appMetrics;

    public ReviewService(
            ReviewRepository reviewRepository,
            MechanicRepository mechanicRepository,
            ServiceOrderRepository serviceOrderRepository,
            AppMetrics appMetrics) {
        this.reviewRepository = reviewRepository;
        this.mechanicRepository = mechanicRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.appMetrics = appMetrics;
    }

    public ReviewResponse createReview(CreateReviewRequest request) {
        MechanicEntity mechanic = mechanicRepository.findById(request.mechanicId())
                .orElseThrow(() -> new EntityNotFoundException("Mechanic " + request.mechanicId()));
        ServiceOrderEntity order = serviceOrderRepository.findById(request.serviceOrderId())
                .orElseThrow(() -> new EntityNotFoundException("ServiceOrder " + request.serviceOrderId()));
        if (order.status() != OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Service order is not completed: " + request.serviceOrderId());
        }
        if (!order.mechanicId().equals(request.mechanicId())) {
            throw new IllegalArgumentException("Service order was handled by another mechanic");
        }
        if (reviewRepository.findByServiceOrderId(request.serviceOrderId()).isPresent()) {
            throw new IllegalArgumentException("This service order has already been reviewed");
        }

        var review = new ReviewEntity(null, request.mechanicId(), request.serviceOrderId(),
                request.rating(), request.comment(), LocalDateTime.now());
        ReviewEntity saved = reviewRepository.save(review);
        MechanicEntity updated = mechanic.withNewReview(request.rating());
        mechanicRepository.save(updated);
        appMetrics.incrementReviewsCreated();
        appMetrics.recordReviewRating(request.rating());
        return toResponse(saved);
    }

    public List<ReviewResponse> getReviewsForMechanic(String mechanicId) {
        mechanicRepository.findById(mechanicId)
            .orElseThrow(() -> new EntityNotFoundException("Mechanic " + mechanicId));
        return reviewRepository.findByMechanicId(mechanicId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public double getAverageRating(String mechanicId) {
        MechanicEntity mechanic = mechanicRepository.findById(mechanicId)
            .orElseThrow(() -> new EntityNotFoundException("Mechanic " + mechanicId));
        return mechanic.score();
    }

    public void deleteReview(String reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("Review with id: " + reviewId));
        MechanicEntity mechanic = mechanicRepository.findById(review.mechanicId())
            .orElseThrow(() -> new EntityNotFoundException("Mechanic " + review.mechanicId()));
        MechanicEntity updated = mechanic.withRemovedReview(review.rating());
        mechanicRepository.save(updated);
        reviewRepository.deleteById(reviewId);
    }

    private ReviewResponse toResponse(ReviewEntity review) {
        return new ReviewResponse(
                review.id(),
                review.mechanicId(),
                review.serviceOrderId(),
                review.rating(),
                review.comment(),
                review.createdAt()
        );
    }
}