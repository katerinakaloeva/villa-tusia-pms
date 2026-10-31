package com.villatusia.pms.reservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties/{propertyId}/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<ReservationResponseDto> getByPropertyId(@PathVariable long propertyId) {
        return reservationService.getByPropertyId(propertyId)
                .stream()
                .map(ReservationResponseDto::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> create(
            @PathVariable long propertyId,
            @Valid @RequestBody CreateReservationRequestDto request
    ) {
        Reservation reservation = reservationService.create(
                propertyId,
                request
        );

        ReservationResponseDto response =
                ReservationResponseDto.from(reservation);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{reservationId}/cancel")
    public ReservationResponseDto cancel(
            @PathVariable long propertyId,
            @PathVariable long reservationId
    ) {
        Reservation reservation = reservationService.cancel(
                propertyId,
                reservationId
        );

        return ReservationResponseDto.from(reservation);
    }
}
