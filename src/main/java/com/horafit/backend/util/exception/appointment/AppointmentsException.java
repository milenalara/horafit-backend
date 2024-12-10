package com.horafit.backend.util.exception.appointment;

public class AppointmentsException {
    public static class AppointmentAlreadyExistsException extends RuntimeException{
        public AppointmentAlreadyExistsException(String message) {
            super(message);
        }
    }

    public static class AppointmentDeleteException extends RuntimeException{
        public AppointmentDeleteException(String message) {
            super(message);
        }
    }


    public static class AppointmentNotFoundException  extends RuntimeException {
        public AppointmentNotFoundException (String message) {
            super(message);
        }
    }
}
