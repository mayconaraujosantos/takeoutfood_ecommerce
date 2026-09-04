package com.ifoodclone.order.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RestaurantSummary {
    private Long id;
    private String name;
    private Boolean active;
}
