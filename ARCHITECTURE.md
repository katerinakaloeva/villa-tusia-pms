# Villa Tusia PMS — Architecture

## 1. Project Scope and Current Status

Villa Tusia PMS is a backend application for managing a vacation rental property and its reservations.

The first version focuses on:

- Managing property details, pricing, and stay rules.
- Creating and viewing reservations.
- Updating reservation details.
- Cancelling reservations according to the cancellation policy.
- Checking availability and preventing double bookings.

The application is intended for the property manager. The proposed first version is a local demo, accessed through Swagger UI or another API client.

Authentication, Booking.com integration, seasonal pricing, payment processing, reporting, and a frontend are outside the proposed MVP.

### Current status

The project currently implements the property configuration slice:

- Java 21, Spring Boot 4.1.1, Gradle Wrapper 9.7.1, Lombok, and JUnit 6 dependencies.
- A `Property` JPA entity mapped to the PostgreSQL `properties` table.
- Flyway migrations V1 and V2 to create the table and insert Villa Tusia.
- `GET /api/properties/{propertyId}` and full-replacement `PUT /api/properties/{propertyId}` endpoints.
- Request validation, property lookup, and structured HTTP error responses.
- Local PostgreSQL configuration through Docker Compose and `.env.

Reservation behavior, automated application tests, authentication, frontend, and external booking integration are not implemented yet. The remaining sections describe the planned reservation feature and identify decisions that still need agreement.

## 2. Application Structure

The application uses a **layered monolith**: one Spring Boot application connected to one PostgreSQL database.

A typical request follows this path:

```text
API Client
    |
    v
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL
```

### Responsibilities

| Component | Responsibility | Example |
|---|---|---|
| Controller | Receives HTTP requests and returns responses. | Accepts a request to create a reservation. |
| Service | Applies business rules and coordinates the operation. | Checks availability and calculates the total. |
| Repository | Reads and writes database records. | Finds reservations for a property. |
| Entity | Represents stored business data and its valid state. | `Property` or `Reservation`. |
| DTO | Defines the data accepted or returned by the API. | Reservation dates and guest count. |
| Exception handler | Converts errors into consistent HTTP responses. | Returns a booking-conflict error. |

Controllers will not contain pricing or availability logic. Entities will not be returned directly as API responses.

### Package organization

Code will be grouped by feature:

```text
com.villatusia.pms
├── property
├── reservation
└── common
    └── exception
```

Each feature will contain its own controller, service, repository, entity, and DTOs as needed.

This keeps related code together while separating responsibilities.

Service interfaces, generic base classes, and additional patterns will only be introduced when they solve a concrete problem.

### Why this architecture?

Villa Tusia has a small number of closely related features. One application is easier to run, test, and maintain than several independent services.

The MVP does not need microservices, CQRS, Event Sourcing, Kafka, Redis, or Hexagonal Architecture.

## 3. Property and Reservation

### Property

A property represents the entire accommodation being rented.

Its planned fields include:

- ID.
- Name, location, and description.
- Maximum guest capacity.
- Base nightly rate and currency.
- Minimum stay.
- Check-in and check-out times.
- Timezone.
- Cancellation notice period.
- Active status.

Initial settings:

| Setting | Value |
|---|---|
| Property | Villa Tusia |
| Maximum guests | 2 |
| Nightly rate | EUR 100.00 |
| Minimum stay | 1 night |
| Check-in | 15:00 |
| Check-out | 11:00 |
| Timezone | `Europe/Athens` |
| Cancellation notice | 14 days |

### Reservation

A reservation represents one booking of the entire property.

Its planned fields include:

- ID and property reference.
- Main guest details.
- Check-in and check-out dates.
- Guest count.
- Agreed nightly rate.
- Total amount and currency.
- Booking source.
- Status.
- Creation and update timestamps.

The exact guest fields will be agreed before implementation.

### Relationship

```text
One Property → Many Reservations
```

Each reservation belongs to one property. A property can have many reservations over time.

Guests do not need application accounts in the proposed MVP.

### Reservation status

The proposed initial statuses are:

- `CONFIRMED`: the reservation is valid and blocks its dates.
- `CANCELLED`: the reservation remains in the history but no longer blocks availability.

A valid new reservation becomes confirmed immediately. It does not wait for manual acceptance.

A separate `COMPLETED` status will only be added if a feature requires it.

## 4. Business Rules

### Dates and availability

Check-out must be after check-in.

A stay includes the arrival date and excludes the departure date from its occupied nights.

For example:

```text
Check-in:  August 12
Check-out: August 15
Nights:    3
```

Another booking can start on August 15 because the previous guests leave by 11:00 and the next guests arrive from 15:00.

Reservations must respect the applicable guest capacity and minimum stay.

Availability is checked for specific dates. It is not stored as a single available/unavailable flag.

### Pricing

The reservation total is calculated automatically:

```text
Total = Number of nights × Agreed nightly rate
```

Example:

```text
3 nights × EUR 100.00 = EUR 300.00
```

When a reservation is created, the current property rate is saved in that reservation as the agreed nightly rate.

Changing the property’s rate later does not change existing reservations.

When reservation dates change:

1. Check the new dates and availability.
2. Calculate the new number of nights.
3. Keep the original agreed nightly rate.
4. Recalculate the total.
5. Save the changes together.

Money will use `BigDecimal` in Java and a decimal type in PostgreSQL.

Seasonal pricing is deferred.

### Payment

The full reservation amount is due on the check-in date.

This does not mean that the application charges a card or knows that the payment has been collected. Payment processing is outside the MVP.

### Cancellation

The business requirement prohibits cancellation within the final 14 days before arrival.

The proposed exact interpretation is:

```text
Last cancellation date = Check-in date minus 14 days
```

Cancellation is allowed through that date, using the local date in `Europe/Athens`.

For arrival on August 20:

- August 6: cancellation is allowed.
- August 7: cancellation is rejected.

**This exact boundary still requires approval.**

Cancellation changes the status to `CANCELLED`; it does not delete the reservation.

### Changes to existing reservations

A failed modification must leave the previous reservation unchanged.

Changes to the property’s rate or minimum-stay settings must not automatically modify existing reservations.

The following rules remain open:

- Which changes are allowed after check-in.
- Whether cancelled reservations can be edited.
- Which stay rules apply when an older booking is modified.
- How capacity reductions affect existing bookings.
- Whether moving the arrival date changes cancellation eligibility.
- What happens when an already cancelled reservation is cancelled again.

These rules will be agreed before implementing the corresponding operations.

## 5. Validation and Error Handling

The application will validate data at three levels:

| Level | Purpose |
|---|---|
| Request validation | Reject missing or malformed input. |
| Business validation | Enforce reservation rules. |
| Database constraints | Protect stored data, including concurrent writes. |

For example, Bean Validation can reject a missing check-in date. The Service checks whether the requested stay is available.

### Business exceptions

Application-specific business errors will share a common base class:

```text
RuntimeException
└── BusinessException
    ├── PropertyNotFoundException
    ├── ReservationNotFoundException
    ├── ReservationConflictException
    └── CancellationNotAllowedException
