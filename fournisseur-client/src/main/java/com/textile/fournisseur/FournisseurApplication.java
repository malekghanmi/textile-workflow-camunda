package com.textile.fournisseur;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FournisseurApplication {
    public static void main(String[] args) {
        SpringApplication.run(FournisseurApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  FOURNISSEUR CLIENT demarre - Port 8081");
        System.out.println("  Workers Zeebe actifs :");
        System.out.println("   -> confirm-order-erp");
        System.out.println("   -> send-delivery-notification");
        System.out.println("==============================================");
    }
}
