package io.micronaut.test.r2dbc;

import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.annotation.Transactional;
import io.r2dbc.spi.Connection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Transactional(isolation = TransactionDefinition.Isolation.SERIALIZABLE)
public interface PetRepository extends ReactorCrudRepository<Pet, Long> {
    Flux<Pet> list();
}
