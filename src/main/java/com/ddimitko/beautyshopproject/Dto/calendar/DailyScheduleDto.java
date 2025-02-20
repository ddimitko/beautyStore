package com.ddimitko.beautyshopproject.Dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyScheduleDto {

    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

}
