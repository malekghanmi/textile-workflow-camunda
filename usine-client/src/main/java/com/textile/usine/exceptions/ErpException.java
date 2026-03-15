package com.textile.usine.exceptions;

/**
 * Exception metier ERP.
 * Declenchee par ErpService quand le systeme ERP est indisponible.
 * Interceptee par CalculerBesoinsWorker pour lancer la BpmnError ERP_ERROR.
 */
public class ErpException extends RuntimeException {

    private final String errorCode;

    public ErpException(String message) {
        super(message);
        this.errorCode = "ERP_ERROR";
    }

    public ErpException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
