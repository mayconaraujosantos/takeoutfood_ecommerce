package com.ifoodclone.order.client;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentResult {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String status;
}
