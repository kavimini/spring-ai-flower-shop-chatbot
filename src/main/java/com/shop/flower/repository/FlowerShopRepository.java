package com.shop.flower.repository;

import com.shop.flower.model.Flower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlowerShopRepository extends JpaRepository<Flower,Long> {
    Optional<Flower> findByTypeIgnoreCase(String type);
}
