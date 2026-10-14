package com.villatusia.pms.common.exception;

public final class MinimumStayNotMetException extends BusinessException {

    public MinimumStayNotMetException(long requestedNights, int minimumStayNights) {
        super(
                "MINIMUM_STAY_NOT_MET",
                "Requested stay of " + requestedNights
                        + " nights is shorter than the property's minimum of "
                        + minimumStayNights + " nights."
        );
    }
}
