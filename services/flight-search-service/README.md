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

Aircraft follows the same aggregate repository / application use case / HTTP
adapter structure as Airport, reusing the existing shared error handling.
`AircraftRequest` is reused for create/update and `AircraftView` for all responses.
The JPA entity maps only `id`, `model`, and `total_seats` to `aircrafts`.

| Method | Path | Result |
| --- | --- | --- |
| POST | `/api/v1/aircrafts` | 201, aircraft view and Location header |
| GET | `/api/v1/aircrafts?page=0&size=20` | 200, array ordered by model then ID |
| GET | `/api/v1/aircrafts/{aircraftId}` | 200, aircraft view; 404 if missing |
| PUT | `/api/v1/aircrafts/{aircraftId}` | 200, updated aircraft view |

Create/update body:

```json
{"model":"Airbus A320","totalSeats":180}
```

Model must be nonblank and at most 255 characters; totalSeats must be a positive
integer. Model names are not unique in the documented schema. Updates lock the
aircraft row for the transaction. Capacity changes now validate the maximum number
of assigned seats on any individual Flight using this Aircraft. Reducing below
that count returns 409 with `AIRCRAFT_CAPACITY_BELOW_ASSIGNED_SEATS`. Counts include
all seat statuses and are not summed across different flights. Seat creation
acquires the same Aircraft lock, coordinating it with capacity changes.

`data.sql` also seeds four demo aircraft configurations with stable UUIDs ending
in 001 through 004 under `a2000000-0000-4000-8000-000000000xxx`. It runs after JPA
initializes the tables. `ON CONFLICT (id) DO NOTHING` preserves existing seed rows
and edits across restarts; deleted seed rows are re-created on the next startup.
POST requests always create a new aircraft and are independent of startup seeding.

API Gateway routes aircraft paths to Flight Search using its existing authenticated
policy. No aircraft page or aircraft field exists in the customer frontend, so no
frontend integration or page was added.

Manual verification after restarting Flight Search and Gateway:

1. GET `/api/v1/aircrafts?page=0&size=100` directly at port 8083, or through Gateway
   at port 8080 with a Bearer token. A fresh catalog contains four demo aircraft.
2. Restart Flight Search and GET again: the seed rows and their IDs remain unchanged.
3. POST the example body; expect 201 with an ID and Location header, then GET that ID.
4. PUT that ID with an updated model and totalSeats of 190; expect 200.
5. Reductions that still cover every assigned Flight are allowed. Set capacity
   below the maximum assigned seat count to get 409. A zero/missing capacity or
   blank model returns 400; a valid but missing UUID returns 404.

No Aircraft Java test classes were added; verify these flows manually.

## Flight and Fare API

Flight references Airport by code and Aircraft by UUID. JPA enforces the local
foreign keys `flights.origin_code -> airports.code`,
`flights.destination_code -> airports.code`, `flights.aircraft_id -> aircrafts.id`,
and `fares.flight_id -> flights.id`. No cross-service foreign key is introduced.
Flight code is not unique: the same route number may recur on different dates.

