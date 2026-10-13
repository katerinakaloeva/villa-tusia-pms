# Villa Tusia PMS

A Java backend portfolio project for managing a vacation rental property and its bookings.

## Current implementation

- Java 21, Spring Boot 4.1.1, Gradle 9.7.1, and JUnit 6 dependencies.
- PostgreSQL persistence through Spring Data JPA and Flyway migrations.
- Read one property with `GET /api/properties/{propertyId}`.
- Replace the complete property configuration with `PUT /api/properties/{propertyId}`.
- Validate incoming property data and return structured errors for missing properties and invalid requests.
- Create the `properties` table and insert Villa Tusia through Flyway migrations V1 and V2.

Reservation endpoints, authentication, a frontend, seasonal pricing, payments, and Booking.com integration are not implemented yet. Automated application tests have not been added yet.

## Villa Tusia settings

| Setting | Value |
|---|---|
| Maximum guests | 2 |
| Base nightly rate | EUR 100.00 |
| Minimum stay | 1 night |
| Check-in | 15:00 |
| Check-out | 11:00 |
| Time zone | `Europe/Athens` |
| Cancellation notice | 14 days |

The property is rented as one whole accommodation. Reservation date and cancellation rules will be implemented with the reservation feature.

## Requirements

- JDK 21 (a full JDK, not only a JRE)
- Docker Desktop with the Linux container engine and Docker Compose
- Internet access for initial dependency and PostgreSQL image downloads

The included Gradle Wrapper means a global Gradle installation is not required.

## Local configuration

The local `.env` file is ignored by Git. If you need to create it, copy the example once:

```powershell
Copy-Item .env.example .env
```

Edit `.env` and set a non-empty `POSTGRES_PASSWORD`. Keep any existing local values. Never commit `.env`; commit only the placeholder template `.env.example`.

## Start PostgreSQL

From the project root:

```powershell
docker compose up -d --wait postgres
docker compose ps
```

The Compose file uses `postgres:18.6-bookworm`, publishes the database on `127.0.0.1:5433`, and stores its data in the named `postgres_data` volume. PostgreSQL data survives container restarts. The volume is not a backup; do not remove it unless you intend to erase the local database.

## Start the application

With PostgreSQL running and `.env` configured, use Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

On macOS/Linux:

```sh
sh ./gradlew bootRun
```

Flyway applies pending migrations on application startup. The application uses `spring.jpa.hibernate.ddl-auto=validate`, so Hibernate checks entity/table compatibility instead of creating tables.

## API examples

Read the property with ID 1:

```http
GET /api/properties/1
```

Update it by sending the complete editable property representation:

```http
PUT /api/properties/1
Content-Type: application/json
```

```json
{
  "name": "Villa Tusia",
  "location": "Greece",
  "description": "Vacation home with sea view, approximately 500 meters from the sea.",
  "maxGuests": 2,
  "baseNightlyRate": 100.00,
  "currency": "EUR",
  "minimumStayNights": 1,
  "checkInTime": "15:00",
  "checkOutTime": "11:00",
  "timeZone": "Europe/Athens",
  "cancellationNoticeDays": 14,
  "active": true
}
```

A successful request returns a `PropertyResponse`. A missing property returns HTTP 404. Invalid request fields return HTTP 400 with a validation code and field messages.

## Project structure

- `src/main/java/com/villatusia/pms/property`: property entity, repository, service, controller, and request/response DTOs.
- `src/main/java/com/villatusia/pms/common/exception`: shared business exceptions and API exception handling.
- `src/main/resources/db/migration`: versioned Flyway SQL migrations.
- `src/main/resources/application.properties`: Spring Boot and database configuration.
- `compose.yaml`: local PostgreSQL service, port, volume, and healthcheck.
- `.env.example`: local environment template without a real password.
- `ARCHITECTURE.md`: architecture, business rules, and remaining design decisions.

Only synthetic guest data belongs in this project.
