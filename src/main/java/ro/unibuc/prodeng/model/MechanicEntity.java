package ro.unibuc.prodeng.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mechanics")
public record MechanicEntity(
        @Id String id,
        String firstName,
        String lastName,
        String phone,
        double score,
        int reviewCount
) {
    public MechanicEntity withNewReview(int rating) {
        int newCount = this.reviewCount + 1;
        double newScore = ((this.score * this.reviewCount) + rating) / newCount;
        return new MechanicEntity(id, firstName, lastName, phone, newScore, newCount);
    }

    public MechanicEntity withRemovedReview(int rating) {
        if (this.reviewCount <= 1) {
            return new MechanicEntity(id, firstName, lastName, phone, 0.0, 0);
        }
        int newCount = this.reviewCount - 1;
        double newScore = ((this.score * this.reviewCount) - rating) / newCount;
        return new MechanicEntity(id, firstName, lastName, phone, newScore, newCount);
    }
}
