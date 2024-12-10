package com.horafit.backend.util.exception.client;

public class ClientException extends RuntimeException {
    public static class EmailNotFoundException extends RuntimeException {
        public EmailNotFoundException(String message) {
            super(message);
        }
    }

    public static class IncorrectPasswordException extends RuntimeException {
        public IncorrectPasswordException(String message) {
            super(message);
        }
    }

    public static class ClientNotFoundException extends RuntimeException {
        public ClientNotFoundException(String message) {
            super(message);
        }
    }

    public static class ClientAlreadyExistsException extends RuntimeException {
        public ClientAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class ContractAlreadySigned extends RuntimeException {
        public ContractAlreadySigned(String message) {
            super(message);
        }
    }

}

