package com.textile.usine.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facture {
    private String  numeroFacture;
    private String  numeroCommande;
    private String  nomClient;
    private double  montantTotal;
    private boolean paiement;
    private String  dateFacture;
    private String  statut;
}
