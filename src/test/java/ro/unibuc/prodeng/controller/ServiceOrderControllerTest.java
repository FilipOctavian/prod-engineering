package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.prodeng.model.OrderStatus;
import ro.unibuc.prodeng.request.CreateServiceOrderRequest;
import ro.unibuc.prodeng.request.RequiredPartRequest;
import ro.unibuc.prodeng.response.RequiredPartResponse;
import ro.unibuc.prodeng.response.ServiceOrderResponse;
import ro.unibuc.prodeng.service.ServiceOrderService;

@ExtendWith(MockitoExtension.class)
class ServiceOrderControllerTest {

    @Mock
    private ServiceOrderService serviceOrderService;

    @InjectMocks
    private ServiceOrderController serviceOrderController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(serviceOrderController).build();
    }

    @Test
    void createServiceOrder_returnsCreatedOrder() throws Exception {
        String requestBody = """
                {
                  "carId": "car-1",
                  "mechanicId": "mech-1",
                  "serviceName": "Revizie",
                  "description": "Schimb ulei",
                  "laborCost": 200,
                  "scheduledAt": "2026-03-23T10:00:00",
                  "requiredParts": [
                    {
                      "partId": "part-1",
                      "quantity": 2
                    }
                  ]
                }
                """;

        ServiceOrderResponse response = new ServiceOrderResponse(
                "order-1",
                "car-1",
                "mech-1",
                "Revizie",
                "Schimb ulei",
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(290),
                List.of(new RequiredPartResponse("part-1", 2, BigDecimal.valueOf(45))),
                LocalDateTime.of(2026, 3, 23, 10, 0),
                null,
                OrderStatus.IN_PROGRESS
        );

        when(serviceOrderService.createServiceOrder(any(CreateServiceOrderRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/service-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("order-1")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void completeOrder_returnsCompletedOrder() throws Exception {
        ServiceOrderResponse response = new ServiceOrderResponse(
                "order-1",
                "car-1",
                "mech-1",
                "Revizie",
                "Schimb ulei",
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(290),
                List.of(),
                LocalDateTime.of(2026, 3, 23, 10, 0),
                LocalDateTime.of(2026, 3, 23, 12, 0),
                OrderStatus.COMPLETED
        );

        when(serviceOrderService.completeOrder("order-1")).thenReturn(response);

        mockMvc.perform(patch("/api/service-orders/order-1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void getOrderById_returnsOrder() throws Exception {
        ServiceOrderResponse response = new ServiceOrderResponse(
                "order-1",
                "car-1",
                "mech-1",
                "Revizie",
                "Schimb ulei",
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(290),
                List.of(),
                LocalDateTime.of(2026, 3, 23, 10, 0),
                null,
                OrderStatus.IN_PROGRESS
        );

        when(serviceOrderService.getOrderById("order-1")).thenReturn(response);

        mockMvc.perform(get("/api/service-orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("order-1")));
    }

    @Test
    void getOrdersByStatus_returnsOrders() throws Exception {
        when(serviceOrderService.getOrdersByStatus(OrderStatus.IN_PROGRESS)).thenReturn(List.of(
                new ServiceOrderResponse(
                        "order-1",
                        "car-1",
                        "mech-1",
                        "Revizie",
                        "Schimb ulei",
                        BigDecimal.valueOf(200),
                        BigDecimal.valueOf(90),
                        BigDecimal.valueOf(290),
                        List.of(),
                        LocalDateTime.of(2026, 3, 23, 10, 0),
                        null,
                        OrderStatus.IN_PROGRESS
                )));

        mockMvc.perform(get("/api/service-orders").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
