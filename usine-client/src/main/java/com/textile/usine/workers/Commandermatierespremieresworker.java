package com.textile.usine.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker Zeebe pour la Service Task "Commander matieres premieres" (TCmd).
 * Job type : commander-matieres
 *
 * Ce worker envoie le message Zeebe "CommandeMatieres" pour démarrer
 * le processus collaborateur ProcFourn (pool Fournisseur).
 *
 * Variables INPUT  : numeroCommande, typeTissu, quantiteManquante, priorite
 * Variables OUTPUT : commandeMatieresEnvoyee (boolean)
 */
@Component
class CommanderMatieresPremieresWorker {

    @Autowired
    private ZeebeClient zeebeClient;

    @JobWorker(type = "commander-matieres", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {
        System.out.println("\n[WORKER] ===== commander-matieres =====");
        System.out.println("[WORKER] JobKey : " + job.getKey());

        Map<String, Object> vars = job.getVariablesAsMap();
        String numeroCommande   = (String) vars.getOrDefault("numeroCommande", "UNKNOWN");
        String typeTissu        = (String) vars.getOrDefault("typeTissu", "coton");
        double quantiteManquante = ((Number) vars.getOrDefault("quantiteManquante", 0.0)).doubleValue();
        String priorite         = (String) vars.getOrDefault("priorite", "normale");

        System.out.println("[WORKER] Input -> commande=" + numeroCommande
                + " | tissu=" + typeTissu
                + " | quantiteManquante=" + quantiteManquante + "m"
                + " | priorite=" + priorite);

        try {
            // Préparer les variables pour le processus fournisseur
            Map<String, Object> messageVars = new HashMap<>();
            messageVars.put("numeroCommande",    numeroCommande);
            messageVars.put("typeTissu",         typeTissu);
            messageVars.put("quantiteManquante", quantiteManquante);
            messageVars.put("priorite",          priorite);

            // Envoyer le message Zeebe pour démarrer ProcFourn
            zeebeClient.newPublishMessageCommand()
                    .messageName("CommandeMatieres")
                    .correlationKey(numeroCommande)
                    .variables(messageVars)
                    .send()
                    .join();

            System.out.println("[WORKER] Message 'CommandeMatieres' envoyé avec succès pour commande=" + numeroCommande);

            // Retourner les variables au processus Usine
            Map<String, Object> result = new HashMap<>();
            result.put("commandeMatieresEnvoyee", true);

            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

            System.out.println("[WORKER] Job complété -> en attente de la livraison fournisseur");

        } catch (Exception e) {
            System.err.println("[WORKER] ERREUR envoi message : " + e.getMessage());
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage("Erreur envoi CommandeMatieres : " + e.getMessage())
                    .send()
                    .join();
        }
    }
}