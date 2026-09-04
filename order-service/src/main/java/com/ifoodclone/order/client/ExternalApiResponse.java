package com.ifoodclone.order.client;

import lombok.Data;
import lombok.NoArgsConstructor;

// Mirrors the {success, message, data, error, timestamp} envelope every service in
// this repo wraps its responses in (see e.g. RestaurantDto.ApiResponse). Used here to
// unwrap responses from services called directly over HTTP.
@Data
@NoArgsConstructor
public class ExternalApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
}
