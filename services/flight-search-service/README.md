# Flight Search Service

Spring Boot service for flight search and read-only customer flight data.
The frontend has no catalog management screens. Public CUD APIs and their
request DTOs, commands, and application handlers have been removed.

## Public API used by the frontend

| Method | Path | Frontend consumer |
| --- | --- | --- |
| GET | `/api/v1/airports?page=0&size=100` | `airportService.js`, search widget |
| GET | `/api/v1/flights` | `flightService.search`, search results |
| GET | `/api/v1/flights/{flightId}` | `flightService.detail`, booking page |
| GET | `/api/v1/flights/{flightId}/availability` | `flightService.availability`, booking page |
| GET | `/api/v1/flights/{flightId}/seats` | `seatService.js`, seat selection |

Search accepts `origin`, `destination`, `departureDate`, `fareClass`, `quantity`,
`page`, and `size`. Departure dates use Asia/Ho_Chi_Minh. Only future SCHEDULED
or DELAYED flights with an effective fare and enough seats in the requested class
are returned. Public search quantity is 1-9. Pages start at zero and size is 1-100.
Airport results are ordered by code then ID; seats by seat number then ID.
Seat listing accepts optional `seatClass` and pagination.
Flight detail includes fares and availability, so separate public fare endpoints
are unnecessary. Aircraft management, airport/seat detail, and single-class
availability endpoints have also been removed because the frontend does not use them.

## Data and internal integration

The database retains `flights`, `airports`, `aircrafts`, `fares`, `seats`, and
`flight_availability`. Aircraft JPA entities remain necessary for flight relations.
`data.sql` seeds catalog data and reconciles availability at startup. No tables or
existing records are deleted by this cleanup. Catalog maintenance uses SQL or an
external data import; this service exposes no public catalog writes.

Availability counts AVAILABLE seats per class; capacity counts all seats of that
class. HELD, BOOKED, and BLOCKED seats are unavailable. Counts are snapshots and
do not reserve inventory. Internal seat assignment writes refresh availability in
the same transaction and lock the flight to serialize changes.

Booking Service still calls internal fare quotes and seat assignment/release.
These endpoints are required by the existing booking and ticket flows and remain
protected by `X-Internal-Api-Key`. Public read-only scope does not remove these
internal inventory operations.

## Run and verify

Requires Java 21 and Maven. From the repository root:

```sh
mvn -pl services/flight-search-service -am test
mvn -pl services/flight-search-service -am install -DskipTests
mvn -pl services/flight-search-service spring-boot:run
```

Copy `infra/.env.example` to `infra/.env` and configure `FLIGHT_SEARCH_DB_*` and
`INTERNAL_API_KEY`. Start PostgreSQL and Eureka before Flight Search. The default
service port is 8083. API Gateway forwards the public airport list and flight
paths through Eureka. Internal endpoints are called directly by Booking Service.

This module currently has no automated test files. Validate public reads and
internal Booking contracts manually when changing behavior.

## Internal fare quote for Booking Service

Internal HTTP adapters live in `infrastructure/client/rest` and their transport
DTOs in `infrastructure/client/dto`, separately from public API and application
models. `FareQuoteRequest` binds the query parameters; `AssignSeatRequest` binds
the seat assignment body. The controller maps application `FareQuoteView` into
`FareQuoteResponse`, preserving Booking's JSON contract. Application ports and
domain code do not depend on infrastructure DTOs. Release returns 204 without a
response body, so it needs no response DTO.

`GET /internal/v1/flights/{flightId}/fare-quote?fareClass=ECONOMY&quantity=1`
is called directly by Booking's `FlightSearchFeignClient` through Eureka.
Booking's existing `InternalFeignConfiguration` adds `X-Internal-Api-Key`;
no manual Feign header parameter or Gateway route is required.

Flight Search reads `app.internal-api-key` from `INTERNAL_API_KEY`, rejects a blank
configured key at startup, and validates the header before querying a quote.
Missing/incorrect keys return 401 using shared `CustomException`/`ErrorResponse`.
Both services must use the same key. This authenticates possession of the shared
secret, not an exclusive Booking identity; other holders have the same trust.

The response matches Booking's current `FlightFareResponse`:

```json
{"flightId":"a3000000-0000-4000-8000-000000000001","fareClass":"ECONOMY","unitPrice":1200000,"currency":"VND","availableSeats":11,"bookable":true}
```

Quotes select the requested class's fare effective at `[validFrom, validTo)` and
read availability in a repeatable-read transaction. `bookable` requires SCHEDULED
or DELAYED status and a future departure. Booking separately checks availableSeats
against quantity. Any positive quantity is accepted, matching Booking's domain.
The quote does not reserve seats or decrement availability.

Missing flight/current fare returns 404; unsupported Booking currency (outside
VND/USD) returns 409; invalid quantity/parameters return 400. Ambiguous current
fares or a missing availability projection return 500 rather than a false quote.
Booking maps 404 to FlightNotFound and 409 to FlightNotBookable; authentication or
server errors remain communication failures and prevent creation of a Booking.

