package com.villatusia.pms.property;

import com.villatusia.pms.common.exception.PropertyNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public Property getById(long propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(
                        () -> new PropertyNotFoundException(propertyId)
                );
    }

    @Transactional
    public Property update(long propertyId, UpdatePropertyRequest request) {
        Property property = getById(propertyId);
        property.update(
                request.name(),
                request.location(),
                request.description(),
                request.maxGuests(),
                request.baseNightlyRate(),
                request.currency(),
                request.minimumStayNights(),
                request.checkInTime(),
                request.checkOutTime(),
                request.timeZone(),
                request.cancellationNoticeDays(),
                request.active()
        );
        return property;
    }
}
