package com.textile.fournisseur.services;

import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service d'envoi du message Zeebe "LivraisonConfirmee".
 *
 * Ce message est envoye depuis le pool Fournisseur vers le pool Usine.
 * Il debloque le Message Intermediate Catch Event "CELiv" dans ProcUsine.
 *
 * CLE DE CORRELATION : numeroCommande
 * -> Zeebe utilise cette valeur pour retrouver l'instance ProcUsine en attente.
 */
@Service
public class NotificationService {

    @Autowired
    private ZeebeClient zeebeClient;

    public void envoyerNotificationLivraison(
            String numeroCommande,
            String numeroSuivi,
            String transporteur,
            String dateLivraisonEstimee) {

        System.out.println("[NOTIFICATION] Envoi message LivraisonConfirmee");
        System.out.println("[NOTIFICATION] Correlation key : numeroCommande=" + numeroCommande);

        Map<String, Object> messageVars = new HashMap<>();
        messageVars.put("numeroSuiviMatiere",          numeroSuivi);
        messageVars.put("transporteurMatiere",          transporteur);
        messageVars.put("dateLivraisonEstimeeMatiere",  dateLivraisonEstimee);
        messageVars.put("livraisonConfirmee",           true);

        // Publication du message Zeebe -> doit correspondre au messageName du BPMN
        zeebeClient.newPublishMessageCommand()
                .messageName("LivraisonConfirmee")    // <- doit etre identique au BPMN
                .correlationKey(numeroCommande)         // <- cle de correlation
                .variables(messageVars)
                .send()
                .join();

        System.out.println("[NOTIFICATION] Message LivraisonConfirmee envoye avec succes !");
    }
}
