package com.vacationtracker.entity;

import com.vacationtracker.entity.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class VacationRequestEntity {

    private UUID id;
    private int employeeId;
    private Status status;
    private int resolvedBy;
    private LocalDateTime requestCreatedAt;
    private LocalDate vacationStartDate;
    private LocalDate vacationEndDate;

    public VacationRequestEntity(UUID id, int employeeId, Status status, LocalDateTime requestCreatedAt, LocalDate vacationStartDate, LocalDate vacationEndDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.status = status;
        this.requestCreatedAt = requestCreatedAt;
        this.vacationStartDate = vacationStartDate;
        this.vacationEndDate = vacationEndDate;
    }

    public VacationRequestEntity() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(int resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getRequestCreatedAt() {
        return requestCreatedAt;
    }

    public void setRequestCreatedAt(LocalDateTime requestCreatedAt) {
        this.requestCreatedAt = requestCreatedAt;
    }

    public LocalDate getVacationStartDate() {
        return vacationStartDate;
    }

    public void setVacationStartDate(LocalDate vacationStartDate) {
        this.vacationStartDate = vacationStartDate;
    }

    public LocalDate getVacationEndDate() {
        return vacationEndDate;
    }

    public void setVacationEndDate(LocalDate vacationEndDate) {
        this.vacationEndDate = vacationEndDate;
    }
}
