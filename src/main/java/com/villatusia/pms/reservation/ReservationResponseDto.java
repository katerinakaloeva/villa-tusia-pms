package com.villatusia.pms.reservation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ReservationResponseDto(
        Long id,
        Long propertyId,
        String guestName,
        String guestEmail,
        String guestPhone,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        int guestCount,
        BigDecimal agreedNightlyRate,
        BigDecimal totalAmount,
        String currency,
        ReservationStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ReservationResponseDto from(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getProperty().getId(),
                reservation.getGuestName(),
                reservation.getGuestEmail(),
                reservation.getGuestPhone(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getGuestCount(),
                reservation.getAgreedNightlyRate(),
                reservation.getTotalAmount(),
                reservation.getCurrency(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
