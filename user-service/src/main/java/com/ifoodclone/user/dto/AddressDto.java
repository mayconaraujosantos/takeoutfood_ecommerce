package com.ifoodclone.user.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ifoodclone.user.entity.Address;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AddressDto {

    private AddressDto() {
    }

    // @Data + @Builder alone suppresses the no-args constructor Jackson needs to
    // deserialize a @RequestBody (Lombok only auto-generates one when no other
    // constructor exists, and @Builder's internal all-args constructor counts as one).
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Rótulo é obrigatório")
        private String label;
        @NotBlank(message = "Rua é obrigatória")
        private String street;
        private String number;
        private String complement;
        private String neighborhood;
        @NotBlank(message = "Cidade é obrigatória")
        private String city;
        @NotBlank(message = "Estado é obrigatório")
        private String state;
        private String zipCode;
        @Builder.Default
        private boolean isDefault = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String label;
        private String street;
        private String number;
        private String complement;
        private String neighborhood;
        private String city;
        private String state;
        private String zipCode;
    }

    @Data
    @Builder
    public static class AddressInfo {
        private Long id;
        private String label;
        private String street;
        private String number;
        private String complement;
        private String neighborhood;
        private String city;
        private String state;
        private String zipCode;
        private Boolean isDefault;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;

        public static AddressInfo from(Address address) {
            return AddressInfo.builder()
                    .id(address.getId())
                    .label(address.getLabel())
                    .street(address.getStreet())
                    .number(address.getNumber())
                    .complement(address.getComplement())
                    .neighborhood(address.getNeighborhood())
                    .city(address.getCity())
                    .state(address.getState())
                    .zipCode(address.getZipCode())
                    .isDefault(address.getIsDefault())
                    .createdAt(address.getCreatedAt())
                    .build();
        }
    }
}
