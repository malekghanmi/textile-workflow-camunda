package com.textile.usine.services;

import com.textile.usine.exceptions.ErpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service ERP : calcule les besoins en matieres premieres.
 * Lance ErpException si le systeme ERP est indisponible.
 *
 * Pour simuler une erreur ERP : mettre ERP_DISPONIBLE = false
 * -> cela declenchera le Error Boundary Event BEErr dans le BPMN.
 */
@Service
public class ErpService {

    @Autowired
    private StockService stockService;

    // Mettre false pour tester le Error Boundary Event ERP_ERROR
    private static final boolean ERP_DISPONIBLE = true;

    public boolean calculerBesoins(String typeTissu, int quantite) {
        System.out.println("[ERP] Debut calcul besoins -> tissu=" + typeTissu + " | quantite=" + quantite);

        if (!ERP_DISPONIBLE) {
            throw new ErpException("Systeme ERP indisponible - connexion refusee", "ERP_ERROR");
        }

        // Simulation temps de traitement ERP
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        boolean stock = stockService.isStockSuffisant(typeTissu, quantite);
        System.out.println("[ERP] Resultat -> stock suffisant = " + stock);
        return stock;
    }

    public double getQuantiteManquante(String typeTissu, int quantite) {
        return stockService.getQuantiteManquante(typeTissu, quantite);
    }
}
