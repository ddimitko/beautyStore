package com.ddimitko.beautyshopproject.repositories;

import com.ddimitko.beautyshopproject.nomenclatures.Roles;
import com.ddimitko.beautyshopproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    //boolean existsByUserId(Long userId);
    List<User> findAllByRole(Roles role);
    Optional<User> findByIdAndRole(Long id, Roles role);

}
