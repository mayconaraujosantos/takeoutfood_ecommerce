package com.ifoodclone.order.client;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MenuItemSummary {
    private Long id;
    private Long restaurantId;
    private String name;
    private BigDecimal price;
    private Boolean available;
}
