package com.livraisons.backend.controller;
import com.livraisons.backend.model.Personnel;
import com.livraisons.backend.repository.PersonnelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private PersonnelRepository repo;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Optional<Personnel> p = repo.findByLoginAndMotP(username, password);
        if (p.isPresent()) return ResponseEntity.ok(p.get());
        return ResponseEntity.status(401).body("Identifiants incorrects");
    }
}
