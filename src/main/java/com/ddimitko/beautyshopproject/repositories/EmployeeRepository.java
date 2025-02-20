package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByShopId(Long shopId);
    Optional<Employee> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

}
