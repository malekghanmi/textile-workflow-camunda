package com.textile.usine.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commande {
    private String numeroCommande;
    private String nomClient;
    private String emailClient;
    private String typeVetement;
    private String typeTissu;
    private String referenceModele;
    private String taille;
    private String couleur;
    private int    quantite;
    private String priorite;
    private String dateLivraisonSouhaitee;
}
