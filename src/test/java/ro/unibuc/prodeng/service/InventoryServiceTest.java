package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.model.PartEntity;
import ro.unibuc.prodeng.repository.PartRepository;
import ro.unibuc.prodeng.request.ReceiveDeliveryRequest;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private AutoServiceCatalogService catalogService;

    @Mock
    private PartRepository partRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void receiveDelivery_whenSupplierMatches_increasesStockAndSaves() {
        ReceiveDeliveryRequest request = new ReceiveDeliveryRequest("part-1", "sup-1", 4, LocalDateTime.of(2026, 3, 23, 10, 0));
        PartEntity existingPart = new PartEntity("part-1", "Filter", 6, BigDecimal.valueOf(25), "sup-1");
        PartEntity updatedPart = new PartEntity("part-1", "Filter", 10, BigDecimal.valueOf(25), "sup-1");

        when(catalogService.ensurePartExists("part-1")).thenReturn(existingPart);
        when(partRepository.save(any(PartEntity.class))).thenReturn(updatedPart);

        PartEntity result = inventoryService.receiveDelivery(request);

        assertSame(updatedPart, result);
        assertEquals(10, result.availableStock());
        verify(partRepository).save(any(PartEntity.class));
    }

    @Test
    void receiveDelivery_whenSupplierDoesNotMatch_throwsException() {
        ReceiveDeliveryRequest request = new ReceiveDeliveryRequest("part-1", "sup-2", 4, LocalDateTime.of(2026, 3, 23, 10, 0));
        PartEntity existingPart = new PartEntity("part-1", "Filter", 6, BigDecimal.valueOf(25), "sup-1");

        when(catalogService.ensurePartExists("part-1")).thenReturn(existingPart);

        assertThrows(IllegalArgumentException.class, () -> inventoryService.receiveDelivery(request));
    }
}
