package com.textile.fournisseur.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Worker Zeebe pour la Service Task "Confirmer commande ERP" (TCC).
 * Job type : confirm-order-erp
 *
 * Variables INPUT  : numeroCommande, typeTissu, quantiteManquante
 * Variables OUTPUT : confirmationId, commandeErpConfirmee (boolean)
 */
@Component
public class ConfirmerCommandeErpWorker {

    @JobWorker(type = "confirm-order-erp", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {
        System.out.println("\n[WORKER] ===== confirm-order-erp =====");
        System.out.println("[WORKER] JobKey : " + job.getKey());

        Map<String, Object> vars = job.getVariablesAsMap();
        String numeroCommande = (String) vars.getOrDefault("numeroCommande", "UNKNOWN");
        String typeTissu      = (String) vars.getOrDefault("typeTissu", "coton");
        double quantite       = ((Number) vars.getOrDefault("quantiteManquante", 10.0)).doubleValue();

        try {
            // Simulation confirmation dans le systeme ERP fournisseur
            Thread.sleep(300);

            String confirmationId = "CONF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

            System.out.println("[WORKER] Commande confirmee -> " + confirmationId
                    + " | commande=" + numeroCommande
                    + " | tissu=" + typeTissu
                    + " | quantite=" + quantite + "m");

            Map<String, Object> result = new HashMap<>();
            result.put("confirmationId",         confirmationId);
            result.put("commandeErpConfirmee",   true);

            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("[WORKER] Erreur confirm-order-erp : " + e.getMessage());
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}
