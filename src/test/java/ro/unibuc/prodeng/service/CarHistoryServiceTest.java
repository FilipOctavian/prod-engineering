package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.metrics.AppMetrics;
import ro.unibuc.prodeng.model.CarEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.model.ServiceOrderEntity;
import ro.unibuc.prodeng.repository.CarRepository;
import ro.unibuc.prodeng.repository.ServiceOrderRepository;
import ro.unibuc.prodeng.response.CarHistoryResponse;
import ro.unibuc.prodeng.response.CarHistorySummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarHistoryServiceTest {

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private AppMetrics appMetrics;

    @InjectMocks
    private CarHistoryService carHistoryService;

    private final CarEntity car = new CarEntity("car-1", "Dacia", "Logan", 2020, "B-123-ABC", "client-1");

    private final ServiceOrderEntity completedOrder = new ServiceOrderEntity(
            "order-1", "car-1", "mech-1", "Oil Change", "Replace oil",
            BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.valueOf(150),
            List.of(), LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0),
            OrderStatus.COMPLETED
    );

    private final ServiceOrderEntity activeOrder = new ServiceOrderEntity(
            "order-2", "car-1", "mech-1", "Brake Check", "Inspect brakes",
            BigDecimal.valueOf(80), BigDecimal.ZERO, BigDecimal.valueOf(80),
            List.of(), LocalDateTime.of(2026, 2, 1, 10, 0), null,
            OrderStatus.IN_PROGRESS
    );

    @Test
    void testGetAllOrders_existingCar_returnsAllOrders() {
        when(carRepository.findById("car-1")).thenReturn(Optional.of(car));
        when(serviceOrderRepository.findByCarId("car-1")).thenReturn(List.of(completedOrder, activeOrder));

        List<CarHistoryResponse> result = carHistoryService.getAllOrders("car-1");

        assertEquals(2, result.size());
        assertEquals("order-1", result.get(0).orderId());
        assertEquals("order-2", result.get(1).orderId());
        verify(appMetrics, times(1)).incrementCarHistoryViews();
    }

    @Test
    void testGetAllOrders_carNotFound_throwsEntityNotFoundException() {
        when(carRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carHistoryService.getAllOrders("unknown"));
        verify(appMetrics, never()).incrementCarHistoryViews();
    }

    @Test
    void testGetSummary_carWithOrders_returnsCorrectSummary() {
        when(carRepository.findById("car-1")).thenReturn(Optional.of(car));
        when(serviceOrderRepository.findByCarId("car-1")).thenReturn(List.of(completedOrder, activeOrder));

        CarHistorySummaryResponse result = carHistoryService.getSummary("car-1");

        assertEquals("car-1", result.carId());
        assertEquals(2, result.totalOrders());
        assertEquals(1, result.completedOrders());
        assertEquals(BigDecimal.valueOf(150), result.totalSpent());
    }

    @Test
    void testGetSummary_carNotFound_throwsEntityNotFoundException() {
        when(carRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carHistoryService.getSummary("unknown"));
    }

    @Test
    void testGetSummary_carWithNoOrders_returnsZeroTotals() {
        when(carRepository.findById("car-1")).thenReturn(Optional.of(car));
        when(serviceOrderRepository.findByCarId("car-1")).thenReturn(Collections.emptyList());

        CarHistorySummaryResponse result = carHistoryService.getSummary("car-1");

        assertEquals(0, result.totalOrders());
        assertEquals(0, result.completedOrders());
        assertEquals(BigDecimal.ZERO, result.totalSpent());
    }

    @Test
    void testGetCompletedOrders_existingCar_returnsOnlyCompleted() {
        when(carRepository.findById("car-1")).thenReturn(Optional.of(car));
        when(serviceOrderRepository.findByCarIdAndStatus("car-1", OrderStatus.COMPLETED))
                .thenReturn(List.of(completedOrder));

        List<CarHistoryResponse> result = carHistoryService.getCompletedOrders("car-1");

        assertEquals(1, result.size());
        assertEquals("order-1", result.get(0).orderId());
        assertEquals(OrderStatus.COMPLETED, result.get(0).status());
    }

    @Test
    void testGetCompletedOrders_carNotFound_throwsEntityNotFoundException() {
        when(carRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carHistoryService.getCompletedOrders("unknown"));
    }

    @Test
    void testGetActiveOrder_carWithActiveOrder_returnsActiveOrder() {
        when(carRepository.findById("car-1")).thenReturn(Optional.of(car));
        when(serviceOrderRepository.findByCarIdAndStatus("car-1", OrderStatus.IN_PROGRESS))
                .thenReturn(List.of(activeOrder));

        CarHistoryResponse result = carHistoryService.getActiveOrder("car-1");

        assertEquals("order-2", result.orderId());
        assertEquals(OrderStatus.IN_PROGRESS, result.status());
    }

    @Test
    void testGetActiveOrder_noActiveOrder_throwsEntityNotFoundException() {
        when(carRepository.findById("car-1")).thenReturn(Optional.of(car));
        when(serviceOrderRepository.findByCarIdAndStatus("car-1", OrderStatus.IN_PROGRESS))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> carHistoryService.getActiveOrder("car-1"));
    }

    @Test
    void testGetActiveOrder_carNotFound_throwsEntityNotFoundException() {
        when(carRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carHistoryService.getActiveOrder("unknown"));
    }
}
