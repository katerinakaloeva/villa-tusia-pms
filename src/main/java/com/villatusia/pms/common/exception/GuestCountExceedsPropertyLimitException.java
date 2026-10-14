package com.villatusia.pms.common.exception;

public final class GuestCountExceedsPropertyLimitException
        extends BusinessException {

    public GuestCountExceedsPropertyLimitException(
            int requestedGuestCount,
            int maxGuests
    ) {
        super(
                "GUEST_COUNT_EXCEEDS_PROPERTY_LIMIT",
                "Guest count " + requestedGuestCount
                        + " exceeds the property's maximum of " + maxGuests + "."
        );
    }
}
