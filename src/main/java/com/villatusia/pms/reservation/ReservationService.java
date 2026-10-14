package com.villatusia.pms.reservation;

import com.villatusia.pms.common.exception.GuestCountExceedsPropertyLimitException;
import com.villatusia.pms.common.exception.InvalidReservationDatesException;
import com.villatusia.pms.common.exception.MinimumStayNotMetException;
import com.villatusia.pms.common.exception.PropertyInactiveException;
import com.villatusia.pms.common.exception.PropertyNotFoundException;
import com.villatusia.pms.common.exception.ReservationUnavailableException;
import com.villatusia.pms.property.Property;
import com.villatusia.pms.property.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PropertyService propertyService;

    public List<Reservation> getByPropertyId(long propertyId) {
        propertyService.getById(propertyId);

        return reservationRepository.findByProperty_IdOrderByCheckInDateAscIdAsc(propertyId);
    }

    @Transactional
    public Reservation create(
            long propertyId,
            CreateReservationRequestDto request
    ) {
        Property property = propertyService.getById(propertyId);

        if (!property.isActive()) {
            throw new PropertyInactiveException(propertyId);
        }

        LocalDate checkInDate = request.checkInDate();
        LocalDate checkOutDate = request.checkOutDate();

        if (!checkOutDate.isAfter(checkInDate)) {
            throw new InvalidReservationDatesException();
        }

        if (request.guestCount() > property.getMaxGuests()) {
            throw new GuestCountExceedsPropertyLimitException(
                    request.guestCount(),
                    property.getMaxGuests()
            );
        }

        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        int minimumStayNights = property.getMinimumStayNights();

        if (nights < minimumStayNights) {
            throw new MinimumStayNotMetException(nights, minimumStayNights);
        }

        boolean datesUnavailable =
                reservationRepository
                        .existsByProperty_IdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                                propertyId,
                                ReservationStatus.CONFIRMED,
                                checkOutDate,
                                checkInDate
                        );

        if (datesUnavailable) {
            throw new ReservationUnavailableException();
        }

        BigDecimal agreedNightlyRate = property.getBaseNightlyRate();
        BigDecimal totalAmount = agreedNightlyRate.multiply(
                BigDecimal.valueOf(nights)
        );

        Reservation reservation = new Reservation(
                property,
                request.guestName(),
                request.guestEmail(),
                request.guestPhone(),
                checkInDate,
                checkOutDate,
                request.guestCount(),
                agreedNightlyRate,
                totalAmount,
                property.getCurrency()
        );

        return reservationRepository.save(reservation);
    }
}
