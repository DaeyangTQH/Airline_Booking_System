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

The default port is `8085`. Both Booking and Flight Search import the ignored repository-root `.env` as a properties file when run from their Maven module directories, so Maven does not need manually exported environment variables. The root `.env` was copied from `infra/.env`; copy it again if you change values in the infrastructure file. The service includes JPA and PostgreSQL dependencies, but has no domain persistence mappings or HTTP endpoints yet. The startup test excludes datasource auto-configuration so it does not require a live database.

## Open decisions for later steps

- Select the PostgreSQL schema and migrations for the six documented tables.
- Select a locking strategy for atomic multi-seat holds and define the hold expiration policy.
- Define fare validity overlap policy and how availability is reconciled with seat state.
- Define internal API authentication and the price snapshot contract with Booking Service.
