# Flight Search Service

Spring Boot service scaffold for the Flight Search bounded context. This module follows [the implementation guide](../../docs/Flight_Search_DDD_Commit_Guide_Hexagonal.html), starting with its initialization step.

## Ownership and domain language

Flight Search owns six planned tables: `flights`, `airports`, `aircrafts`, `fares`, `seats`, and `flight_availability`. Booking Service owns bookings and passenger selection. The two services communicate through an internal API; neither writes to the other's database.

| Concept | Role |
| --- | --- |
| Flight | Aggregate root for schedule, status, and fares |
| Airport | Aggregate root for airport catalog data |
| Aircraft | Aggregate root for model and capacity |
| Fare | Child entity of Flight; changed through Flight |
| Seat | Aggregate root for hold, booking, and block transitions |
| FlightAvailability | Read model; seat status remains the source of truth |

The intended package dependency direction is `api -> application -> domain`, with `infrastructure` implementing the domain repositories and application output ports. The domain now contains the four aggregate roots, the Fare child entity, value objects, enums, and domain exceptions. JPA entities and adapters will remain separate from these domain classes.

Current domain decisions: fares of the same class on one flight cannot have overlapping validity periods; a period's end is exclusive. A seat hold expires at its expiry instant, and a booked seat is not released by the hold release operation. Airport-code uniqueness, seat-number uniqueness within a flight, airport existence, and assigned-seat counts require repository or application checks when those layers are added.

## Run and verify

Requires Java 21 and Maven. From the repository root:

```sh
mvn -pl services/flight-search-service -am test
mvn -pl services/flight-search-service -am install -DskipTests
mvn -pl services/flight-search-service spring-boot:run
```

Copy `infra/.env.example` to `infra/.env` and configure the `FLIGHT_SEARCH_DB_*` variables. `FLIGHT_SEARCH_SERVICE_PORT=8083` sets the service port. Both Booking and Flight Search import `infra/.env` as a properties file when run from the repository root or their Maven module directories, so Maven does not need manually exported environment variables. Start PostgreSQL with `docker compose --env-file infra/.env -f infra/docker-compose.yml up -d`; on first initialization, it creates `flight_search_db`. For an existing PostgreSQL volume, create that database manually if it is missing, since initialization scripts only run on an empty data directory. Start Eureka before the service to enable discovery. API Gateway routes `/api/v1/flights/**` to `flight-search-service` through Eureka. The service includes JPA and PostgreSQL dependencies and implements the Airport catalog endpoints described below.

## Airport API

On startup, Hibernate initializes the schema before Spring runs UTF-8 `data.sql`.
The script seeds ten airport codes matching the frontend catalog: HAN, SGN, DAD,
CXR, PQC, DLI, HUI, VDO, BKK, SIN. `ON CONFLICT (code) DO NOTHING` inserts only
missing codes; restarts do not duplicate airports or overwrite edited records.
The script is checked at each startup, but each airport is inserted only once
while its code remains in the database. Deleting a seed airport allows it to be
inserted again on the next startup.

Airport is the first implemented catalog aggregate. Its HTTP adapter calls
`AirportUseCase`, implemented by a transactional `AirportService`. The service uses
the domain `AirportRepository`; the JPA adapter owns persistence mapping. Simple
catalog reads reuse that repository without a duplicate query port. Responses reuse
`AirportView`; errors reuse `common`'s `CustomException`, `ErrorType`, and
`ErrorResponse`. The JPA entity has exactly the five documented airport columns;
it does not extend `BaseEntity`, which would add audit columns outside this schema.
The existing `ddl-auto: update` creates the airport table in the configured database.

| Method | Path | Result |
| --- | --- | --- |
| POST | `/api/v1/airports` | 201, airport view and Location header |
| GET | `/api/v1/airports/{airportId}` | 200, airport view; 404 if missing |
| GET | `/api/v1/airports?page=0&size=20` | 200, array ordered by code then ID |
| PUT | `/api/v1/airports/{airportId}` | 200, updated airport view |

Pages start at zero; size is between 1 and 100. Empty pages return an empty array.
POST body:

```json
{"code":"HAN","name":"Noi Bai","city":"Hanoi","country":"Vietnam"}
```

PUT accepts `name`, `city`, and `country`; the airport ID and code remain unchanged.
Codes are normalized to uppercase; invalid codes/fields return 400 and duplicate
codes return 409, including a violation of the named database unique constraint.
API Gateway forwards airport paths to Flight Search through Eureka. Its existing
authentication policy applies to writes; airport GET requests are public for the customer search widget. Role-based catalog write authorization is not yet
implemented. Direct service access has no HTTP authentication configured.
Delete and seat hold/confirm/release operations are not implemented.

Tests cover creation, normalization, duplicate codes, updates, missing records,
pagination validation, HTTP validation/errors, persistence mapping, and translation
of the unique constraint violation. These tests do not require a live database.

## Aircraft API

POST/GET /api/v1/aircrafts and GET/PUT /api/v1/aircrafts/{id} use AircraftUseCase,
AircraftService, the domain repository, and JPA adapter. Request: model and
totalSeats; response reuses AircraftView. Capacity must be positive. Reductions
are deferred until Seat persistence can validate assigned counts. Four sample
configurations use stable IDs; restarts preserve existing records and edits.

## Flight and Fare API

POST/GET /api/v1/flights, GET /api/v1/flights/{id}, and PUT /api/v1/flights/{id}/schedule.
Manage fares under /api/v1/flights/{id}/fares; Fare writes go through the locked
Flight aggregate. Airport codes and Aircraft UUIDs are checked and protected by
local foreign keys. Current-fare search supports route, Vietnam-local departure
date, class, and pagination. Seat filtering is introduced with Availability.
Seed adds 14 flights and 42 fares with stable keys, +7-day outbound and +13-day
return schedules relative to first seeding. Existing schedules are preserved.
Frontend search and details use real API UUIDs and fare data; booking stays disabled.
