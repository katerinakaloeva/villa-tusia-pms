package com.villatusia.pms.reservation;

import com.villatusia.pms.common.exception.GuestCountExceedsPropertyLimitException;
import com.villatusia.pms.common.exception.InvalidReservationDatesException;
import com.villatusia.pms.common.exception.MinimumStayNotMetException;
import com.villatusia.pms.common.exception.PropertyInactiveException;
import com.villatusia.pms.common.exception.PropertyNotFoundException;
import com.villatusia.pms.common.exception.ReservationUnavailableException;
import com.villatusia.pms.common.exception.ReservationCancellationException;
import com.villatusia.pms.common.exception.ReservationNotFoundException;
import com.villatusia.pms.property.Property;
import com.villatusia.pms.property.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {

    private final Clock clock;
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

    @Transactional
    public Reservation cancel(long propertyId, long reservationId) {
        Property property = propertyService.getById(propertyId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!Objects.equals(reservation.getProperty().getId(), propertyId)) {
            throw new ReservationNotFoundException(reservationId);
        }

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ReservationCancellationException(
                    "RESERVATION_ALREADY_CANCELLED",
                    "This reservation is already cancelled."
            );
        }

        LocalDate today = Instant.now(clock)
                .atZone(ZoneId.of(property.getTimeZone()))
                .toLocalDate();
        LocalDate cancellationDeadline = reservation.getCheckInDate()
                .minusDays(property.getCancellationNoticeDays());

        if (!today.isBefore(cancellationDeadline)) {
            throw new ReservationCancellationException(
                    "CANCELLATION_WINDOW_CLOSED",
                    "This reservation cannot be cancelled within "
                            + property.getCancellationNoticeDays()
                            + " days before check-in."
            );
        }

        reservation.cancel();

        return reservationRepository.save(reservation);
    }
}
