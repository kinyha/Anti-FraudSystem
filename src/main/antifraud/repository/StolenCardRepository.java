package antifraud.repository;

import antifraud.model.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

    @Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {
        StolenCard findByNumber(String number);
        boolean existsByNumber(String number);
        void deleteByNumber(String number);
}
