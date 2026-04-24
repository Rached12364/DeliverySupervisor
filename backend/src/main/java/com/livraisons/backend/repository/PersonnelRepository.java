package com.livraisons.backend.repository;
import com.livraisons.backend.model.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonnelRepository extends JpaRepository<Personnel, Integer> {
    Optional<Personnel> findByLoginAndMotP(String login, String motP);
}
