package com.horafit.backend.controller;

import com.horafit.backend.dto.payment.UpdatePaymentDTO;
import com.horafit.backend.dto.response.ResponseDTO;
import com.horafit.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pagamentos")
@CrossOrigin
@Tag(name = "Payment API", description = "API para operações relacionadas a pagamentos.")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "Obter último pagamento do cliente.",
            description = "Este endpoint permite que o cliente veja a data do último pagamento realizado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Último pagamento retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum pagamento encontrado para o cliente")
    })
    @GetMapping("/ultimo/{clientId}")
    public ResponseEntity<String> getUltimoPagamento(@PathVariable Long clientId) {
        String ultimoPagamento = paymentService.getUltimoPagamento(clientId);
        if (ultimoPagamento.contains("Nenhum pagamento")) {
            return ResponseEntity.status(404).body(ultimoPagamento);
        }
        return ResponseEntity.ok(ultimoPagamento);
    }

    @Operation(summary = "Verificar se o cliente tem pagamento confirmado.",
            description = "Este endpoint verifica se o cliente tem um pagamento confirmado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento confirmado"),
            @ApiResponse(responseCode = "404", description = "Nenhum pagamento confirmado encontrado")
    })
    @GetMapping("/verificar/{clientId}")
    public ResponseEntity<String> verificarPagamento(@PathVariable Long clientId) {
        boolean pagamentoConfirmado = paymentService.verificarPagamento(clientId);
        if (pagamentoConfirmado) {
            return ResponseEntity.ok("Pagamento confirmado");
        } else {
            return ResponseEntity.status(404).body("Nenhum pagamento confirmado encontrado");
        }
    }

    @Operation(summary = "Verificar se o cliente tem pagamento confirmado para o mês atual.",
            description = "Este endpoint verifica se o cliente tem um pagamento confirmado para o mês atual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagamento confirmado para o mês atual"),
            @ApiResponse(responseCode = "404", description = "Nenhum pagamento confirmado encontrado para o mês atual")
    })
    @GetMapping("/verificarMesAtual/{clientId}")
    public ResponseEntity<String> verificarPagamentoMesAtual(@PathVariable Long clientId) {
        boolean pagamentoConfirmado = paymentService.verificarPagamentoDoMesAtual(clientId);
        if (pagamentoConfirmado) {
            return ResponseEntity.ok("Pagamento confirmado para o mês atual");
        } else {
            return ResponseEntity.status(404).body("Nenhum pagamento confirmado encontrado para o mês atual");
        }
    }

    @Operation(summary = "Atualizar a data de pagamento de um cliente.",
            description = "Este endpoint permite atualizar a data de pagamento para um cliente específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data de pagamento atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente ou pagamento não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos enviados na requisição")
    })
    @PutMapping("/atualizarPagamento")
    public ResponseEntity<ResponseDTO> atualizarPagamento(@RequestBody UpdatePaymentDTO updatePaymentRequest) {
        try {
            paymentService.atualizarPagamento(updatePaymentRequest);

            return ResponseEntity.ok(new ResponseDTO("Data de pagamento atualizada com sucesso", 200));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResponseDTO("Erro interno no servidor: " + e.getMessage(), 500));
        }
    }
}
