package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.ShopImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopImageRepository extends JpaRepository<ShopImage, Long> {

    List<ShopImage> findByShopId(Long shopId);
}
