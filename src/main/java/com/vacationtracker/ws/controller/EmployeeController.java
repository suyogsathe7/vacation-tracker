package com.vacationtracker.ws.controller;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.utils.CommonConstants;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import com.vacationtracker.ws.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.vacationtracker.utils.Validator.parseStatusParameter;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    //Route to get employee vacation requests
    /*
        Note: This endpoint can also be used for managers to see employee request's.
     */
    @GetMapping("/{employeeId}/requests")
    public List<VacationRequestEntity> getRequests(@PathVariable int employeeId, @RequestParam(required = false) String status) {
        if(StringUtils.isEmpty(status)) {
            return employeeService.getRequests(employeeId);
        }
        List<Status> statuses = parseStatusParameter(status);
        if (statuses.isEmpty()) {
            throw new CustomException(CommonConstants.INVALID_STATUS_PARAMETER + status);
        }
        return employeeService.getRequestsByStatus(employeeId, new HashSet<>(statuses));
    }

    //Route to get employee's remaining vacation days
    // assuming weekends are not incorporated
    @GetMapping("/{employeeId}/remaining-days")
    public int getRemainingVacationDays(@PathVariable int employeeId) {
        return employeeService.getRemainingVacationDays(employeeId);
    }

    //Route to create a vacation request
    @PostMapping("/{employeeId}/requests")
    public ResponseEntity<VacationRequestEntity> createRequest(@PathVariable int employeeId, @RequestBody VacationRequestEntity request) {
        VacationRequestEntity postedRequest = employeeService.createRequest(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(postedRequest);
    }

    //Route to delete a vacation request
    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<VacationRequestEntity> deleteRequest(@PathVariable UUID requestId) {
        VacationRequestEntity deletedRequest = employeeService.deleteRequest(requestId);
        return ResponseEntity.ok().body(deletedRequest);
    }

}
