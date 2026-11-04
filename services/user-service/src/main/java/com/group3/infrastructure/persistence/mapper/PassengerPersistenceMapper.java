package com.group3.infrastructure.persistence.mapper;

import com.group3.domain.aggregate.Passenger;
import com.group3.domain.vo.PassengerId;
import com.group3.domain.vo.UserId;
import com.group3.infrastructure.persistence.entity.PassengerJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PassengerPersistenceMapper {
    public Passenger toDomain(PassengerJpaEntity e) {
        return Passenger.restore(new PassengerId(e.getId()), new UserId(e.getUser().getId()),
                e.getFullName(), e.getPassportNo(), e.getDob(), e.getPhone(), e.getEmail());
    }

    public void copyToEntity(Passenger p, PassengerJpaEntity e) {
        e.setId(p.getId().value());
        e.setFullName(p.getFullName());
        e.setPassportNo(p.getPassportNo());
        e.setDob(p.getDob());
        e.setPhone(p.getPhone());
        e.setEmail(p.getEmail());
    }
}
