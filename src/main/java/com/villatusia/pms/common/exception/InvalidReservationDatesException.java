package com.villatusia.pms.common.exception;

public final class InvalidReservationDatesException extends BusinessException {

    public InvalidReservationDatesException() {
        super(
                "INVALID_RESERVATION_DATES",
                "Check-out date must be after check-in date."
        );
    }
}
