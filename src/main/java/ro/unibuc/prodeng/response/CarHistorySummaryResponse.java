package ro.unibuc.prodeng.response;

import java.math.BigDecimal;

public record CarHistorySummaryResponse(
        String carId,
        int totalOrders,
        int completedOrders,
        BigDecimal totalSpent
) {
}