Manual verification (no new Java tests):

1. Start PostgreSQL/Eureka, Flight Search (8083), and Booking with the same
   `INTERNAL_API_KEY`. Use an existing future flight with a currently valid fare.
2. Call the quote endpoint directly on Flight Search without the header, then
   with an incorrect key: expect 401 in both cases.
3. Call with the configured key: expect 200 and fields matching the current fare
   and availability endpoints. Keep the actual key out of logs/screenshots.
4. With a valid key, test quantity=0 (400), missing flight (404), and quantity
   above availableSeats (200 quote; Booking rejects with insufficient seats).
5. Create a Booking through its public API using a valid user JWT. Verify that
   the Feign request succeeds without manually passing an internal header.
6. Configure different keys for the two services and restart: Booking creation
   must fail without saving a Booking. Restore matching keys afterward.

## Internal seat assignment for Booking Service

These endpoints match the existing `FlightSeatFeignClient` without changing
Booking's flow. Both require the same internal API key as fare quotes.

| Method | Path | Body | Success |
| --- | --- | --- | --- |
| PUT | `/internal/v1/flights/{flightId}/seat-assignments/{ticketId}` | `{"seatId":"<UUID>"}` | 204 |
| DELETE | `/internal/v1/flights/{flightId}/seat-assignments/{ticketId}` | None | 204 |

PUT assigns an AVAILABLE seat as BOOKED to the external ticket reference. Repeat
PUT for the same assignment succeeds without changing inventory, even if the
flight has since departed. Changing seats validates the new seat before releasing
the previous seat, then writes both changes atomically. HELD (including expired
holds awaiting expiry), BLOCKED, and BOOKED seats belonging to someone else are
not available. Existing BOOKED seats with no ticket reference are not reclaimed.
New assignments/changes require a future SCHEDULED/DELAYED flight.

DELETE releases only the requested ticket's assignment. It is idempotent if no
assignment exists; it never releases another ticket's seat. Cleanup is allowed
after departure/cancellation. A ticket already assigned on another flight cannot
be modified using the wrong flight ID.

Seat adds nullable `assigned_ticket_id` with unique constraint
`uk_seats_assigned_ticket`; it is an external ID without a cross-service FK.
Existing rows remain unchanged with a null reference. Restart Flight Search to
apply the column/unique constraint using the current Hibernate ddl-auto=update
configuration. No seed ticket assignments are created. The public Seat response
continues to omit owner IDs.

All assignment writes lock Flight first, using the existing seat mutation lock
convention. Saves flush the old ticket reference before replacement and refresh
availability in the same transaction. A failed replacement rolls back both seats
and availability. The unique constraint also prevents a concurrent assignment of
the same ticket across different flights.

Validation responses: missing/incorrect key 401, malformed UUID/JSON or null/missing
seatId 400, missing Flight/Seat 404, wrong Flight/unavailable Seat/inactive Flight
or duplicate ticket assignment 409. Booking's existing adapter already maps 404
and 409 and checks the user owns an ISSUED ticket before making the Feign call.
This contract carries no bookingId, fareClass or user identity: Flight Search
trusts the authenticated internal caller for ticket existence, ownership, ticket
status and purchased class. It cannot independently verify those facts locally.

Manual verification (no new Java tests):

1. Restart Flight Search and verify the new column and unique constraint exist.
   Choose a future flight, two AVAILABLE seats A/B, and ticket IDs T1/T2.
2. PUT T1/A without a key or with an incorrect key: 401; with valid key: 204.
   A becomes BOOKED and its class availability falls by one. Repeat PUT: 204,
   no further decrease. Restart and verify the assignment persists.
3. PUT T2/A: 409. PUT T1/B: 204; A becomes AVAILABLE, B BOOKED, and availability
   reflects their respective classes. Attempt switching T1 to BLOCKED/HELD/A seat
   owned by T2: 409 and the old assignment stays intact.
4. Test missing/null seatId and malformed IDs (400), unknown Flight/Seat (404),
   a seat or assigned ticket from another Flight (409), inactive/departed flight
   for a new assignment (409). Use otherwise valid authenticated requests.
5. DELETE T1: 204; B becomes AVAILABLE and availability rises by one. Repeat:
   204 with unchanged counts. DELETE T2 must never free a seat owned by T1.
6. Send concurrent PUTs for the same seat by different tickets: only one succeeds.
   Send concurrent PUTs for the same ticket across flights: only one succeeds.
7. Call Booking's existing ticket PUT/DELETE seat APIs with a real ISSUED ticket
   and valid JWT. Verify Booking's seatId and Flight Search's state agree.

Transactions are local to each service. A Booking database failure after a
successful remote call can still leave state different between services; retries
of the same PUT/DELETE are safe, but distributed compensation/reconciliation is
not implemented by this contract.

## Open decisions for later steps

- Add schema migrations.
- Implement atomic seat reservation, confirmation, and expiry.
- Add distributed compensation/reconciliation for remote seat assignments.
