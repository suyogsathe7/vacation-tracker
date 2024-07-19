package com.vacationtracker.ws.dao;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static com.vacationtracker.utils.DataSource.setEmployeesMockData;
import static com.vacationtracker.utils.DataSource.setVacationRequestsMockData;

@Repository
public class VacationRequestDAO {

    private final List<VacationRequestEntity> vacationRequests = new ArrayList<>();
    private Set<Integer> employeeIds;

    public VacationRequestDAO() {
        //Initialize with some mock vacation requests
        employeeIds = new HashSet<>();
        setVacationRequestsMockData(vacationRequests);
        setEmployeesMockData(employeeIds);
    }

    public List<VacationRequestEntity> findAll(){
        return new ArrayList<>(vacationRequests);
    }

    public List<VacationRequestEntity> findAllByMultipleStatuses(Set<Status> statuses){
        return vacationRequests.stream()
                .filter(request -> statuses.contains(request.getStatus()))
                .collect(Collectors.toList());
    }

    public List<VacationRequestEntity> findByEmployee(int employeeId) {
        return vacationRequests.stream()
                .filter(request -> request.getEmployeeId() == employeeId)
                .collect(Collectors.toList());
    }

    public VacationRequestEntity findById(UUID requestId) {
        return vacationRequests.stream()
                .filter(request -> request.getId().equals(requestId))
                .findFirst().orElse(null);
    }

    /*
        Save/Update/Delete Vacation Requests
     */

    public void save(VacationRequestEntity request) {
        vacationRequests.add(request);
    }

    public void update(VacationRequestEntity request) {
        VacationRequestEntity existingVacationRequest = findById(request.getId());
        if (existingVacationRequest != null) {
            existingVacationRequest.setStatus(request.getStatus());
            existingVacationRequest.setResolvedBy(request.getResolvedBy());
        }
    }

    public void delete(VacationRequestEntity request) {
        VacationRequestEntity existingVacationRequest = findById(request.getId());
        if (existingVacationRequest != null) {
            vacationRequests.remove(request);
        }
    }

    public Set<Integer> getEmployeeIds() {
        return employeeIds;
    }

}
