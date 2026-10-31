package com.villatusia.pms.reservation;

import com.villatusia.pms.common.exception.*;
import com.villatusia.pms.property.Property;
import com.villatusia.pms.property.PropertyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PropertyService propertyService;

    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void shouldReturnReservationsForProperty() {
        long propertyId = 1L;
        Property property = mock(Property.class);
        Reservation firstReservation = mock(Reservation.class);
        Reservation secondReservation = mock(Reservation.class);
        List<Reservation> reservations = List.of(firstReservation, secondReservation);

        when(propertyService.getById(propertyId)).thenReturn(property);
        when(reservationRepository.findByProperty_IdOrderByCheckInDateAscIdAsc(propertyId))
                .thenReturn(reservations);

        List<Reservation> result = reservationService.getByPropertyId(propertyId);

        assertEquals(reservations, result);
        verify(reservationRepository).findByProperty_IdOrderByCheckInDateAscIdAsc(propertyId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldReturnEmptyListWhenPropertyHasNoReservations() {
        long propertyId = 1L;
        Property property = mock(Property.class);

        when(propertyService.getById(propertyId)).thenReturn(property);
        when(reservationRepository.findByProperty_IdOrderByCheckInDateAscIdAsc(propertyId))
                .thenReturn(List.of());

        List<Reservation> result = reservationService.getByPropertyId(propertyId);

        assertEquals(List.of(), result);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldRejectListingReservationsWhenPropertyDoesNotExist() {
        long propertyId = 999L;

        when(propertyService.getById(propertyId))
                .thenThrow(new PropertyNotFoundException(propertyId));

        assertThrows(
                PropertyNotFoundException.class,
                () -> reservationService.getByPropertyId(propertyId)
        );

        verifyNoInteractions(reservationRepository);
    }



    @Test
    void shouldCalculateTotalAmountForThreeNights() {
        // Arrange
        long propertyId = 1L;

        LocalDate checkInDate = LocalDate.of(2027, 8, 12);
        LocalDate checkOutDate = LocalDate.of(2027, 8, 15);

        Property property = mock(Property.class);

        when(property.isActive()).thenReturn(true);
        when(property.getMaxGuests()).thenReturn(2);
        when(property.getMinimumStayNights()).thenReturn(1);
        when(property.getBaseNightlyRate())
                .thenReturn(new BigDecimal("100.00"));
        when(property.getCurrency()).thenReturn("EUR");

        when(propertyService.getById(propertyId))
                .thenReturn(property);

        when(reservationRepository
                .existsByProperty_IdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                        propertyId,
                        ReservationStatus.CONFIRMED,
                        checkOutDate,
                        checkInDate
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateReservationRequestDto request =
                new CreateReservationRequestDto(
                        "Test Guest",
                        "guest@example.com",
                        "TEST-PHONE",
                        checkInDate,
                        checkOutDate,
                        2
                );

         Reservation result = reservationService.create(propertyId, request);

         assertEquals(
                new BigDecimal("300.00"),
                result.getTotalAmount()
        );
        assertEquals(new BigDecimal("100.00"), result.getAgreedNightlyRate());
        assertEquals("EUR", result.getCurrency());

        verify(reservationRepository).save(result);
    }

    @Test
    void shouldRejectCheckOutBeforeCheckIn() {
         long propertyId = 1L;

        Property property = mock(Property.class);

        when(property.isActive()).thenReturn(true);
        when(propertyService.getById(propertyId))
                .thenReturn(property);

        CreateReservationRequestDto request =
                new CreateReservationRequestDto(
                        "Test Guest",
                        "guest@example.com",
                        "TEST-PHONE",
                        LocalDate.of(2027, 8, 15),
                        LocalDate.of(2027, 8, 12),
                        2
                );

         assertThrows(
                InvalidReservationDatesException.class,
                () -> reservationService.create(propertyId, request)
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationWhenDatesAreUnavailable() {
         long propertyId = 1L;
        LocalDate checkInDate = LocalDate.of(2027, 8, 12);
        LocalDate checkOutDate = LocalDate.of(2027, 8, 15);

        Property property = mock(Property.class);

        when(property.isActive()).thenReturn(true);
        when(property.getMaxGuests()).thenReturn(2);
        when(property.getMinimumStayNights()).thenReturn(1);
        when(propertyService.getById(propertyId))
                .thenReturn(property);

        when(reservationRepository
                .existsByProperty_IdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                        propertyId,
                        ReservationStatus.CONFIRMED,
                        checkOutDate,
                        checkInDate
                ))
                .thenReturn(true);

        CreateReservationRequestDto request =
                new CreateReservationRequestDto(
                        "Test Guest",
                        "guest@example.com",
                        "TEST-PHONE",
                        checkInDate,
                        checkOutDate,
                        2
                );

         assertThrows(
                ReservationUnavailableException.class,
                () -> reservationService.create(propertyId, request)
        );

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationShorterThanMinimumStay() {
        long propertyId = 1L;
        Property property = mock(Property.class);

        when(property.isActive()).thenReturn(true);
        when(property.getMaxGuests()).thenReturn(2);
        when(property.getMinimumStayNights()).thenReturn(3);
        when(propertyService.getById(propertyId)).thenReturn(property);

        CreateReservationRequestDto request = new CreateReservationRequestDto(
                "Test Guest",
                "guest@example.com",
                "TEST-PHONE",
                LocalDate.of(2027, 8, 12),
                LocalDate.of(2027, 8, 14),
                2
        );

        MinimumStayNotMetException exception = assertThrows(
                MinimumStayNotMetException.class,
                () -> reservationService.create(propertyId, request)
        );

        assertEquals("MINIMUM_STAY_NOT_MET", exception.getCode());
        assertEquals(
                "Requested stay of 2 nights is shorter than the property's minimum of 3 nights.",
                exception.getMessage()
        );
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldAcceptReservationExactlyAtMinimumStay() {
        long propertyId = 1L;
        LocalDate checkInDate = LocalDate.of(2027, 8, 12);
        LocalDate checkOutDate = LocalDate.of(2027, 8, 15);
        Property property = mock(Property.class);

        when(property.isActive()).thenReturn(true);
        when(property.getMaxGuests()).thenReturn(2);
        when(property.getMinimumStayNights()).thenReturn(3);
        when(property.getBaseNightlyRate()).thenReturn(new BigDecimal("100.00"));
        when(property.getCurrency()).thenReturn("EUR");
        when(propertyService.getById(propertyId)).thenReturn(property);

        when(reservationRepository
                .existsByProperty_IdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
                        propertyId,
                        ReservationStatus.CONFIRMED,
                        checkOutDate,
                        checkInDate
                ))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateReservationRequestDto request = new CreateReservationRequestDto(
                "Test Guest",
                "guest@example.com",
                "TEST-PHONE",
                checkInDate,
                checkOutDate,
                2
        );

        Reservation result = reservationService.create(propertyId, request);

        assertEquals(checkInDate, result.getCheckInDate());
        assertEquals(checkOutDate, result.getCheckOutDate());
        assertEquals(new BigDecimal("300.00"), result.getTotalAmount());
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        verify(reservationRepository).save(result);
    }

    @Test
    void shouldRejectReservationWhenGuestCountExceedsPropertyLimit() {
        long propertyId = 1L;
        LocalDate checkInDate = LocalDate.of(2027, 8, 12);
        LocalDate checkOutDate = LocalDate.of(2027, 8, 15);

        Property property = mock(Property.class);

        when(property.isActive()).thenReturn(true);
        when(property.getMaxGuests()).thenReturn(2);
        when(propertyService.getById(propertyId)).thenReturn(property);

        CreateReservationRequestDto request = new CreateReservationRequestDto(
                "Test Guest",
                "guest@example.com",
                "TEST-PHONE",
                checkInDate,
                checkOutDate,
                3
        );

        assertThrows(
                GuestCountExceedsPropertyLimitException.class,
                () -> reservationService.create(propertyId, request)
        );

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationWhenPropertyIsInactive() {
        long propertyId = 1L;
        Property property = mock(Property.class);

        when(property.isActive()).thenReturn(false);
        when(propertyService.getById(propertyId)).thenReturn(property);

        CreateReservationRequestDto request = new CreateReservationRequestDto(
                "Test Guest",
                "guest@example.com",
                "TEST-PHONE",
                LocalDate.of(2027, 8, 12),
                LocalDate.of(2027, 8, 15),
                2
        );

        assertThrows(
                PropertyInactiveException.class,
                () -> reservationService.create(propertyId, request)
        );

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldRejectReservationWhenPropertyDoesNotExist() {
        long propertyId = 1L;

        when(propertyService.getById(propertyId))
                .thenThrow(new PropertyNotFoundException(propertyId));

        CreateReservationRequestDto request = new CreateReservationRequestDto(
                "Test Guest",
                "guest@example.com",
                "TEST-PHONE",
                LocalDate.of(2027, 8, 12),
                LocalDate.of(2027, 8, 15),
                2
        );

        assertThrows(
                PropertyNotFoundException.class,
                () -> reservationService.create(propertyId, request)
        );

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldCancelReservationMoreThanFourteenDaysBeforeCheckIn() {
        when(clock.instant()).thenReturn(Instant.parse("2027-01-01T12:00:00Z"));
        Reservation reservation = prepareCancellationReservation(
                LocalDate.of(2027, 1, 16)
        );
        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.cancel(1L, 10L);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void shouldRejectCancellationExactlyFourteenDaysBeforeCheckIn() {
        when(clock.instant()).thenReturn(Instant.parse("2027-01-01T12:00:00Z"));
        Reservation reservation = prepareCancellationReservation(
                LocalDate.of(2027, 1, 15)
        );

        ReservationCancellationException exception = assertThrows(
                ReservationCancellationException.class,
                () -> reservationService.cancel(1L, 10L)
        );

        assertEquals("CANCELLATION_WINDOW_CLOSED", exception.getCode());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldRejectCancellationFourteenHoursBeforeCheckIn() {
        // 23:00 UTC is 01:00 the next day in Europe/Athens during winter.
        when(clock.instant()).thenReturn(Instant.parse("2027-01-01T23:00:00Z"));
        Reservation reservation = prepareCancellationReservation(
                LocalDate.of(2027, 1, 2)
        );

        assertThrows(
                ReservationCancellationException.class,
                () -> reservationService.cancel(1L, 10L)
        );

        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    private Reservation prepareCancellationReservation(LocalDate checkInDate) {
        Property property = mock(Property.class);
        when(property.getId()).thenReturn(1L);
        when(property.getTimeZone()).thenReturn("Europe/Athens");
        when(property.getCancellationNoticeDays()).thenReturn(14);
        when(propertyService.getById(1L)).thenReturn(property);

        Reservation reservation = new Reservation(
                property,
                "Test Guest",
                "guest@example.com",
                "0000000000",
                checkInDate,
                checkInDate.plusDays(2),
                2,
                new BigDecimal("100.00"),
                new BigDecimal("200.00"),
                "EUR"
        );
        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        return reservation;
    }
}
