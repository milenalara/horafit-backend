package com.horafit.backend.dto.appointment;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class AppointmentEditBatchDTO {
        private String frequency; // Frequência (ex.: "Semanal")
        private String modality; // Nova modalidade do atendimento
        private Long physiotherapistId; // ID do fisioterapeuta
        private String location; // Localização do atendimento
        private RepetirDTO repetir;
        private Map<String, List<LocalTime>> daysAndTimes; // Dias e horários no formato dia -> [horário, ...]

        public String getFrequency() {
                return frequency;
        }

        public String getModality() {
                return modality;
        }

        public Long getPhysiotherapistId() {
                return physiotherapistId;
        }

        public String getLocation() {
                return location;
        }

        public Map<String, List<LocalTime>> getDaysAndTimes() {
                return daysAndTimes;
        }

        public RepetirDTO getRepetir() {
                return repetir;
        }
}
