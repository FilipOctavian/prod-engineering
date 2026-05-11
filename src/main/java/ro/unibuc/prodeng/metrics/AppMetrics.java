package ro.unibuc.prodeng.metrics;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import ro.unibuc.prodeng.repository.PartRepository;

@Component
public class AppMetrics {

    private static final int LOW_STOCK_THRESHOLD = 5;

    private final MeterRegistry registry;
    private final PartRepository partRepository;
    private final Counter serviceOrdersCreated;
    private final Counter usersCreated;
    private final Counter reviewsCreated;
    private final Counter carHistoryViews;
    private final Timer serviceOrderCreateTimer;
    private final Timer userCreateTimer;
    private final DistributionSummary reviewRatingSummary;

    public AppMetrics(MeterRegistry registry, PartRepository partRepository) {
        this.registry = registry;
        this.partRepository = partRepository;
        this.serviceOrdersCreated = Counter.builder("prod_eng_service_orders_created")
                .description("Service orders created")
                .register(registry);
        this.usersCreated = Counter.builder("prod_eng_user_created")
                .description("Users created")
                .register(registry);
        this.reviewsCreated = Counter.builder("prod_eng_reviews_created")
                .description("Reviews created")
                .register(registry);
        this.carHistoryViews = Counter.builder("prod_eng_car_history_views")
                .description("Car service history views")
                .register(registry);
        this.serviceOrderCreateTimer = Timer.builder("prod_eng_service_order_create_seconds")
                .description("Service order creation time")
                .register(registry);
        this.userCreateTimer = Timer.builder("prod_eng_user_create_seconds")
                .description("User creation time")
                .register(registry);
        this.reviewRatingSummary = DistributionSummary.builder("prod_eng_review_rating")
                .description("Review ratings")
                .register(registry);

        Gauge.builder("prod_eng_parts_low_stock", this, AppMetrics::countLowStockParts)
                .description("Parts with low stock")
                .register(registry);
    }

    public Timer.Sample startServiceOrderCreateTimer() {
        return Timer.start(registry);
    }

    public void stopServiceOrderCreateTimer(Timer.Sample sample) {
        sample.stop(serviceOrderCreateTimer);
    }

    public Timer.Sample startUserCreateTimer() {
        return Timer.start(registry);
    }

    public void stopUserCreateTimer(Timer.Sample sample) {
        sample.stop(userCreateTimer);
    }

    public void incrementServiceOrdersCreated() {
        serviceOrdersCreated.increment();
    }

    public void incrementUsersCreated() {
        usersCreated.increment();
    }

    public void incrementReviewsCreated() {
        reviewsCreated.increment();
    }

    public void incrementCarHistoryViews() {
        carHistoryViews.increment();
    }

    public void recordReviewRating(int rating) {
        reviewRatingSummary.record(rating);
    }

    public void incrementError(String type) {
        registry.counter("prod_eng_errors", "type", type).increment();
    }

    private double countLowStockParts() {
        return partRepository.findAll().stream()
                .filter(part -> part.availableStock() < LOW_STOCK_THRESHOLD)
                .count();
    }
}
