package com.group3.flightsearch.infrastructure.adapter;

import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.port.out.FlightAvailabilityPort;
import com.group3.flightsearch.application.query.*;
import com.group3.flightsearch.domain.model.FlightStatus;
import com.group3.flightsearch.infrastructure.persistence.entity.*;
import com.group3.flightsearch.infrastructure.persistence.repository.*;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FlightQueryAdapter implements FlightQueryPort {
    private static final ZoneId SEARCH_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final FlightJpaRepository flights;
    private final FareJpaRepository fares;
    private final FlightAvailabilityPort availability;

    @Override
    public Optional<FlightView> findById(UUID id) {
        var flight = flights.findById(id);
        if (flight.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(view(flight.get(),
                fares.findAllByFlightIdOrderByFareClassAscValidFromAsc(id), availability.findByFlight(id)));
    }

    @Override
    public List<FlightView> search(FlightSearchQuery search) {
        Specification<FlightJpaEntity> specification = new SearchSpecification(search);
        var rows = flights.findAll(specification, PageRequest.of(search.page(), search.size(),
                Sort.by("departureTime", "id"))).getContent();
        if (rows.isEmpty()) return List.of();

        List<UUID> flightIds = new ArrayList<>();
        for (FlightJpaEntity flight : rows) {
            flightIds.add(flight.getId());
        }
        Map<UUID, List<FlightAvailabilityView>> byAvailability = new HashMap<>();
        for (FlightAvailabilityView capacity : availability.findByFlights(flightIds)) {
            List<FlightAvailabilityView> capacities = byAvailability.get(capacity.flightId());
            if (capacities == null) {
                capacities = new ArrayList<>();
                byAvailability.put(capacity.flightId(), capacities);
            }
            capacities.add(capacity);
        }
        Map<UUID, List<FareJpaEntity>> byFlight = new HashMap<>();
        for (FareJpaEntity fare : fares.findAllByFlightIdInOrderByFareClassAscValidFromAsc(flightIds)) {
            if (fare.getValidFrom().isAfter(search.pricedAt()) || !fare.getValidTo().isAfter(search.pricedAt())) {
                continue;
            }
            if (search.fareClass() != null && fare.getFareClass() != search.fareClass()) {
                continue;
            }
            if (!hasEnoughSeats(fare, byAvailability.getOrDefault(fare.getFlightId(), List.of()), search.quantity())) {
                continue;
            }
            List<FareJpaEntity> flightFares = byFlight.get(fare.getFlightId());
            if (flightFares == null) {
                flightFares = new ArrayList<>();
                byFlight.put(fare.getFlightId(), flightFares);
            }
            flightFares.add(fare);
        }
        List<FlightView> result = new ArrayList<>();
        for (FlightJpaEntity flight : rows) {
            result.add(view(flight, byFlight.getOrDefault(flight.getId(), List.of()),
                    byAvailability.getOrDefault(flight.getId(), List.of())));
        }
        return List.copyOf(result);
    }

    private boolean hasEnoughSeats(FareJpaEntity fare, List<FlightAvailabilityView> capacities, int quantity) {
        for (FlightAvailabilityView capacity : capacities) {
            if (capacity.fareClass() == fare.getFareClass() && capacity.availableSeats() >= quantity) {
                return true;
            }
        }
        return false;
    }

    private static final class SearchSpecification implements Specification<FlightJpaEntity> {
        private final FlightSearchQuery search;

        private SearchSpecification(FlightSearchQuery search) {
            this.search = search;
        }

        @Override
        public Predicate toPredicate(Root<FlightJpaEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            var predicates = new ArrayList<Predicate>();
            predicates.add(root.get("status").in(FlightStatus.SCHEDULED, FlightStatus.DELAYED));
            predicates.add(cb.greaterThan(root.get("departureTime"), search.pricedAt()));
            if (search.originCode() != null) predicates.add(cb.equal(root.get("originCode"), search.originCode()));
            if (search.destinationCode() != null) predicates.add(cb.equal(root.get("destinationCode"), search.destinationCode()));
            if (search.departureDate() != null) {
                var start = search.departureDate().atStartOfDay(SEARCH_ZONE).toInstant();
                var end = search.departureDate().plusDays(1).atStartOfDay(SEARCH_ZONE).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureTime"), start));
                predicates.add(cb.lessThan(root.get("departureTime"), end));
            }
            var priceQuery = query.subquery(UUID.class);
            var fare = priceQuery.from(FareJpaEntity.class);
            var pricePredicates = new ArrayList<Predicate>();
            pricePredicates.add(cb.equal(fare.get("flightId"), root.get("id")));
            pricePredicates.add(cb.lessThanOrEqualTo(fare.get("validFrom"), search.pricedAt()));
            pricePredicates.add(cb.greaterThan(fare.get("validTo"), search.pricedAt()));
            if (search.fareClass() != null) pricePredicates.add(cb.equal(fare.get("fareClass"), search.fareClass()));
            var capacity = priceQuery.from(FlightAvailabilityJpaEntity.class);
            pricePredicates.add(cb.equal(capacity.get("flightId"), fare.get("flightId")));
            pricePredicates.add(cb.equal(capacity.get("fareClass"), fare.get("fareClass")));
            pricePredicates.add(cb.greaterThanOrEqualTo(capacity.get("availableSeats"), search.quantity()));
            priceQuery.select(fare.get("id")).where(pricePredicates.toArray(new Predicate[0]));
            predicates.add(cb.exists(priceQuery));
            return cb.and(predicates.toArray(new Predicate[0]));
        }
    }

    private FlightView view(FlightJpaEntity flight, List<FareJpaEntity> prices,
                            List<FlightAvailabilityView> capacity) {
        List<FareView> fareViews = new ArrayList<>();
        for (FareJpaEntity fare : prices) {
            fareViews.add(new FareView(fare.getId(), fare.getFlightId(), fare.getFareClass(),
                    fare.getBasePrice(), fare.getCurrency(), fare.getValidFrom(), fare.getValidTo()));
        }
        return new FlightView(flight.getId(), flight.getFlightCode(), flight.getOriginCode(),
                flight.getOrigin().getCity(), flight.getDestinationCode(), flight.getDestination().getCity(),
                flight.getDepartureTime(), flight.getArrivalTime(), flight.getAircraftId(),
                flight.getAircraft().getModel(), flight.getStatus(), List.copyOf(fareViews), capacity);
    }
}
