package com.vacationtracker.ws.service;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.utils.CommonConstants;
import com.vacationtracker.ws.dao.VacationRequestDAO;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.vacationtracker.utils.CommonConstants.*;
import static com.vacationtracker.utils.Validator.*;

@Service
public class EmployeeService {

    private final VacationRequestDAO vacationRequestDAO;

    public EmployeeService(VacationRequestDAO vacationRequestDAO) {
        this.vacationRequestDAO = vacationRequestDAO;
    }

    /*
       Service for handling employee vacation request
     */

    public List<VacationRequestEntity> getRequests(int employeeId) {
        if(!vacationRequestDAO.getEmployeeIds().contains(employeeId)){
            throw new CustomException(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST);
        }
        return vacationRequestDAO.findByEmployee(employeeId);
    }

    public List<VacationRequestEntity> getRequestsByStatus(int employeeId, Set<Status> statuses) {
        if(!vacationRequestDAO.getEmployeeIds().contains(employeeId)){
            throw new CustomException(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST);
        }
        return vacationRequestDAO.findByEmployee(employeeId).stream()
                .filter(request -> statuses.contains(request.getStatus()))
                .collect(Collectors.toList());
    }

    /*
       Service for handling remaining vacation days
     */

    public int getRemainingVacationDays(int employeeId) {
        if(!vacationRequestDAO.getEmployeeIds().contains(employeeId)){
            throw new CustomException(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST);
        }
        int usedDays = 0;
        List<VacationRequestEntity> requests = vacationRequestDAO.findByEmployee(employeeId);
        if(!requests.isEmpty()){
            for (VacationRequestEntity request : requests) {
                if (request.getStatus() == Status.APPROVED) {
                    LocalDate startDate = request.getVacationStartDate();
                    LocalDate endDate = request.getVacationEndDate();
                    long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
                    usedDays += (int) daysBetween;
                    if(usedDays >= MAX_NUMBER_OF_VACATION_DAYS_PER_YEAR){
                        return 0;
                    }
                }
            }
            return MAX_NUMBER_OF_VACATION_DAYS_PER_YEAR - usedDays;
        }
        return MAX_NUMBER_OF_VACATION_DAYS_PER_YEAR;
    }

    /*
        Service for handling Create Vacation Request
     */
    public VacationRequestEntity createRequest(int employeeId, VacationRequestEntity request) {
        if(!vacationRequestDAO.getEmployeeIds().contains(employeeId)){
            throw new CustomException(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST);
        }
        validateNewVacationRequest(request);

        //Check if start date is after end date
        LocalDate startDate = request.getVacationStartDate();
        LocalDate endDate = request.getVacationEndDate();
        if(startDate.isAfter(endDate)){
            throw new CustomException(CommonConstants.START_DATE_CANNOT_BE_AFTER_END_DATE);
        }
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        //Don't allow to take vacation for more than 1 month
        if(daysBetween >= 30){
            throw new CustomException(CommonConstants.VACATION_DAYS_EXCEEDED_ERROR + MAX_NUMBER_OF_VACATION_DAYS_PER_YEAR);
        }

        List<VacationRequestEntity> requests = vacationRequestDAO.findAll();

        // Handle Duplicate Incoming Request
        for(VacationRequestEntity r : requests){
            if(r.getEmployeeId() == employeeId && r.getVacationStartDate().isEqual(request.getVacationStartDate()) && r.getVacationEndDate().isEqual(request.getVacationEndDate())){
                throw new CustomException(CommonConstants.VACATION_ALREADY_EXISTS);
            }
        }
        int days = endDate.compareTo(startDate) + 1;
        if (getRemainingVacationDays(employeeId) < days) {
            throw new CustomException(VACATION_DAYS_EXCEEDED_ERROR + getRemainingVacationDays(employeeId));
        }
        VacationRequestEntity vacationRequest = setVacationRequestFields(employeeId, request);
        vacationRequestDAO.save(vacationRequest);
        return vacationRequest;
    }

    public VacationRequestEntity deleteRequest(UUID requestId) {
        VacationRequestEntity request = vacationRequestDAO.findById(requestId);
        if (request == null) {
            throw new CustomException(CommonConstants.VACATION_NOT_EXISTS);
        }
        vacationRequestDAO.delete(request);
        return request;
    }

    public VacationRequestEntity setVacationRequestFields(int employeeId, VacationRequestEntity request){
        if(!vacationRequestDAO.getEmployeeIds().contains(employeeId)){
            throw new CustomException(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST);
        }
        VacationRequestEntity vacationRequest = new VacationRequestEntity();
        vacationRequest.setId(UUID.randomUUID());
        vacationRequest.setEmployeeId(employeeId);
        vacationRequest.setRequestCreatedAt(LocalDateTime.now());
        vacationRequest.setResolvedBy(0);
        vacationRequest.setStatus(Status.PENDING);
        vacationRequest.setVacationStartDate(request.getVacationStartDate());
        vacationRequest.setVacationEndDate(request.getVacationEndDate());
        return vacationRequest;
    }

}
