package com.villatusia.pms.reservation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByProperty_IdOrderByCheckInDateAscIdAsc(Long propertyId);

    boolean existsByProperty_IdAndStatusAndCheckInDateBeforeAndCheckOutDateAfter(
            Long propertyId,
            ReservationStatus status,
            LocalDate requestedCheckOutDate,
            LocalDate requestedCheckInDate
    );
}
