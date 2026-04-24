package com.livraisons.backend.controller;

import com.livraisons.backend.model.Commande;
import com.livraisons.backend.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/commandes")
public class CommandeController {

    @Autowired
    private CommandeRepository commandeRepository;

    @GetMapping
    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    @GetMapping("/today")
    public List<Commande> getTodayCommandes() {
        String today = LocalDate.now().toString();
        return commandeRepository.findByDate(today);
    }

    @GetMapping("/{id}")
    public Commande getCommandeById(@PathVariable int id) {
        return commandeRepository.findById(id).orElse(null);
    }
}