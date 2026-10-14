package com.villatusia.pms.common.exception;

public final class ReservationUnavailableException extends BusinessException {

    public ReservationUnavailableException() {
        super(
                "RESERVATION_DATES_UNAVAILABLE",
                "The property is already reserved for the selected dates."
        );
    }
}
