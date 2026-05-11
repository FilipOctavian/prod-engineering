package ro.unibuc.prodeng.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.response.CarHistoryResponse;
import ro.unibuc.prodeng.response.CarHistorySummaryResponse;
import ro.unibuc.prodeng.service.CarHistoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class CarHistoryControllerTest {

    @Mock
    private CarHistoryService carHistoryService;

    @InjectMocks
    private CarHistoryController carHistoryController;

    private MockMvc mockMvc;

    private final CarHistoryResponse completedEntry = new CarHistoryResponse(
            "order-1", "car-1", "mech-1", "Oil Change", "Replace oil",
            BigDecimal.valueOf(100), BigDecimal.valueOf(50), BigDecimal.valueOf(150),
            LocalDateTime.of(2026, 1, 1, 10, 0), LocalDateTime.of(2026, 1, 2, 10, 0),
            OrderStatus.COMPLETED
    );

    private final CarHistoryResponse activeEntry = new CarHistoryResponse(
            "order-2", "car-1", "mech-1", "Brake Check", "Inspect brakes",
            BigDecimal.valueOf(80), BigDecimal.ZERO, BigDecimal.valueOf(80),
            LocalDateTime.of(2026, 2, 1, 10, 0), null,
            OrderStatus.IN_PROGRESS
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(carHistoryController).build();
    }

    @Test
    void testGetAllOrders_existingCar_returnsAllOrders() throws Exception {
        when(carHistoryService.getAllOrders("car-1")).thenReturn(List.of(completedEntry, activeEntry));

        mockMvc.perform(get("/api/car-history/{carId}", "car-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderId", is("order-1")))
                .andExpect(jsonPath("$[1].orderId", is("order-2")));

        verify(carHistoryService, times(1)).getAllOrders("car-1");
    }

    @Test
    void testGetAllOrders_carNotFound_returnsNotFound() throws Exception {
        when(carHistoryService.getAllOrders("unknown"))
                .thenThrow(new EntityNotFoundException("Car unknown"));

        mockMvc.perform(get("/api/car-history/{carId}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetSummary_existingCar_returnsSummary() throws Exception {
        CarHistorySummaryResponse summary = new CarHistorySummaryResponse("car-1", 2, 1, BigDecimal.valueOf(150));
        when(carHistoryService.getSummary("car-1")).thenReturn(summary);

        mockMvc.perform(get("/api/car-history/{carId}/summary", "car-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId", is("car-1")))
                .andExpect(jsonPath("$.totalOrders", is(2)))
                .andExpect(jsonPath("$.completedOrders", is(1)))
                .andExpect(jsonPath("$.totalSpent", is(150)));

        verify(carHistoryService, times(1)).getSummary("car-1");
    }

    @Test
    void testGetSummary_carNotFound_returnsNotFound() throws Exception {
        when(carHistoryService.getSummary("unknown"))
                .thenThrow(new EntityNotFoundException("Car unknown"));

        mockMvc.perform(get("/api/car-history/{carId}/summary", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCompletedOrders_existingCar_returnsCompletedOnly() throws Exception {
        when(carHistoryService.getCompletedOrders("car-1")).thenReturn(List.of(completedEntry));

        mockMvc.perform(get("/api/car-history/{carId}/completed", "car-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("COMPLETED")));

        verify(carHistoryService, times(1)).getCompletedOrders("car-1");
    }

    @Test
    void testGetActiveOrder_carWithActiveOrder_returnsActiveOrder() throws Exception {
        when(carHistoryService.getActiveOrder("car-1")).thenReturn(activeEntry);

        mockMvc.perform(get("/api/car-history/{carId}/active", "car-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId", is("order-2")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        verify(carHistoryService, times(1)).getActiveOrder("car-1");
    }

    @Test
    void testGetActiveOrder_noActiveOrder_returnsNotFound() throws Exception {
        when(carHistoryService.getActiveOrder("car-1"))
                .thenThrow(new EntityNotFoundException("Active order for car car-1"));

        mockMvc.perform(get("/api/car-history/{carId}/active", "car-1"))
                .andExpect(status().isNotFound());
    }
}
