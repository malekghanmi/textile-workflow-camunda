package com.textile.fournisseur.workers;

import com.textile.fournisseur.services.NotificationService;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Worker Zeebe pour la Service Task "Envoyer notification livraison" (TNotifLiv).
 * Job type : send-delivery-notification
 *
 * ACTION CRITIQUE : publie le message Zeebe "LivraisonConfirmee"
 * avec cle de correlation = numeroCommande.
 * -> Doit correspondre exactement au messageName et correlationKey du BPMN.
 *
 * Variables INPUT  : numeroCommande, confirmationId
 * Variables OUTPUT : notificationEnvoyee (boolean), numeroSuiviMatiere
 */
@Component
public class EnvoyerNotificationLivraisonWorker {

    @Autowired
    private NotificationService notificationService;

    @JobWorker(type = "send-delivery-notification", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {
        System.out.println("\n[WORKER] ===== send-delivery-notification =====");
        System.out.println("[WORKER] JobKey : " + job.getKey());

        Map<String, Object> vars = job.getVariablesAsMap();
        String numeroCommande = (String) vars.getOrDefault("numeroCommande", "UNKNOWN");
        String transporteur   = (String) vars.getOrDefault("transporteurMatiere", "DHL");

        try {
            String numeroSuivi          = "SUIVI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String dateLivraisonEstimee = LocalDate.now().plusDays(2).toString();

            System.out.println("[WORKER] Envoi notification livraison pour commande : " + numeroCommande);

            // Envoie le message Zeebe qui deverrouille CELiv dans ProcUsine
            notificationService.envoyerNotificationLivraison(
                    numeroCommande,
                    numeroSuivi,
                    transporteur,
                    dateLivraisonEstimee
            );

            Map<String, Object> result = new HashMap<>();
            result.put("notificationEnvoyee",       true);
            result.put("numeroSuiviMatiere",         numeroSuivi);
            result.put("dateLivraisonEstimeeMatiere", dateLivraisonEstimee);

            System.out.println("[WORKER] Notification envoyee -> suivi=" + numeroSuivi);

            client.newCompleteCommand(job.getKey())
                    .variables(result)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("[WORKER] Erreur send-delivery-notification : " + e.getMessage());
            client.newFailCommand(job.getKey())
                    .retries(job.getRetries() - 1)
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}
