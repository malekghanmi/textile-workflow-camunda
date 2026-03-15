package com.textile.usine.controller;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour demarrer le processus ProcUsine.
 * POST http://localhost:8080/api/usine/start
 *
 * Corps JSON:
 * {
 *   "numeroCommande": "CMD-001",
 *   "nomClient":      "Asma Moussa",
 *   "emailClient":    "asma@test.com",
 *   "typeVetement":   "Veste",
 *   "typeTissu":      "coton",
 *   "quantite":       10,
 *   "priorite":       "normale"
 * }
 */
@RestController
@RequestMapping("/api/usine")
public class StartUsineController {

    @Autowired
    private ZeebeClient zeebeClient;

    @PostMapping("/start")
    public ResponseEntity<String> startProcess(@RequestBody Map<String, Object> variables) {
        try {
            ProcessInstanceEvent instance = zeebeClient
                    .newCreateInstanceCommand()
                    .bpmnProcessId("ProcUsine")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            String msg = "Processus ProcUsine demarre avec succes !\n"
                    + "ProcessInstanceKey : " + instance.getProcessInstanceKey() + "\n"
                    + "Version            : " + instance.getVersion();
            System.out.println("[USINE] " + msg);
            return ResponseEntity.ok(msg);

        } catch (Exception e) {
            System.err.println("[USINE] Erreur demarrage : " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur : " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Usine Client actif - Port 8080 - Cluster : 09e23dc2-c664-4aeb-9297-127895dc0a9f");
    }
}
