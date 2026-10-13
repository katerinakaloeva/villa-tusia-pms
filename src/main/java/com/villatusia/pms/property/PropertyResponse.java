package com.villatusia.pms.property;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

public record PropertyResponse(
        Long id,
        String name,
        String location,
        String description,
        int maxGuests,
        BigDecimal baseNightlyRate,
        String currency,
        int minimumStayNights,
        LocalTime checkInTime,
        LocalTime checkOutTime,
        String timeZone,
        int cancellationNoticeDays,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getLocation(),
                property.getDescription(),
                property.getMaxGuests(),
                property.getBaseNightlyRate(),
                property.getCurrency(),
                property.getMinimumStayNights(),
                property.getCheckInTime(),
                property.getCheckOutTime(),
                property.getTimeZone(),
                property.getCancellationNoticeDays(),
                property.isActive(),
                property.getCreatedAt(),
                property.getUpdatedAt()
        );
    }
}
