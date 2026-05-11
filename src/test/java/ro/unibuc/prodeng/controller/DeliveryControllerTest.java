package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.prodeng.model.PartEntity;
import ro.unibuc.prodeng.request.ReceiveDeliveryRequest;
import ro.unibuc.prodeng.service.InventoryService;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private DeliveryController deliveryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deliveryController).build();
    }

    @Test
    void receiveDelivery_returnsUpdatedPart() throws Exception {
        String requestBody = """
                {
                  "partId": "part-1",
                  "supplierId": "sup-1",
                  "quantity": 5,
                  "deliveredAt": "2026-03-23T10:00:00"
                }
                """;
        PartEntity response = new PartEntity("part-1", "Filter", 15, BigDecimal.valueOf(25), "sup-1");

        when(inventoryService.receiveDelivery(any(ReceiveDeliveryRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/deliveries/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("part-1")))
                .andExpect(jsonPath("$.availableStock", is(15)));
    }
}
