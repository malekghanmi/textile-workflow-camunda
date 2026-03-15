package com.textile.usine.workers;

import com.textile.usine.exceptions.ErpException;
import com.textile.usine.services.ErpService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker Zeebe pour la Service Task "Calculer besoins matieres" (TCalc).
 * Job type : calculate-material-needs
 *
 * Variables INPUT  : typeTissu, quantite, numeroCommande
 * Variables OUTPUT : stock (boolean), quantiteManquante (double)
 *
 * Si ErpException est levee -> lance BpmnError ERP_ERROR
 * -> interceptee par Error Boundary Event BEErr -> redirect vers TIT (Notifier service IT)
 */
@Component
public class CalculerBesoinsWorker {

    @Autowired
    private ErpService erpService;

    @JobWorker(type = "calculate-material-needs", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {
        System.out.println("\n[WORKER] ===== calculate-material-needs =====");
        System.out.println("[WORKER] JobKey : " + job.getKey());

        Map<String, Object> vars = job.getVariablesAsMap();
        String typeTissu       = (String) vars.getOrDefault("typeTissu", "coton");
        int    quantite        = ((Number) vars.getOrDefault("quantite", 1)).intValue();
        String numeroCommande  = (String) vars.getOrDefault("numeroCommande", "UNKNOWN");

        System.out.println("[WORKER] Input -> commande=" + numeroCommande
                + " | tissu=" + typeTissu + " | quantite=" + quantite);

        try {
            boolean stock      = erpService.calculerBesoins(typeTissu, quantite);
            double  manquante  = stock ? 0.0 : erpService.getQuantiteManquante(typeTissu, quantite);

            Map<String, Object> result = new HashMap<>();
            result.put("stock",              stock);
            result.put("quantiteManquante",  manquante);
            result.put("typeTissuCalcule",   typeTissu);

            System.out.println("[WORKER] Output -> stock=" + stock + " | manquante=" + manquante + "m");

            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (ErpException e) {
            // !! Lance l'erreur BPMN qui declenche le Error Boundary Event BEErr !!
            System.err.println("[WORKER] ERREUR ERP -> code=" + e.getErrorCode()
                    + " | msg=" + e.getMessage());

            client.newThrowErrorCommand(job.getKey())
                    .errorCode("ERP_ERROR")
                    .errorMessage("ERP indisponible : " + e.getMessage())
                    .send()
                    .join();
        }
    }
}
