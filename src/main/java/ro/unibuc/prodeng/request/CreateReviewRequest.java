package ro.unibuc.prodeng.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(
        @NotBlank String mechanicId,
        @NotBlank String serviceOrderId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment
) {
}