Fare is saved only through the Flight aggregate repository. Each fare update
loads and locks the owning Flight row, preserving all existing fares and
serializing changes through that root. Current domain rules reject overlapping
validity periods for the same fare class; intervals are start-inclusive and
end-exclusive. Cancelled/completed flights cannot receive fare changes, while
restoring them from persistence remains supported.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/v1/flights` | Create a scheduled flight |
| GET | `/api/v1/flights` | Search future active flights with a current fare |
| GET | `/api/v1/flights/{id}` | Flight details, catalog labels, and all fares |
| PUT | `/api/v1/flights/{id}/schedule` | Update departure/arrival through reschedule |
| POST | `/api/v1/flights/{id}/fares` | Add fare through Flight |
| GET | `/api/v1/flights/{id}/fares` | List all fares, including expired/future fares |
| GET | `/api/v1/flights/{id}/fares/{fareId}` | Read a fare owned by this Flight |
| PUT | `/api/v1/flights/{id}/fares/{fareId}` | Update price, currency, and validity |

Example create Flight body (use an existing aircraft ID):

```json
{
  "flightCode": "VN999",
  "originCode": "HAN",
  "destinationCode": "SGN",
  "departureTime": "2026-12-15T08:00:00+07:00",
  "arrivalTime": "2026-12-15T10:10:00+07:00",
  "aircraftId": "a2000000-0000-4000-8000-000000000001"
}
```

Example add Fare body (omit fareClass for PUT, which retains the existing class):

```json
{
  "fareClass": "ECONOMY",
  "basePrice": 1290000,
  "currency": "VND",
  "validFrom": "2026-11-01T00:00:00+07:00",
  "validTo": "2026-12-15T08:00:00+07:00"
}
```

Search parameters are optional `origin`, `destination`, `departureDate` (ISO date,
interpreted in Asia/Ho_Chi_Minh), and `fareClass`, with `page=0&size=20` (max 100).
Search returns an array, ordered by departure time and ID, with only fares valid
at the server's search time and matching the requested class. Details return all
fares. Search accepts `quantity=1` (1-9) and requires both a current fare and enough
available seats in the SAME fare class before pagination. Flight responses include
an `availability` array. Internal service-to-service integration is excluded from these commits.
Gateway allows public flight GETs; write requests require the
existing authentication policy. Role-based write authorization remains pending.

Startup seed adds 14 flights and 42 fares using stable UUIDs. Airport/Aircraft
seeds run first. Flights reference airport codes and aircraft IDs present in the
catalog. Flight departure dates are +7 days for outbound and +13 days for return
from first seeding, in Vietnam local time. Subsequent restarts preserve schedules,
IDs, and edited prices. After those dates pass, choose future seeded/manual flights;
the initializer intentionally does not roll existing schedules forward.

The existing frontend search and detail pages now consume these APIs, including
pagination and loading/error/empty states. Round-trip search displays each leg
separately. Fare class is passed to details, which computes a basic-price estimate
without claiming a seat reservation. No airline/baggage values are invented from
the flight code; the schema has no such catalog fields. Booking confirmation is
disabled until seat reservation and Booking integration are implemented.

Manual verification (no new Java tests were added):

1. Restart Flight Search and Gateway. Read `/api/v1/flights` and choose a seeded
   departure date; the initial HAN-SGN date is +7 days from first Flight seeding.
2. Open `/flights` with HAN-SGN, that date, and ECONOMY. Expect four flights;
   change to BUSINESS/FIRST to see the corresponding seeded prices.
3. Open a card: UUID, route, aircraft, times, class, and price must match the API.
4. For round-trip, choose +13 days as return date and select the return leg.
5. Restart again and compare counts/IDs: 14 seeded flights and 42 fares remain.
6. Using direct port 8083 or authenticated Gateway writes, create a Flight with
   nonexistent airports/aircraft: expect 404. Invalid route/time returns 400.
7. Add/update a Fare through its Flight; negative price, invalid currency,
   invalid dates, and overlapping intervals return 400. A fare from another Flight
   returns 404. Inactive-flight fare changes return 409.

## Seat API

Seat is an independent aggregate referencing Flight by UUID. The persistence
entity maps only the seven documented columns to `seats`. The database enforces
the Flight FK and unique `(flight_id, seat_number)` constraint. Booking owner is
an external UUID reference with no cross-service FK. Public `SeatView` excludes
the booking owner and hold expiry; it contains ID, Flight ID, number, class, status.

| Method | Path | Result |
| --- | --- | --- |
| POST | `/api/v1/flights/{flightId}/seats` | 201, new AVAILABLE seat and Location |
| GET | `/api/v1/flights/{flightId}/seats` | 200, paginated seat array |
| GET | `/api/v1/seats/{seatId}` | 200, seat detail; 404 if missing |

POST body:

```json
{"seatNumber":"12A","seatClass":"ECONOMY"}
```

The server assigns UUID and AVAILABLE status; clients cannot set hold/booking
state through this API. Numbers are normalized to uppercase and validated by
SeatNumber. Listing accepts optional `seatClass`, `page=0`, `size=20` (max 100),
ordered by number then ID. A missing Flight returns 404 rather than an empty list.
Creation locks the Flight and its Aircraft in one transaction before counting
all assigned seats. Duplicate numbers, full capacity, or an inactive/departed
Flight return 409. Different flights may use the same seat number. The unique
constraint also protects duplicate inserts at the database boundary.

Startup seed adds up to 24 demo seats per seeded Flight (336 for 14 fresh flights):
row 1 FIRST, row 2 BUSINESS, rows 3-4 ECONOMY, columns A-F. Seat 4F is BLOCKED;
other sample seats are AVAILABLE. UUIDs are deterministic, duplicate IDs/numbers
are skipped, and no existing statuses/booking fields are overwritten. If aircraft
capacity was reduced or seats were created manually, only remaining capacity is
filled. This is a small catalog sample, not a complete aircraft cabin layout.

The existing flight detail page shows a read-only list for the selected class,
with loading/error/empty states and textual seat statuses. It loads every page
and sorts seat numbers naturally. Viewing seats does not reserve them. Flight
search now filters by passenger count using the Availability projection below.
No internal hold/confirm/release endpoint is included yet.

Manual verification (no new Java tests were added):

1. Restart Flight Search AND Gateway. Open a seeded flight detail: ECONOMY has
   12 demo seats, BUSINESS/FIRST each have 6, unless prior data/capacity limited seed.
2. Inspect seat 4F under ECONOMY: it is shown as blocked. No seat can be selected.
3. Read `/api/v1/flights/{id}/seats?size=100`, restart, and compare IDs/counts:
   24 per fresh seeded Flight, unchanged after restart.
4. POST a new `12A` ECONOMY seat through direct 8083 or authenticated Gateway:
   expect 201, then POST `12a` again to the same Flight: expect 409.
5. Read the Location path `/api/v1/seats/{seatId}`. Missing IDs return 404;
   malformed numbers/classes or invalid pagination return 400.
6. Reduce an Aircraft capacity below the largest assigned-seat count on a Flight:
   expect 409. Setting it exactly to that count is allowed; creating another seat
   on that full Flight returns 409. Restore sample capacity after testing.

## FlightAvailability read model

This is a projection, not an Aggregate Root: no domain repository and no public
write endpoint. JPA maps the four documented columns with composite primary key
`(flight_id, fare_class)` and a local Flight FK. `FlightAvailabilityView` is reused
by Flight results and the read-only endpoints:

| Method | Path | Result |
| --- | --- | --- |
| GET | `/api/v1/flights/{flightId}/availability` | All three fare classes |
| GET | `/api/v1/flights/{flightId}/availability/{fareClass}` | One fare class |

```json
{"flightId":"a3000000-0000-4000-8000-000000000001","fareClass":"ECONOMY","availableSeats":11,"classCapacity":12}
```

`classCapacity` counts all Seat rows for that Flight/class, including HELD,
BOOKED, and BLOCKED. `availableSeats` counts only AVAILABLE rows; it is not based
on Aircraft.totalSeats. Empty classes have zero/zero rows. HELD seats remain
unavailable until the future expiry workflow changes their state.

New Flight creation initializes three class rows in its transaction. Every
`SeatRepositoryAdapter.save` flushes Seat then calls `FlightAvailabilityPort.refresh`
in the same transaction. Refresh requires an existing transaction; failure rolls
back the Seat write. Future reservation workflows must use the existing Flight
lock convention and this adapter so projection writes stay serialized per Flight.

After startup seeding, `data.sql` upserts counts derived from Seat for ALL flights.
It preserves source data and projection keys; counts are reconciled on each
restart. Manual SQL changes to Seat require a restart to reconcile; no background
reconciliation scheduler or database trigger is installed.

Fresh seed produces 42 projection rows: ECONOMY 11/12, BUSINESS 6/6, FIRST 6/6.
Search matches quantity to the same class as the current fare, before pagination.
Batch projection reads populate Flight responses. Frontend passes passenger count,
shows availability on cards, and reads the endpoint on the existing detail page.
Viewing counts is not a reservation; booking confirmation remains disabled.

Manual verification (no new Java tests):

1. Start Docker/PostgreSQL and restart Flight Search. Restart Gateway if it still
   runs the old security configuration that requires authentication for Flight GETs.
2. Read the availability endpoint for a seeded Flight: expect default counts
   above unless Seat data was edited. Restart and verify no duplicate keys.
3. POST a new AVAILABLE ECONOMY Seat: availability increases immediately by one,
   without restart. The same transaction commits Seat and its refreshed counts.
4. Search a seeded route/date with `fareClass=FIRST&quantity=7`: expect no sample
   results. Quantity 6 matches; quantity 0 or 10 returns 400.
5. In frontend details, FIRST and 7 passengers shows an insufficient-seat notice;
   ECONOMY shows 11/12 by default. Counts remain a snapshot, not a guarantee.
6. A new Flight without seats has zero/zero for all classes; it appears in search
   only after adding both a current fare and enough AVAILABLE seats.

## Internal fare quote for Booking Service

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
