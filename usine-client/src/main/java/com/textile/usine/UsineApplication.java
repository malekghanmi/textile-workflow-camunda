package com.textile.usine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsineApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsineApplication.class, args);
        System.out.println("==============================================");
        System.out.println("  USINE CLIENT demarre - Port 8080");
        System.out.println("  Workers Zeebe actifs :");
        System.out.println("   -> calculate-material-needs");
        System.out.println("   -> generate-invoice");
        System.out.println("   -> update-production-status");
        System.out.println("==============================================");
    }
}
