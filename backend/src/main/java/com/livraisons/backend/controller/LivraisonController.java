package com.livraisons.backend.controller;
import com.livraisons.backend.model.LivraisonCom;
import com.livraisons.backend.repository.LivraisonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/livraisons")
public class LivraisonController {
    @Autowired
    private LivraisonRepository repo;

    @GetMapping("/today/all")
    public List<LivraisonCom> getAllToday() {
        return repo.findByDate(LocalDate.now().toString())
                   .stream().limit(10).collect(Collectors.toList());
    }

    @GetMapping("/today/{livreurId}")
    public List<LivraisonCom> getTodayForLivreur(@PathVariable int livreurId) {
        return repo.findByDateAndLivreur(LocalDate.now().toString(), livreurId)
                   .stream().limit(10).collect(Collectors.toList());
    }

    @PostMapping("/update/{nocde}")
    public LivraisonCom updateEtat(@PathVariable int nocde,
                                    @RequestParam String etat,
                                    @RequestParam(required = false) String remarque) {
        LivraisonCom liv = repo.findById(nocde).orElseThrow();
        liv.setEtatliv(etat);
        if (remarque != null) liv.setRemarque(remarque);
        return repo.save(liv);
    }
}