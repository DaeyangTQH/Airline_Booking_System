package com.group3.flightsearch.infrastructure.adapter;

import com.group3.flightsearch.application.port.out.FlightQueryPort;
import com.group3.flightsearch.application.query.*;
import com.group3.flightsearch.domain.model.FlightStatus;
import com.group3.flightsearch.infrastructure.persistence.entity.*;
import com.group3.flightsearch.infrastructure.persistence.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FlightQueryAdapter implements FlightQueryPort {
    private static final ZoneId SEARCH_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final FlightJpaRepository flights;
    private final FareJpaRepository fares;

    @Override
    public Optional<FlightView> findById(UUID id) {
        return flights.findById(id).map(flight -> view(flight,
                fares.findAllByFlightIdOrderByFareClassAscValidFromAsc(id)));
    }

    @Override
    public List<FlightView> search(FlightSearchQuery search) {
        Specification<FlightJpaEntity> specification = (root, query, cb) -> {
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
            priceQuery.select(fare.get("id")).where(pricePredicates.toArray(Predicate[]::new));
            predicates.add(cb.exists(priceQuery));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var rows = flights.findAll(specification, PageRequest.of(search.page(), search.size(),
                Sort.by("departureTime", "id"))).getContent();
        if (rows.isEmpty()) return List.of();
        var byFlight = fares.findAllByFlightIdInOrderByFareClassAscValidFromAsc(
                rows.stream().map(FlightJpaEntity::getId).toList()).stream()
                .filter(fare -> !fare.getValidFrom().isAfter(search.pricedAt()) && fare.getValidTo().isAfter(search.pricedAt()))
                .filter(fare -> search.fareClass() == null || fare.getFareClass() == search.fareClass())
                .collect(Collectors.groupingBy(FareJpaEntity::getFlightId));
        return rows.stream().map(flight -> view(flight, byFlight.getOrDefault(flight.getId(), List.of()))).toList();
    }

    private FlightView view(FlightJpaEntity flight, List<FareJpaEntity> prices) {
        var fareViews = prices.stream().map(fare -> new FareView(fare.getId(), fare.getFlightId(),
                fare.getFareClass(), fare.getBasePrice(), fare.getCurrency(), fare.getValidFrom(), fare.getValidTo())).toList();
        return new FlightView(flight.getId(), flight.getFlightCode(), flight.getOriginCode(),
                flight.getOrigin().getCity(), flight.getDestinationCode(), flight.getDestination().getCity(),
                flight.getDepartureTime(), flight.getArrivalTime(), flight.getAircraftId(),
                flight.getAircraft().getModel(), flight.getStatus(), fareViews);
    }
}
