package com.vacationtracker.utils;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.ws.exceptionhandler.CustomException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Validator {

    public static List<Status> parseStatusParameter(String status) {
        String[] statusArray = status.split(CommonConstants.COMMA_DELIMITER);
        return Arrays.stream(statusArray)
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(Validator::isStatusValid) // Validate each status
                .map(Status::valueOf)
                .collect(Collectors.toList());
    }

    private static boolean isStatusValid(String status){
        return status.equalsIgnoreCase(String.valueOf(Status.PENDING))
                || status.equalsIgnoreCase(String.valueOf(Status.APPROVED))
                || status.equalsIgnoreCase(String.valueOf(Status.REJECTED));
    }

    public static void validateVacationRequestToBeProcessed(VacationRequestEntity vacationRequest) {
        if(vacationRequest == null){
            throw new CustomException(CommonConstants.MISSING_VACATION_REQUEST);
        } else if(vacationRequest.getStatus() == null){
            throw new CustomException(CommonConstants.MISSING_STATUS_VALUE);
        } else if(vacationRequest.getResolvedBy() <= 1){
            throw new CustomException(CommonConstants.MISSING_OR_INVALID_MANAGER_ID);
        } else if(vacationRequest.getStatus() != Status.APPROVED && vacationRequest.getStatus() != Status.REJECTED) {
            throw new CustomException("Invalid Status " + vacationRequest.getStatus());
        }
    }

    public static void validateNewVacationRequest(VacationRequestEntity vacationRequest) {
        if(vacationRequest == null){
            throw new CustomException(CommonConstants.MISSING_VACATION_REQUEST);
        } else if(vacationRequest.getVacationStartDate() == null){
            throw new CustomException(CommonConstants.VACATION_START_DATE_NULL_CHECK);
        } else if(vacationRequest.getVacationEndDate() == null){
            throw new CustomException(CommonConstants.VACATION_END_DATE_NULL_CHECK);
        }
    }

}
