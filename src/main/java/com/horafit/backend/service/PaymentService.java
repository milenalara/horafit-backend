package com.horafit.backend.service;

import com.horafit.backend.dto.payment.UpdatePaymentDTO;
import com.horafit.backend.entity.Payment;
import com.horafit.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public String getUltimoPagamento(Long clientId) {
        Optional<Payment> latestPayment = paymentRepository.findLatestPaymentByClientId(clientId);
        if (latestPayment.isPresent()) {
            Date confirmedDate = latestPayment.get().getConfirmed();
            return dateFormat.format(confirmedDate);
        } else {
            return "Nenhum pagamento confirmado registrado.";
        }
    }

    public boolean verificarPagamento(Long clientId) {
        Optional<Payment> latestPayment = paymentRepository.findLatestPaymentByClientId(clientId);
        return latestPayment.isPresent() && latestPayment.get().getConfirmed() != null;
    }

    public boolean verificarPagamentoDoMesAtual(Long clientId) {
        Optional<Payment> latestPayment = paymentRepository.findLatestPaymentByClientId(clientId);
        if (latestPayment.isPresent()) {
            Date confirmedDate = latestPayment.get().getConfirmed();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(confirmedDate);
            
            int paymentMonth = calendar.get(Calendar.MONTH);
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
            
            return paymentMonth == currentMonth;
        }
        return false;
    }

    public boolean atualizarPagamento(UpdatePaymentDTO updatePaymentRequest) {
        Payment payment = paymentRepository.findByClientId(updatePaymentRequest.getClientId())
                .orElseGet(() -> {
                    Payment newPayment = new Payment();
                    newPayment.setClientId(updatePaymentRequest.getClientId());
                    return newPayment;
                });

        payment.setConfirmed(updatePaymentRequest.getDate());
        paymentRepository.save(payment);

        return true;
    }
}
