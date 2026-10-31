package com.villatusia.pms.common.exception;

public final class ReservationCancellationException extends BusinessException {

    public ReservationCancellationException(String code, String message) {
        super(code, message);
    }
}