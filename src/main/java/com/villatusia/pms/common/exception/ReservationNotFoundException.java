package com.villatusia.pms.common.exception;

public final class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException(long reservationId) {
        super(
                "RESERVATION_NOT_FOUND",
                "Reservation " + reservationId + " was not found for this property."
        );
    }
}