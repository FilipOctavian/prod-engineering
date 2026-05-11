package ro.unibuc.prodeng.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.request.CreateReviewRequest;
import ro.unibuc.prodeng.response.ReviewResponse;
import ro.unibuc.prodeng.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.createReview(request));
    }

    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForMechanic(@PathVariable String mechanicId) {
        return ResponseEntity.ok(reviewService.getReviewsForMechanic(mechanicId));
    }

    @GetMapping("/mechanic/{mechanicId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable String mechanicId) {
        return ResponseEntity.ok(reviewService.getAverageRating(mechanicId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
