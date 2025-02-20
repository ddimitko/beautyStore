package com.ddimitko.beautyshopproject.Dto.calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyScheduleDto {

    private List<DailyScheduleDto> dailySchedules;
}
