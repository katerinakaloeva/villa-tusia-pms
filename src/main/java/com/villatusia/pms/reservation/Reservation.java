package com.villatusia.pms.reservation;

import com.villatusia.pms.property.Property;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "guest_name", nullable = false, length = 120)
    private String guestName;

    @Column(name = "guest_email", nullable = false, length = 254)
    private String guestEmail;

    @Column(name = "guest_phone", nullable = false, length = 32)
    private String guestPhone;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "guest_count", nullable = false)
    private int guestCount;

    @Column(name = "agreed_nightly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal agreedNightlyRate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Reservation(
            Property property,
            String guestName,
            String guestEmail,
            String guestPhone,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int guestCount,
            BigDecimal agreedNightlyRate,
            BigDecimal totalAmount,
            String currency
    ) {
        this.property = property;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.guestPhone = guestPhone;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.agreedNightlyRate = agreedNightlyRate;
        this.totalAmount = totalAmount;
        this.currency = currency;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = Instant.now();
    }
}
