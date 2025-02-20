package com.ddimitko.beautyshopproject.entities.calendar;

import com.ddimitko.beautyshopproject.entities.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class AnnualCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private int year;

    @OneToMany(mappedBy = "annualCalendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailySchedule> weeklySchedule = new ArrayList<>();

}
