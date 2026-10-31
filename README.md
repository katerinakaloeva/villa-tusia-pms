# Villa Tusia PMS

A beginner-friendly Java REST API for managing the Villa Tusia vacation rental and its reservations. It is a single Spring Boot application backed by PostgreSQL.

## Current features

- Read and update the property's settings.
- Create a confirmed reservation for the whole property, without manual approval.
- List a property's reservations, ordered by check-in date.
- Cancel confirmed reservations before the configured cancellation deadline.
- Check that the property is active, the dates are valid and available, guest count is within capacity, and the stay meets the property's minimum length.
- Save the agreed nightly rate and total in each reservation. For example, three nights at EUR 100.00 total EUR 300.00.
- Return structured errors for invalid requests and business-rule failures.
- Apply database changes with Flyway migrations V1–V5 and validate the entity/table mapping with Hibernate.
- Explore and call the endpoints through Swagger UI.

The API supports reading, creating, and cancelling reservations. Editing reservations is not implemented. Authentication is not implemented; run this as a local demo with synthetic guest data, and do not expose it publicly or use real guest details.

## Villa Tusia settings

| Setting | Value |
|---|---|
| Rental | The whole property |
| Maximum guests | 2 |
| Base nightly rate | EUR 100.00 |
| Minimum stay | 1 night |
| Check-in | 15:00 |
| Check-out | 11:00 |
| Time zone | `Europe/Athens` |
| Cancellation notice setting | 14 days |

The cancellation notice is stored as a property setting. Cancellation is allowed only before the local date that is the configured number of days before check-in. On that cutoff date and afterward, cancellation is rejected and the reservation remains confirmed.

## Requirements

- JDK 21 (a full JDK, not only a JRE)
- Docker Desktop with the Linux container engine and Docker Compose
- Internet access the first time Gradle dependencies or the PostgreSQL image are needed

The included Gradle Wrapper means a global Gradle installation is not required.

## Local configuration

The local `.env` file is ignored by Git. If you need to create it, copy the example once:

```powershell
Copy-Item .env.example .env
```

Set a non-empty `POSTGRES_PASSWORD` in `.env`. Keep local credentials in `.env`; never commit that file. Commit only the placeholder template `.env.example`.

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
./gradlew bootRun
```

Flyway applies pending migrations on application startup. The application uses `spring.jpa.hibernate.ddl-auto=validate`, so Hibernate checks entity/table compatibility instead of creating tables.

## Swagger UI

With the application running, open [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html). Expand an endpoint, choose **Try it out**, enter the path parameter (for example, `propertyId` = `1`) and choose **Execute**. The page describes the actual API and lets you inspect response codes and JSON.

## API endpoints

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/api/properties/{propertyId}` | Read property settings. |
| `PUT` | `/api/properties/{propertyId}` | Replace the editable property settings; send the complete request body. |
| `GET` | `/api/properties/{propertyId}/reservations` | List that property's reservations. An existing property with no reservations returns `[]`. |
| `POST` | `/api/properties/{propertyId}/reservations` | Create and immediately confirm a reservation. |
| `POST` | `/api/properties/{propertyId}/reservations/{reservationId}/cancel` | Cancel a reservation before the cancellation deadline. |

Example request to create a reservation (use synthetic details and dates that are free in your local database):

```http
POST /api/properties/1/reservations
Content-Type: application/json
```

```json
{
  "guestName": "Test Guest",
  "guestEmail": "test@example.com",
  "guestPhone": "0000000000",
  "checkInDate": "2027-07-20",
  "checkOutDate": "2027-07-23",
  "guestCount": 2
}
```

The service calculates the number of nights from the dates. It reads the nightly rate from the property, saves that agreed rate on the reservation, and calculates the total. With the current base rate, three nights total EUR 300.00. An overlapping confirmed reservation or a stay shorter than the property's minimum is rejected.

Successful reads, property updates, and cancellations return `200 OK`; creating a reservation returns `201 Created`. Invalid input and a stay below the minimum return `400 Bad Request`. A missing property or reservation returns `404 Not Found`. Unavailable dates, an inactive property, a closed cancellation window, or an already cancelled reservation return `409 Conflict`.

## Tests

`ReservationServiceTest` contains 14 unit tests. They use Mockito mocks, so they test service rules without connecting to PostgreSQL. Run the test suite from Windows PowerShell with:

```powershell
.\gradlew.bat test
```

On macOS/Linux:

```sh
./gradlew test
```

Manual Swagger requests use the real local application and PostgreSQL database; they are separate from these unit tests.

## Project structure

- `src/main/java/com/villatusia/pms/property`: property entity, repository, service, controller, and request/response DTOs.
- `src/main/java/com/villatusia/pms/reservation`: reservation entity, repository, service, controller, DTOs, and status.
- `src/main/java/com/villatusia/pms/common/exception`: shared business exceptions and API exception handling.
- `src/main/resources/db/migration`: versioned Flyway SQL migrations.
- `src/main/resources/application.properties`: Spring Boot and environment-based database configuration.
- `compose.yaml`: local PostgreSQL service, port, volume, and health check.
- `.env.example`: local environment template without a real password.
- `ARCHITECTURE.md`: implemented architecture, business rules, and remaining design decisions.

Only synthetic guest data belongs in this project.
