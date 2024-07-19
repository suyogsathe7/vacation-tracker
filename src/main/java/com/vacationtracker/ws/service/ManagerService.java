package com.vacationtracker.ws.service;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.utils.CommonConstants;
import com.vacationtracker.ws.dao.VacationRequestDAO;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vacationtracker.utils.Validator.validateVacationRequestToBeProcessed;

@Service
public class ManagerService {

    VacationRequestDAO vacationRequestDAO;

    public ManagerService(VacationRequestDAO vacationRequestDAO) {
        this.vacationRequestDAO = vacationRequestDAO;
    }

    /*
       Service for handling all vacation requests
     */

    public List<VacationRequestEntity> getAllVacationRequests() {
        return vacationRequestDAO.findAll();
    }

    public List<VacationRequestEntity> getAllVacationRequestsWithStatuses(Set<Status> statuses) {
        return vacationRequestDAO.findAll()
                .stream()
                .filter(request -> statuses.contains(request.getStatus()))
                .collect(Collectors.toList());
    }

    /*
       Service for handling vacation request by employee
     */

    public List<VacationRequestEntity> getVacationRequestsByEmployee(int employeeId) {
        if(!vacationRequestDAO.getEmployeeIds().contains(employeeId)){
            throw new CustomException(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST);
        }
        return vacationRequestDAO.findAll().stream()
                .filter(request -> request.getEmployeeId() == employeeId)
                .collect(Collectors.toList());
    }

     /*
       Service for handling overlapping vacation requests
     */

    public List<VacationRequestEntity> getAllOverlappingRequests() {

        //Do not check for REJECTED requests
        List<VacationRequestEntity> vacationRequests = vacationRequestDAO.findAllByMultipleStatuses(Set.of(Status.APPROVED, Status.PENDING));

        return vacationRequests.stream().filter(req1 -> {
            for (VacationRequestEntity req2 : vacationRequests) {
                    if (areOverlappingRequests(req1, req2)) {
                        return true;
                    }
            }
            return false;
        }).collect(Collectors.toList());
    }

    /*
        Service for processing vacation requests
     */
    public VacationRequestEntity processRequest(UUID requestId, VacationRequestEntity vacationRequest) {
        validateVacationRequestToBeProcessed(vacationRequest);
        VacationRequestEntity request = vacationRequestDAO.findById(requestId);
        if(request == null){
            throw new CustomException(CommonConstants.VACATION_NOT_EXISTS);
        }
        request.setStatus(vacationRequest.getStatus());
        request.setResolvedBy(vacationRequest.getResolvedBy());
        vacationRequestDAO.update(request);
        return request;
    }

    public boolean areOverlappingRequests(VacationRequestEntity req1, VacationRequestEntity req2) {
        LocalDate startDate1 = req1.getVacationStartDate();
        LocalDate endDate1 = req1.getVacationEndDate();
        LocalDate startDate2 = req2.getVacationStartDate();
        LocalDate endDate2 = req2.getVacationEndDate();
        return !req1.equals(req2) &&
                (startDate1.isBefore(endDate2) && endDate1.isAfter(startDate2)) ||
                (startDate1.isEqual(endDate2) || endDate1.isEqual(startDate2));
    }
}
