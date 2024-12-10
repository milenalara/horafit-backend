package com.horafit.backend.util.exception.physiotherapist;

public class PhysiotherapistException extends RuntimeException{
    public static class ClientNotFound extends RuntimeException {
        public ClientNotFound(String message) {
            super(message);
        }
    }
}
