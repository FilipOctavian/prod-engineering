package ro.unibuc.prodeng.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ro.unibuc.prodeng.model.ReviewEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<ReviewEntity, String> {

    List<ReviewEntity> findByMechanicId(String mechanicId);

    Optional<ReviewEntity> findByServiceOrderId(String serviceOrderId);

}
