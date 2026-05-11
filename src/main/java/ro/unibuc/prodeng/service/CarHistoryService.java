package ro.unibuc.prodeng.service;

import org.springframework.stereotype.Service;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.metrics.AppMetrics;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.model.ServiceOrderEntity;
import ro.unibuc.prodeng.repository.CarRepository;
import ro.unibuc.prodeng.repository.ServiceOrderRepository;
import ro.unibuc.prodeng.response.CarHistoryResponse;
import ro.unibuc.prodeng.response.CarHistorySummaryResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarHistoryService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final CarRepository carRepository;
    private final AppMetrics appMetrics;

    public CarHistoryService(
            ServiceOrderRepository serviceOrderRepository,
            CarRepository carRepository,
            AppMetrics appMetrics) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.carRepository = carRepository;
        this.appMetrics = appMetrics;
    }

    public List<CarHistoryResponse> getAllOrders(String carId) {
        carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car " + carId));
        appMetrics.incrementCarHistoryViews();
        return serviceOrderRepository.findByCarId(carId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CarHistorySummaryResponse getSummary(String carId) {
        carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car " + carId));
        List<ServiceOrderEntity> orders = serviceOrderRepository.findByCarId(carId);
        long completedCount = orders.stream().filter(o -> o.status() == OrderStatus.COMPLETED).count();
        BigDecimal totalSpent = orders.stream()
                .filter(o -> o.status() == OrderStatus.COMPLETED)
                .map(ServiceOrderEntity::totalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarHistorySummaryResponse(carId, orders.size(), (int) completedCount, totalSpent);
    }

    public List<CarHistoryResponse> getCompletedOrders(String carId) {
        carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car " + carId));
        return serviceOrderRepository.findByCarIdAndStatus(carId, OrderStatus.COMPLETED).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CarHistoryResponse getActiveOrder(String carId) {
        carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car " + carId));
        List<ServiceOrderEntity> activeOrders = serviceOrderRepository.findByCarIdAndStatus(carId, OrderStatus.IN_PROGRESS);
        if (activeOrders.isEmpty()) {
            throw new EntityNotFoundException("Active order for car " + carId);
        }
        return toResponse(activeOrders.get(0));
    }

    private CarHistoryResponse toResponse(ServiceOrderEntity order) {
        return new CarHistoryResponse(
                order.id(),
                order.carId(),
                order.mechanicId(),
                order.serviceName(),
                order.description(),
                order.laborCost(),
                order.partsCost(),
                order.totalCost(),
                order.scheduledAt(),
                order.completedAt(),
                order.status()
        );
    }
}
