package com.textile.fournisseur.controller;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller REST pour tester manuellement le déclenchement de ProcFourn.
 *
 * En production, ProcFourn est déclenché automatiquement par le message
 * CommandeMatieres envoyé par TCmdMat (ProcUsine) via MF1.
 *
 * Pour tester manuellement :
 * POST http://localhost:8081/api/fournisseur/start
 * Body JSON : { "numeroCommande": "CMD-001", "typeTissu": "coton", "quantiteManquante": 50.0 }
 */
@RestController
@RequestMapping("/api/fournisseur")
public class StartFournisseurController {

    @Autowired
    private ZeebeClient zeebeClient;

    /**
     * ✅ CORRECTION : ProcFourn démarre via un MESSAGE Zeebe (pas newCreateInstanceCommand)
     * car SEFourn est un Message Start Event qui attend "CommandeMatieres".
     */
    @PostMapping("/start")
    public ResponseEntity<String> startProcess(@RequestBody Map<String, Object> variables) {
        try {
            // Récupère le numeroCommande depuis le body, ou génère un ID de test
            String numeroCommande = (String) variables.getOrDefault(
                    "numeroCommande",
                    "CMD-TEST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase()
            );

            // Prépare les variables du message
            Map<String, Object> messageVars = new HashMap<>(variables);
            messageVars.put("numeroCommande", numeroCommande);
            messageVars.putIfAbsent("typeTissu", "coton");
            messageVars.putIfAbsent("quantiteManquante", 10.0);

            System.out.println("[FOURNISSEUR] Envoi message CommandeMatieres");
            System.out.println("[FOURNISSEUR] numeroCommande = " + numeroCommande);

            // ✅ On publie le message "CommandeMatieres" qui déclenche SEFourn
            zeebeClient.newPublishMessageCommand()
                    .messageName("CommandeMatieres")     // <- doit correspondre au BPMN
                    .correlationKey(numeroCommande)       // <- clé de corrélation
                    .variables(messageVars)
                    .send()
                    .join();

            String msg = "Message CommandeMatieres envoyé !\n"
                    + "ProcFourn va démarrer automatiquement.\n"
                    + "numeroCommande : " + numeroCommande;

            System.out.println("[FOURNISSEUR] " + msg);
            return ResponseEntity.ok(msg);

        } catch (Exception e) {
            System.err.println("[FOURNISSEUR] Erreur envoi message : " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fournisseur Client actif - Port 8081");
    }
}