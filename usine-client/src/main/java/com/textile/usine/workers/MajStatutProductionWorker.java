package com.textile.usine.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker Zeebe pour la Service Task "MAJ statut production" (SPT4).
 * Job type : update-production-status
 * Se trouve dans le sous-processus SPFab (Fabrication du Vetement).
 *
 * Variables INPUT  : numeroCommande
 * Variables OUTPUT : statutMisAJour, dateMajProduction
 */
@Component
public class MajStatutProductionWorker {

    @JobWorker(type = "update-production-status", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {
        System.out.println("\n[WORKER] ===== update-production-status =====");
        System.out.println("[WORKER] JobKey : " + job.getKey());

        Map<String, Object> vars = job.getVariablesAsMap();
        String numeroCommande = (String) vars.getOrDefault("numeroCommande", "UNKNOWN");

        try {
            // Simulation MAJ en base de donnees
            Thread.sleep(200);

            String statut  = "FABRICATION_TERMINEE";
            String dateMaj = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            System.out.println("[WORKER] MAJ -> commande=" + numeroCommande
                    + " | statut=" + statut + " | date=" + dateMaj);

            Map<String, Object> result = new HashMap<>();
            result.put("statutMisAJour",    statut);
            result.put("dateMajProduction", dateMaj);

            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("[WORKER] Erreur update-production-status : " + e.getMessage());
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}
