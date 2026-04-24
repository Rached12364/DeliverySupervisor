package com.livraisons.backend.repository;
import com.livraisons.backend.model.LivraisonCom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LivraisonRepository extends JpaRepository<LivraisonCom, Integer> {
    @Query("SELECT l FROM LivraisonCom l WHERE l.dateliv = :date")
    List<LivraisonCom> findByDate(@Param("date") String date);
    @Query("SELECT l FROM LivraisonCom l WHERE l.dateliv = :date AND l.livreur = :livreurId")
    List<LivraisonCom> findByDateAndLivreur(@Param("date") String date, @Param("livreurId") int livreurId);
}
