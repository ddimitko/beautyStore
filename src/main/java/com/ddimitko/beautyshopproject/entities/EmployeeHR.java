package com.ddimitko.beautyshopproject.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class EmployeeHR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    //Personal
    private LocalDate hireDate;
    private double salary;
    private int dateOfBirth;
    private String gender;
    private String contactPhone;
    private String email;
    private String homeAddress;

    //Annual Leave
    private int totalLeaveDays;
    private int usedLeaveDays;


}
