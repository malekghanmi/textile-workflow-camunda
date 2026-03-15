package com.textile.fournisseur.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Matiere {
    private String referenceMatiere;
    private String typeTissu;
    private double quantite;
    private String lotNumero;
    private String dateExpedition;
    private String transporteur;
    private String numeroSuivi;
    private String statut;
}
