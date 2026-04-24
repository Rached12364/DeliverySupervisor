package com.livraisons.backend.repository;

import com.livraisons.backend.model.LigCde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LigCdeRepository extends JpaRepository<LigCde, LigCde.LigCdeId> {

    @Query("SELECT l FROM LigCde l WHERE l.id.nocde = :nocde")
    List<LigCde> findByNocde(@Param("nocde") int nocde);
}