package com.villatusia.pms.common.exception;

public final class PropertyInactiveException extends BusinessException {

    public PropertyInactiveException(long propertyId) {
        super(
                "PROPERTY_INACTIVE",
                "Property with id " + propertyId + " is not active."
        );
    }
}
