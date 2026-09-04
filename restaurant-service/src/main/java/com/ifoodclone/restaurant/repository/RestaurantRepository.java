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

    @Query("SELECT r FROM Restaurant r WHERE r.active = true "
            + "AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:cuisineType IS NULL OR LOWER(r.cuisineType) = LOWER(:cuisineType))")
    List<Restaurant> search(@Param("name") String name, @Param("cuisineType") String cuisineType);
}
