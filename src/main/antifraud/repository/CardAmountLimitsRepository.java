package antifraud.repository;

import antifraud.model.CardAmountLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardAmountLimitsRepository extends JpaRepository<CardAmountLimits, String> {
}
