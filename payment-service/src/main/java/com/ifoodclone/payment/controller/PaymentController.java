package com.ifoodclone.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifoodclone.payment.dto.PaymentDto;
import com.ifoodclone.payment.dto.PaymentDto.ApiResponse;
import com.ifoodclone.payment.dto.PaymentDto.PaymentInfo;
import com.ifoodclone.payment.entity.Payment;
import com.ifoodclone.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Processamento de pagamentos de pedidos")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentInfo>> process(@Valid @RequestBody PaymentDto.ProcessRequest request) {
        Payment payment = paymentService.process(request);

        if (payment.getStatus() == Payment.PaymentStatus.REJECTED) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(ApiResponse.error("Pagamento recusado", "REJECTED"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pagamento aprovado", PaymentInfo.from(payment)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentInfo>> getByOrderId(@PathVariable Long orderId) {
        try {
            Payment payment = paymentService.getByOrderId(orderId);
            return ResponseEntity.ok(ApiResponse.success(PaymentInfo.from(payment)));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
        }
    }
}
