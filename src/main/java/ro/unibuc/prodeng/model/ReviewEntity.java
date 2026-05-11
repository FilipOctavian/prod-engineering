package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reviews")
public record ReviewEntity(
        @Id String id,
        String mechanicId,
        String serviceOrderId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
