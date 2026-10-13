package com.villatusia.pms.property;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record UpdatePropertyRequest(

        @NotBlank
        @Size(max = 120)
        String name,

        @NotBlank
        @Size(max = 255)
        String location,

        @Size(max = 2000)
        String description,

        @NotNull
        @Min(1)
        @Max(2)
        Integer maxGuests,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 8, fraction = 2)
        BigDecimal baseNightlyRate,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}")
        String currency,

        @NotNull
        @Min(1)
        Integer minimumStayNights,

        @NotNull
        LocalTime checkInTime,

        @NotNull
        LocalTime checkOutTime,

        @NotBlank
        @Size(max = 50)
        String timeZone,

        @NotNull
        @Min(0)
        Integer cancellationNoticeDays,

        @NotNull
        Boolean active
) {
}
