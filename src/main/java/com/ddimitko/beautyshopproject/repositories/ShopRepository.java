package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    @Query("SELECT s FROM Shop s JOIN FETCH s.employees WHERE s.id = :shopId")
    Shop findByIdWithEmployees(Long shopId);

    List<Shop> findByNameContainingIgnoreCase(String name);

}
