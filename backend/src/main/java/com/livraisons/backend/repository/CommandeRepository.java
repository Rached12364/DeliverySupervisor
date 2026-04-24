package com.livraisons.backend.repository;

import com.livraisons.backend.model.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Integer> {

    @Query("SELECT c FROM Commande c WHERE c.datecde = :date")
    List<Commande> findByDate(@Param("date") String date);
}