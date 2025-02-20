package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service,Integer> {
    List<Service> findByEmployeeId(Long employeeId);

}
