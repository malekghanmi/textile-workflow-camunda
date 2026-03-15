package com.textile.usine.workers;

import com.textile.usine.models.Facture;
import com.textile.usine.services.FactureService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GenererFactureWorker {

    @Autowired
    private FactureService factureService;

    @JobWorker(type = "generate-invoice", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {
        System.out.println("\n[WORKER] ===== generate-invoice =====");
        System.out.println("[WORKER] JobKey : " + job.getKey());

        Map<String, Object> vars = job.getVariablesAsMap();
        String numeroCommande = (String) vars.getOrDefault("numeroCommande", "CMD-000");
        String nomClient      = (String) vars.getOrDefault("nomClient", "Client");
        int    quantite       = ((Number) vars.getOrDefault("quantite", 1)).intValue();
        String priorite       = (String) vars.getOrDefault("priorite", "normale");

        // Recuperer paiement si deja confirme par TRel (Relancer client)
        // Si la variable paiement existe deja et est true -> ne pas ecraser
        Object paiementExistant = vars.get("paiement");
        boolean dejaPaye = (paiementExistant instanceof Boolean) && (Boolean) paiementExistant;

        try {
            Facture facture = factureService.genererFacture(
                    numeroCommande, nomClient, quantite, priorite);

            // paiement = false par defaut (attente confirmation manuelle via TRel)
            // SAUF si deja confirme par le formulaire Relancer Client
            boolean paiementRecu = dejaPaye;
            facture.setPaiement(paiementRecu);

            Map<String, Object> result = new HashMap<>();
            result.put("paiement",       facture.isPaiement());
            result.put("numeroFacture",  facture.getNumeroFacture());
            result.put("montantTotal",   facture.getMontantTotal());
            // FIX: aussi exporter sous "montantFacture" pour le formulaire Relancer Client
            result.put("montantFacture", facture.getMontantTotal());
            result.put("dateFacture",    facture.getDateFacture());

            System.out.println("[WORKER] Facture=" + facture.getNumeroFacture()
                    + " | Montant=" + facture.getMontantTotal()
                    + " | Paiement=" + facture.isPaiement()
                    + " | DejaPayé=" + dejaPaye);

            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("[WORKER] Erreur generate-invoice : " + e.getMessage());
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}