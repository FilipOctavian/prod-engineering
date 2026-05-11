package ro.unibuc.prodeng.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        String id,
        String mechanicId,
        String serviceOrderId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
