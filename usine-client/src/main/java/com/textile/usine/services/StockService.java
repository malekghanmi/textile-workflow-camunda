package com.textile.usine.services;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Service de verification du stock de tissus.
 * Simule une base de donnees de stock (en metres de tissu).
 */
@Service
public class StockService {

    // Simulation du stock disponible en metres
    private static final Map<String, Double> STOCK = new HashMap<>();

    static {
        STOCK.put("coton",     500.0);
        STOCK.put("polyester", 300.0);
        STOCK.put("lin",       150.0);
        STOCK.put("soie",       50.0);
        STOCK.put("laine",     200.0);
        STOCK.put("melange",   400.0);
    }

    /**
     * Verifie si le stock est suffisant.
     * Estimation : 2 metres de tissu par vetement.
     */
    public boolean isStockSuffisant(String typeTissu, int quantite) {
        double necessaire = quantite * 2.0;
        String key = (typeTissu != null) ? typeTissu.toLowerCase() : "coton";
        double disponible = STOCK.getOrDefault(key, 0.0);
        boolean suffisant = disponible >= necessaire;

        System.out.println("[STOCK] Tissu=" + typeTissu
                + " | Necessaire=" + necessaire + "m"
                + " | Disponible=" + disponible + "m"
                + " | Suffisant=" + suffisant);
        return suffisant;
    }

    /**
     * Retourne la quantite manquante (en metres) si le stock est insuffisant.
     */
    public double getQuantiteManquante(String typeTissu, int quantite) {
        double necessaire = quantite * 2.0;
        String key = (typeTissu != null) ? typeTissu.toLowerCase() : "coton";
        double disponible = STOCK.getOrDefault(key, 0.0);
        return Math.max(0.0, necessaire - disponible);
    }
}
