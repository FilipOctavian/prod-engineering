package ro.unibuc.prodeng.response;

import ro.unibuc.prodeng.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CarHistoryResponse(
        String orderId,
        String carId,
        String mechanicId,
        String serviceName,
        String description,
        BigDecimal laborCost,
        BigDecimal partsCost,
        BigDecimal totalCost,
        LocalDateTime scheduledAt,
        LocalDateTime completedAt,
        OrderStatus status
) {
}
