# Villa Tusia PMS — Architecture

## 1. Project Scope and Current Status

Villa Tusia PMS is a backend application for managing a vacation rental property and its reservations.

The first version focuses on:

- Managing property details, pricing, and stay rules.
- Creating and viewing reservations.
- Checking reservation dates, guest capacity, minimum stay, and existing overlaps.

The application is intended for the property manager and is currently a local demo, accessed through Swagger UI or another API client.

Authentication, Booking.com integration, seasonal pricing, payment processing, reporting, and a frontend are not implemented.

### Current status

The project currently implements property configuration and basic reservation creation/listing:

- Java 21, Spring Boot 4.1.1, Gradle Wrapper 9.7.1, Lombok, Mockito, and JUnit 6.
- `Property` and `Reservation` JPA entities mapped to PostgreSQL tables.
- Flyway migrations V1–V5 create and update the property/reservation schema and insert Villa Tusia.
- `GET /api/properties/{propertyId}` and full-replacement `PUT /api/properties/{propertyId}` endpoints.
- `POST /api/properties/{propertyId}/reservations` creates and confirms a reservation immediately.
- `GET /api/properties/{propertyId}/reservations` lists reservations ordered by check-in date.
- Request validation, property lookup, business-rule checks, and structured HTTP error responses.
- Springdoc OpenAPI and Swagger UI for browsing and manually calling endpoints.
- Eleven unit tests in `ReservationServiceTest`; they use Mockito mocks and do not connect to PostgreSQL.
- Local PostgreSQL configuration through Docker Compose and `.env`.

Reservation editing/cancellation, authentication, integration/API tests, frontend, and external booking integration are not implemented. This is a local learning demo and must not be exposed publicly with guest data because the API has no authentication.

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
| Service | Applies business rules and coordinates the operation. | Checks stay length and availability, then calculates the total. |
| Repository | Reads and writes database records. | Finds reservations for a property. |
| Entity | Represents stored business data and its valid state. | `Property` or `Reservation`. |
| DTO | Defines the data accepted or returned by the API. | Reservation dates and guest count. |
| Exception handler | Converts errors into consistent HTTP responses. | Returns a structured booking-conflict error. |

Controllers will not contain pricing or availability logic. Entities will not be returned directly as API responses; controllers map them to response DTOs.

### Package organization

Code is grouped by feature:

```text
com.villatusia.pms
├── property
├── reservation
└── common
    └── exception
```

Each feature contains its own controller, service, repository, entity, and DTOs as needed.

This keeps related code together while separating responsibilities.

Service interfaces, generic base classes, and additional patterns will only be introduced when they solve a concrete problem.

### Why this architecture?

Villa Tusia has a small number of closely related features. One application is easier to run, test, and maintain than several independent services.

The MVP does not need microservices, CQRS, Event Sourcing, Kafka, Redis, or Hexagonal Architecture.

## 3. Property and Reservation

### Property

A property represents the entire accommodation being rented.

Its stored fields are:

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

Its stored fields are:

- ID and property reference.
- Main guest details.
- Check-in and check-out dates.
- Guest count.
- Agreed nightly rate.
- Total amount and currency.
- Status.
- Creation and update timestamps.

The API requires a guest name, email, and phone number. Use synthetic values during development and testing; do not put real guest details in this portfolio project.

### Relationship

```text
One Property → Many Reservations
```

Each reservation belongs to one property. A property can have many reservations over time.

Guests do not need application accounts in the proposed MVP.

### Reservation status

The implemented reservation statuses are:

- `CONFIRMED`: the reservation is valid and blocks its dates.
- `CANCELLED`: the reservation remains in the history but no longer blocks availability.

A valid new reservation becomes confirmed immediately. It does not wait for manual acceptance. The enum contains `CANCELLED`, but there is no cancellation endpoint yet.

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

The reservation service checks that the property is active, the guest count is within capacity, and the stay meets the configured minimum. A shorter stay is rejected before it is saved.

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

If reservation date editing is added, the proposed flow is:

1. Check the new dates and availability.
2. Calculate the new number of nights.
3. Keep the original agreed nightly rate.
4. Recalculate the total.
5. Save the changes together.

Money will use `BigDecimal` in Java and a decimal type in PostgreSQL.

Seasonal pricing is deferred.

### Payment

The application does not record or process payments. Payment requirements need agreement before a payment feature is added.

### Cancellation

The property stores a 14-day cancellation notice setting. Cancellation behavior and the exact deadline boundary are not implemented yet.

When a cancellation endpoint is designed, cancellation should change the status to `CANCELLED` rather than delete the reservation. The allowed deadline boundary still needs to be agreed before implementation.

### Changes to existing reservations

A failed modification must leave the previous reservation unchanged. Reservation editing is not implemented yet.

