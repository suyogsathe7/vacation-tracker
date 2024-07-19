package com.vacationtracker.utils;

import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.entity.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class DataSource {
    public static void setVacationRequestsMockData(List<VacationRequestEntity> vacationRequests){
        Collections.addAll(vacationRequests,
                new VacationRequestEntity(UUID.randomUUID(), 1, Status.PENDING, LocalDateTime.now(),
                        LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 4)),
                new VacationRequestEntity(UUID.randomUUID(), 2, Status.PENDING, LocalDateTime.now(),
                        LocalDate.of(2024, 3, 2), LocalDate.of(2024, 3, 11)),
                new VacationRequestEntity(UUID.randomUUID(), 3, Status.REJECTED, LocalDateTime.now(),
                        LocalDate.of(2024, 8, 3), LocalDate.of(2024, 8, 12)),
                new VacationRequestEntity(UUID.randomUUID(), 4, Status.APPROVED, LocalDateTime.now(),
                        LocalDate.of(2024, 5, 4), LocalDate.of(2024, 5, 14)),
                new VacationRequestEntity(UUID.randomUUID(), 5, Status.APPROVED, LocalDateTime.now(),
                        LocalDate.of(2024, 5, 4), LocalDate.of(2024, 5, 12)),
                new VacationRequestEntity(UUID.randomUUID(), 6, Status.PENDING, LocalDateTime.now(),
                        LocalDate.of(2024, 7, 8), LocalDate.of(2024, 7, 10)),
                new VacationRequestEntity(UUID.randomUUID(), 7, Status.REJECTED, LocalDateTime.now(),
                        LocalDate.of(2024, 8, 12), LocalDate.of(2024, 8, 23)),
                new VacationRequestEntity(UUID.randomUUID(), 8, Status.PENDING, LocalDateTime.now(),
                        LocalDate.of(2024, 9, 5), LocalDate.of(2024, 9, 20)),
                new VacationRequestEntity(UUID.randomUUID(), 9, Status.APPROVED, LocalDateTime.now(),
                        LocalDate.of(2024, 10, 1), LocalDate.of(2024, 10, 26))
        );
    }

    public static void setEmployeesMockData(Set<Integer> employeeIds) {
        for (int i = 1; i <= 15; i++) {
            employeeIds.add(i);
        }
    }

}
