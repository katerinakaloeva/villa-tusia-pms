package com.villatusia.pms.common.exception;

public final class PropertyNotFoundException extends BusinessException {

    public PropertyNotFoundException(long propertyId) {
        super(
                "PROPERTY_NOT_FOUND",
                "Property with id " + propertyId + " was not found."
        );
    }
}