```

Each business exception will have a stable error code and a safe, understandable message.

A `GlobalExceptionHandler` will handle errors centrally using `@RestControllerAdvice`.

Framework exceptions, such as request-validation errors, will also be handled centrally. They do not need to inherit from `BusinessException`.

### API responses

The proposed error format is Spring `ProblemDetail` with an additional `code` field.

Example:

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "The property is unavailable for the selected dates.",
  "code": "RESERVATION_DATE_CONFLICT",
  "instance": "/api/reservations"
}
```

Typical HTTP statuses:

| Situation | Status |
|---|---|
| Reservation created | `201 Created` |
| Successful retrieval or update with a response body | `200 OK` |
| Invalid request | `400 Bad Request` |
| Resource not found | `404 Not Found` |
| Conflicting reservation or disallowed operation | `409 Conflict` |
| Unexpected application failure | `500 Internal Server Error` |

Error messages will be written in English. Responses will not expose SQL, credentials, or stack traces.

## 6. Database and Testing

### PostgreSQL and Flyway

PostgreSQL will store properties and reservations.

It provides relationships, transactions, and constraints that support reliable booking data.

Flyway will manage database structure through versioned migration files. Hibernate will map Java entities to that structure.

### Transactions

A transaction groups related database changes into one operation.

For example, changing reservation dates and recalculating the total must either succeed together or leave the original reservation intact.

### Preventing double bookings

Checking availability before saving is necessary, but not sufficient.

Two requests might both find the same dates available before either saves a reservation.

The proposed solution is:

- Check availability in the Service.
- Save within a transaction.
- Add a PostgreSQL exclusion constraint that prevents overlapping confirmed reservations for the same property.

An exclusion constraint is a database rule that rejects conflicting date ranges. Adjacent stays remain allowed, and cancelled reservations do not block dates.

The exact constraint will be explained and approved before implementation.

### Conflicting updates

Updating the same reservation from two requests is a separate problem from double booking.

Optimistic locking with `@Version` is a possible solution. It detects that a reservation changed after it was read, rather than silently overwriting another update.

This remains a proposal. The version field and API behavior will not be added without agreement.

### Local environment

The approved local infrastructure is configured in `compose.yaml`:

```text
Spring Boot -> Connects to PostgreSQL using settings from .env
PostgreSQL  -> postgres:18.6-bookworm through Docker Compose
Host access -> 127.0.0.1:5433 maps to container port 5432
Data        -> Named volume mounted at /var/lib/postgresql
```

The database is `villa_tusia`. The local bootstrap user, password, and host port are supplied through the ignored `.env` file. `.env.example` documents the required variables without a real password.

Flyway manages the current property schema: V1 creates the `properties` table and V2 inserts the initial Villa Tusia row. The application enables Flyway and uses Hibernate schema validation. PostgreSQL must be available when the application starts.

The official image initializes the configured PostgreSQL user as a bootstrap administrator. This is a local development account, not an application login or production access model.

Docker provides a repeatable database environment. It does not make this application a microservices system. The named volume preserves data across container replacement but is not a backup.

Credentials and real guest data must not be committed to the repository. See `README.md` for startup and connection instructions.

### Testing approach

| Test type | What it verifies |
|---|---|
| Unit tests | Night counting, price calculations, cancellation rules. |
| Database integration tests | Queries, migrations, constraints, transactions, concurrent writes. |
| API tests | Requests, validation, HTTP statuses, and error responses. |

Important scenarios include:

- Valid reservation.
- Invalid date range.
- Invalid guest count.
- Overlapping reservations.
- Adjacent reservations.
- Cancelled reservations not blocking availability.
- Preserved nightly rate after a price change.
- Correct total after a date change.
- Cancellation on either side of the deadline.
- Failed changes leaving the original reservation intact.
- Concurrent attempts to book the same dates.

Date-sensitive tests will use a controlled clock.

Testcontainers may be used to run integration tests against a temporary PostgreSQL database. This dependency remains proposed and will be discussed before installation.

Tests will be introduced alongside each feature. A successful build alone does not prove that the reservation rules work.
