package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.model.CarEntity;
import ro.unibuc.prodeng.model.MechanicEntity;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.model.PartEntity;
import ro.unibuc.prodeng.model.ServiceOrderEntity;
import ro.unibuc.prodeng.repository.PartRepository;
import ro.unibuc.prodeng.repository.ServiceOrderRepository;
import ro.unibuc.prodeng.request.CreateServiceOrderRequest;
import ro.unibuc.prodeng.request.RequiredPartRequest;
import ro.unibuc.prodeng.response.ServiceOrderResponse;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    @Mock
    private AutoServiceCatalogService catalogService;

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private PartRepository partRepository;

    private ServiceOrderService serviceOrderService;

    @BeforeEach
    void setup() {
        serviceOrderService = new ServiceOrderService(catalogService, serviceOrderRepository, partRepository);
    }

    @Test
    void createServiceOrder_shouldDeductStockAndComputeTotalCost() {
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "car-1",
                "mech-1",
                "Engine Diagnostics",
                "Engine warning light is on",
                BigDecimal.valueOf(120),
                LocalDateTime.of(2026, 3, 5, 10, 0),
                List.of(new RequiredPartRequest("part-1", 2))
        );

        when(catalogService.ensureCarExists("car-1")).thenReturn(new CarEntity("car-1", "Ford", "Focus", 2018, "B-99-XYZ", "client-1"));
        when(catalogService.ensureMechanicExists("mech-1")).thenReturn(new MechanicEntity("mech-1", "Ion", "Popescu", "0711", 0.0, 0));
        when(catalogService.ensurePartExists("part-1")).thenReturn(
                new PartEntity("part-1", "Spark Plug", 10, BigDecimal.valueOf(35), "sup-1"));

        when(serviceOrderRepository.save(any(ServiceOrderEntity.class))).thenAnswer(invocation -> {
            ServiceOrderEntity toSave = invocation.getArgument(0);
            return new ServiceOrderEntity(
                    "order-1",
                    toSave.carId(),
                    toSave.mechanicId(),
                    toSave.serviceName(),
                    toSave.description(),
                    toSave.laborCost(),
                    toSave.partsCost(),
                    toSave.totalCost(),
                    toSave.requiredParts(),
                    toSave.scheduledAt(),
                    toSave.completedAt(),
                    toSave.status()
            );
        });

        ServiceOrderResponse response = serviceOrderService.createServiceOrder(request);

        assertEquals(BigDecimal.valueOf(70), response.partsCost());
        assertEquals(BigDecimal.valueOf(190), response.totalCost());
        assertEquals(OrderStatus.IN_PROGRESS, response.status());
        verify(partRepository, times(1)).saveAll(any());
    }

    @Test
    void createServiceOrder_shouldFailWhenNotEnoughStock() {
        CreateServiceOrderRequest request = new CreateServiceOrderRequest(
                "car-1",
                "mech-1",
                "Oil change",
                "Replace oil and filter",
                BigDecimal.valueOf(80),
                LocalDateTime.of(2026, 3, 5, 10, 0),
                List.of(new RequiredPartRequest("part-1", 5))
        );

        when(catalogService.ensureCarExists("car-1")).thenReturn(new CarEntity("car-1", "Ford", "Focus", 2018, "B-99-XYZ", "client-1"));
        when(catalogService.ensureMechanicExists("mech-1")).thenReturn(new MechanicEntity("mech-1", "Ion", "Popescu", "0711", 0.0, 0));
        when(catalogService.ensurePartExists("part-1")).thenReturn(
                new PartEntity("part-1", "Oil Filter", 2, BigDecimal.valueOf(20), "sup-1"));

        assertThrows(IllegalArgumentException.class, () -> serviceOrderService.createServiceOrder(request));
    }

    @Test
    void completeOrder_shouldSetStatusToCompleted() {
        ServiceOrderEntity inProgressOrder = new ServiceOrderEntity(
                "order-1",
                "car-1",
                "mech-1",
                "Brake inspection",
                "Noise while braking",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(150),
                List.of(),
                LocalDateTime.of(2026, 3, 5, 10, 0),
                null,
                OrderStatus.IN_PROGRESS
        );

        when(serviceOrderRepository.findById("order-1")).thenReturn(Optional.of(inProgressOrder));
        when(serviceOrderRepository.save(any(ServiceOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOrderResponse response = serviceOrderService.completeOrder("order-1");

        assertEquals(OrderStatus.COMPLETED, response.status());
    }

    @Test
    void completeOrder_shouldFailWhenAlreadyCompleted() {
        ServiceOrderEntity completedOrder = new ServiceOrderEntity(
                "order-1",
                "car-1",
                "mech-1",
                "Brake inspection",
                "Noise while braking",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(150),
                List.of(),
                LocalDateTime.of(2026, 3, 5, 10, 0),
                LocalDateTime.of(2026, 3, 5, 11, 0),
                OrderStatus.COMPLETED
        );

        when(serviceOrderRepository.findById("order-1")).thenReturn(Optional.of(completedOrder));

        assertThrows(IllegalArgumentException.class, () -> serviceOrderService.completeOrder("order-1"));
    }

    @Test
    void getOrderById_shouldReturnMappedResponse() {
        ServiceOrderEntity order = new ServiceOrderEntity(
                "order-1",
                "car-1",
                "mech-1",
                "Brake inspection",
                "Noise while braking",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(150),
                List.of(),
                LocalDateTime.of(2026, 3, 5, 10, 0),
                null,
                OrderStatus.IN_PROGRESS
        );

        when(serviceOrderRepository.findById("order-1")).thenReturn(Optional.of(order));

        ServiceOrderResponse response = serviceOrderService.getOrderById("order-1");

        assertEquals("order-1", response.id());
        assertEquals(OrderStatus.IN_PROGRESS, response.status());
    }

    @Test
    void getOrdersByStatus_shouldReturnMappedResponses() {
        when(serviceOrderRepository.findByStatus(OrderStatus.IN_PROGRESS)).thenReturn(List.of(
                new ServiceOrderEntity(
                        "order-1",
                        "car-1",
                        "mech-1",
                        "Brake inspection",
                        "Noise while braking",
                        BigDecimal.valueOf(100),
                        BigDecimal.valueOf(50),
                        BigDecimal.valueOf(150),
                        List.of(),
                        LocalDateTime.of(2026, 3, 5, 10, 0),
                        null,
                        OrderStatus.IN_PROGRESS
                )));

        List<ServiceOrderResponse> responses = serviceOrderService.getOrdersByStatus(OrderStatus.IN_PROGRESS);

        assertEquals(1, responses.size());
        assertNotNull(responses.getFirst().id());
        assertEquals(OrderStatus.IN_PROGRESS, responses.getFirst().status());
    }
}
