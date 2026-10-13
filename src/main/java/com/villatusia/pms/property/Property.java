package com.villatusia.pms.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

@Getter
@Entity
@Table(name = "properties")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_guests", nullable = false)
    private int maxGuests;

    @Column(
            name = "base_nightly_rate",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal baseNightlyRate;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "minimum_stay_nights", nullable = false)
    private int minimumStayNights;

    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;

    @Column(name = "time_zone", nullable = false, length = 50)
    private String timeZone;

    @Column(name = "cancellation_notice_days", nullable = false)
    private int cancellationNoticeDays;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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

    public void update(
            String name,
            String location,
            String description,
            int maxGuests,
            BigDecimal baseNightlyRate,
            String currency,
            int minimumStayNights,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            String timeZone,
            int cancellationNoticeDays,
            boolean active
    ) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.maxGuests = maxGuests;
        this.baseNightlyRate = baseNightlyRate;
        this.currency = currency;
        this.minimumStayNights = minimumStayNights;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.timeZone = timeZone;
        this.cancellationNoticeDays = cancellationNoticeDays;
        this.active = active;
    }
}
