package example.micronaut;

import example.micronaut.entities.Product;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

@Repository
interface ProductRepository extends JpaRepository<Product, Long> {
}
