package io.micronaut.test.r2dbc;

import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.annotation.Transactional;

@Transactional(isolation = TransactionDefinition.Isolation.SERIALIZABLE)
public interface PetRepository extends ReactorCrudRepository<Pet, Long> {
    Flux<Pet> list();
}
