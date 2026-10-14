package com.villatusia.pms.reservation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateReservationRequestDto(

        @NotBlank
        @Size(max = 120)
        String guestName,

        @NotBlank
        @Email
        @Size(max = 254)
        String guestEmail,

        @NotBlank
        @Size(max = 32)
        String guestPhone,

        @NotNull
        LocalDate checkInDate,

        @NotNull
        LocalDate checkOutDate,

        @NotNull
        @Min(1)
        Integer guestCount
) {
}