The creation flow stores the agreed nightly rate on the reservation. Changes to the property’s rate or minimum-stay settings do not automatically modify existing reservations.

The following rules need agreement before reservation editing is added:

- Which changes are allowed after check-in.
- Whether cancelled reservations can be edited.
- Which stay rules apply when an older booking is modified.
- How capacity reductions affect existing bookings.
- Whether moving the arrival date changes cancellation eligibility.
- What happens when an already cancelled reservation is cancelled again.

These rules will be agreed before implementing the corresponding operations.

## 5. Validation and Error Handling

The application validates data at three levels:

| Level | Purpose |
|---|---|
| Request validation | Reject missing or malformed input. |
| Business validation | Enforce reservation rules. |
| Database constraints | Protect stored data and enforce rules at the persistence boundary. |

For example, Bean Validation can reject a missing check-in date. The Service checks whether the requested stay is available.

### Business exceptions

Application-specific business errors share a common base class:

```text
RuntimeException
└── BusinessException
    ├── PropertyNotFoundException
    ├── PropertyInactiveException
    ├── InvalidReservationDatesException
    ├── GuestCountExceedsPropertyLimitException
    ├── MinimumStayNotMetException
    └── ReservationUnavailableException
```

Each business exception has a stable error code and a safe, understandable message.

A `GlobalExceptionHandler` handles errors centrally using `@RestControllerAdvice`.

Framework exceptions, such as request-validation errors, will also be handled centrally. They do not need to inherit from `BusinessException`.

### API responses

The error format is Spring `ProblemDetail` with an additional `code` field.

Example:

```json
{
  "type": "about:blank",
  "title": "Reservation dates unavailable",
  "status": 409,
  "detail": "The property is already reserved for the selected dates.",
  "code": "RESERVATION_DATES_UNAVAILABLE",
  "instance": "/api/properties/1/reservations"
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

Error messages are in English. Responses do not expose SQL, credentials, or stack traces.

## 6. Database and Testing

### PostgreSQL and Flyway

PostgreSQL stores properties and reservations.

It provides relationships, transactions, and constraints that support reliable booking data.

Flyway manages database structure through versioned migration files. Hibernate maps Java entities to that structure.

### Transactions

A transaction groups related database changes into one operation.

For example, changing reservation dates and recalculating the total must either succeed together or leave the original reservation intact.

### Preventing double bookings

Checking availability before saving is necessary, but not sufficient.

Two requests might both find the same dates available before either saves a reservation.

The current implementation:

- Checks availability in the Service.
- Saves the new reservation inside a transaction.

This check alone does not prevent a race if two requests check the same dates at the same time. A PostgreSQL exclusion constraint has been proposed but is not implemented. Until database-level protection is added, concurrent requests may create overlapping confirmed reservations. Adjacent stays are allowed by the current overlap query, and only confirmed reservations block dates.

The constraint design should be explained and agreed before implementation.

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

Flyway manages the current schema: V1 creates the `properties` table, V2 inserts Villa Tusia, V3 creates the `reservations` table, V4 changes the cancellation notice column type, and V5 changes the small-integer columns to integers to match the Java entity types. Applied migrations must remain unchanged because Flyway checks their recorded checksums. The application enables Flyway and uses Hibernate schema validation. PostgreSQL must be available when the application starts.

The official image initializes the configured PostgreSQL user as a bootstrap administrator. This is a local development account, not an application login or production access model.

Docker provides a repeatable database environment. It does not make this application a microservices system. The named volume preserves data across container replacement but is not a backup.

Credentials and real guest data must not be committed to the repository. See `README.md` for startup and connection instructions.

### Testing approach

| Test type | What it verifies |
|---|---|
| Unit tests (implemented) | Reservation service rules using Mockito mocks; they do not connect to PostgreSQL. |
| Database integration tests (future) | Queries, migrations, constraints, transactions, and concurrent writes against PostgreSQL. |
| API tests (future) | Requests, validation, HTTP statuses, and error responses through the application. |

The current `ReservationServiceTest` contains eleven unit tests for:

- Reading a property's reservations, returning an empty list, and rejecting a missing property.
- Calculating the total from the nightly rate and number of nights.
- Rejecting invalid dates, unavailable dates, guest counts above capacity, and inactive or missing properties.
- Rejecting stays below the configured minimum and accepting a stay exactly at the minimum.

Still-important future scenarios include adjacent stays, cancelled reservations not blocking availability, cancellation deadlines, preservation of the agreed nightly rate after a property price change, reservation edits, and concurrent booking attempts.

Date-sensitive tests will use a controlled clock.

Testcontainers may be used to run integration tests against a temporary PostgreSQL database. This dependency remains proposed and will be discussed before installation.

Tests will be introduced alongside each feature. A successful build alone does not prove that the reservation rules work.
