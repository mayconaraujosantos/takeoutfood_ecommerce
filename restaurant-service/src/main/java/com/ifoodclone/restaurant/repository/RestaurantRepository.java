package com.ifoodclone.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ifoodclone.restaurant.entity.Restaurant;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByActiveTrue();

    Optional<Restaurant> findByIdAndActiveTrue(Long id);

    // :name and :cuisineType are each used twice: once in an "IS NULL" comparison, once inside
    // LOWER(...)/CONCAT(...). Left untyped, Hibernate infers a different JDBC type for each
    // occurrence of the same parameter, and pgjdbc ends up sending the null case as bytea --
    // Postgres then fails resolving LOWER(bytea) before it ever reaches "IS NULL" ("function
    // lower(bytea) does not exist"). CAST(... AS string) pins both occurrences to VARCHAR.
    @Query("SELECT r FROM Restaurant r WHERE r.active = true "
            + "AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) "
            + "AND (:cuisineType IS NULL OR LOWER(r.cuisineType) = LOWER(CAST(:cuisineType AS string)))")
    List<Restaurant> search(@Param("name") String name, @Param("cuisineType") String cuisineType);
}
