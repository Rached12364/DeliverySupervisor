package com.livraisons.backend.controller;

import com.livraisons.backend.model.LigCde;
import com.livraisons.backend.repository.LigCdeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ligcdes")
public class LigCdeController {

    @Autowired
    private LigCdeRepository ligCdeRepository;

    @GetMapping
    public List<LigCde> getAllLigCdes() {
        return ligCdeRepository.findAll();
    }

    @GetMapping("/commande/{nocde}")
    public List<LigCde> getLigCdesByCommande(@PathVariable int nocde) {
        return ligCdeRepository.findByNocde(nocde);
    }
}