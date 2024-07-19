package com.vacationtracker.ws.controller;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.utils.CommonConstants;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import com.vacationtracker.ws.service.ManagerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static com.vacationtracker.utils.Validator.parseStatusParameter;

@RestController
@RequestMapping("/api/v1/managers")
public class ManagerController {

    ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    //Route to return all the vacation requests
    @GetMapping("/requests")
    public List<VacationRequestEntity> getAllVacationRequests(@RequestParam(required = false) String status) {
        if(StringUtils.isEmpty(status)) {
            return managerService.getAllVacationRequests();
        }
        List<Status> statuses = parseStatusParameter(status);
        if (statuses.isEmpty()) {
            throw new CustomException(CommonConstants.INVALID_STATUS_PARAMETER + status);
        }

        return managerService.getAllVacationRequestsWithStatuses(new HashSet<>(statuses));
    }

    //Route to return vacation requests for a specific employee
    @GetMapping("/employees/{employeeId}/requests")
    public List<VacationRequestEntity> getVacationRequestsByEmployee(@PathVariable int employeeId) {
        return managerService.getVacationRequestsByEmployee(employeeId);
    }

    //Route to return all overlapping vacation requests
    @GetMapping("/overlapping-requests")
    public List<VacationRequestEntity> getOverlappingVacationRequests() {
        return managerService.getAllOverlappingRequests();
    }

    //Route to process a vacation request
    @PostMapping("/requests/{requestId}")
    public ResponseEntity<VacationRequestEntity> processRequest(@PathVariable UUID requestId, @RequestBody VacationRequestEntity vacationRequest) {
        VacationRequestEntity postedRequest = managerService.processRequest(requestId, vacationRequest);
        return ResponseEntity.status(HttpStatus.OK).body(postedRequest);
    }

}
