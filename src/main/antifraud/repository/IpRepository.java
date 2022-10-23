package antifraud.repository;

import antifraud.model.Ip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpRepository extends JpaRepository<Ip, Integer> {
    Ip findByIp(String ip);
    boolean existsByIp(String ip);
    void deleteByIp(String ip);
}
