package com.textile.usine.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EnvoyerCommandeMatiereWorker {

    @Autowired
    private ZeebeClient zeebeClient;

    @JobWorker(type = "envoyer-commande-matiere", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {
        System.out.println("[WORKER] ===== envoyer-commande-matiere =====");

        Map<String, Object> vars = job.getVariablesAsMap();
        String numeroCommande = (String) vars.getOrDefault("numeroCommande", "UNKNOWN");
        String typeTissu      = (String) vars.getOrDefault("typeTissu", "coton");
        double quantiteManquante = ((Number) vars.getOrDefault("quantiteManquante", 10.0)).doubleValue();

        try {
            Map<String, Object> messageVars = new HashMap<>(vars);

            System.out.println("[WORKER] Envoi message CommandeMatieres -> commande=" + numeroCommande);

            zeebeClient.newPublishMessageCommand()
                    .messageName("CommandeMatieres")
                    .correlationKey(numeroCommande)
                    .variables(messageVars)
                    .send()
                    .join();

            System.out.println("[WORKER] Message CommandeMatieres envoye !");

            client.newCompleteCommand(job.getKey())
                    .variables(Map.of("commandeMatieresEnvoyee", true))
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("[WORKER] Erreur : " + e.getMessage());
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}