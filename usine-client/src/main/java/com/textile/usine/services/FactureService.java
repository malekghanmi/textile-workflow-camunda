package com.textile.usine.services;

import com.textile.usine.models.Facture;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service de generation de factures.
 * Calcule le montant total et cree une facture.
 */
@Service
public class FactureService {

    private static final double PRIX_UNITAIRE = 45.0; // EUR par vetement

    public Facture genererFacture(String numeroCommande, String nomClient, int quantite, String priorite) {
        System.out.println("[FACTURE] Generation facture pour commande : " + numeroCommande);

        double montantTotal = quantite * PRIX_UNITAIRE;

        // Majoration 20% si livraison urgente
        if ("urgente".equalsIgnoreCase(priorite)) {
            montantTotal *= 1.20;
        }

        Facture facture = new Facture();
        facture.setNumeroFacture("FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        facture.setNumeroCommande(numeroCommande);
        facture.setNomClient(nomClient);
        facture.setMontantTotal(montantTotal);
        facture.setPaiement(false); // Par defaut en attente - sera confirme manuellement
        facture.setDateFacture(LocalDate.now().toString());
        facture.setStatut("EN_ATTENTE_PAIEMENT");

        System.out.println("[FACTURE] Creee : " + facture.getNumeroFacture()
                + " | Montant : " + montantTotal + " EUR"
                + " | Paiement : " + facture.isPaiement());
        return facture;
    }
}
