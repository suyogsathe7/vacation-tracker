package com.vacationtracker.ws.service;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.utils.CommonConstants;
import com.vacationtracker.ws.dao.VacationRequestDAO;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.vacationtracker.utils.CommonConstants.VACATION_DAYS_EXCEEDED_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private VacationRequestDAO vacationRequestDAO;

    @InjectMocks
    private EmployeeService employeeService;

    private int validEmployeeId;
    private VacationRequestEntity request;

    @BeforeEach
    public void init(){
        validEmployeeId = 1;
        request = new VacationRequestEntity(UUID.randomUUID(), validEmployeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5));
    }

    @Test
    public void testGetRequestsWithValidEmployeeId() {
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Collections.singleton(validEmployeeId));
        when(vacationRequestDAO.findByEmployee(validEmployeeId)).thenReturn(Collections.singletonList(request));

        List<VacationRequestEntity> requests = employeeService.getRequests(validEmployeeId);
        assertEquals(1, requests.size());
        assertEquals(request, requests.get(0));

        verify(vacationRequestDAO, times(1)).getEmployeeIds();
        verify(vacationRequestDAO, times(1)).findByEmployee(validEmployeeId);
    }

    @Test
    public void testGetRequestsWithInvalidEmployeeId() {
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Collections.emptySet());

        CustomException exception = assertThrows(CustomException.class, () -> {
            employeeService.getRequests(999);
        });
        assertEquals(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST, exception.getMessage());
        verify(vacationRequestDAO, times(1)).getEmployeeIds();
    }

    @Test
    public void testGetRequestsByStatus() {
        VacationRequestEntity pendingRequest = new VacationRequestEntity(UUID.randomUUID(), validEmployeeId, Status.PENDING, LocalDateTime.now(),
                LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5));

        VacationRequestEntity approvedRequest = new VacationRequestEntity(UUID.randomUUID(), validEmployeeId, Status.APPROVED, LocalDateTime.now(),
                LocalDate.of(2024, 5, 5), LocalDate.of(2024, 5, 10));

        when(vacationRequestDAO.findByEmployee(validEmployeeId)).thenReturn(Arrays.asList(pendingRequest, approvedRequest));
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(validEmployeeId));

        Set<Status> statuses = new HashSet<>(Collections.singletonList(Status.APPROVED));

        List<VacationRequestEntity> requests = employeeService.getRequestsByStatus(validEmployeeId, statuses);

        assertEquals(1, requests.size());
        assertEquals(Status.APPROVED, requests.get(0).getStatus());
        verify(vacationRequestDAO).findByEmployee(validEmployeeId);
        verify(vacationRequestDAO, times(1)).getEmployeeIds();
    }

    @Test
    public void testGetRemainingVacationDaysForEmployeesWithNoVacationRequests() {
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Collections.singleton(validEmployeeId));
        when(vacationRequestDAO.findByEmployee(validEmployeeId)).thenReturn(Collections.emptyList());

        int remainingDays = employeeService.getRemainingVacationDays(validEmployeeId);
        assertEquals(30, remainingDays);

        verify(vacationRequestDAO, times(1)).getEmployeeIds();
        verify(vacationRequestDAO, times(1)).findByEmployee(validEmployeeId);
    }

    @Test
    public void testGetRemainingVacationDaysForEmployeesWithApprovedRequests() {
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Collections.singleton(validEmployeeId));
        when(vacationRequestDAO.findByEmployee(validEmployeeId)).thenReturn(Arrays.asList(
                new VacationRequestEntity(UUID.randomUUID(), validEmployeeId, Status.APPROVED,
                        LocalDateTime.now(), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5))
        ));

        int remainingDays = employeeService.getRemainingVacationDays(validEmployeeId);
        assertEquals(25, remainingDays);

        verify(vacationRequestDAO, times(1)).getEmployeeIds();
        verify(vacationRequestDAO, times(1)).findByEmployee(validEmployeeId);
    }

    @Test
    public void testCreateRequestWithInsufficientVacationDays() {
        VacationRequestEntity request = new VacationRequestEntity();
        request.setVacationStartDate(LocalDate.of(2024, 8, 1));
        request.setVacationEndDate(LocalDate.of(2024, 8, 30)); // 30 days requested

        VacationRequestEntity existingRequest = new VacationRequestEntity();
        existingRequest.setStatus(Status.APPROVED);
        existingRequest.setVacationStartDate(LocalDate.of(2024, 8, 1));
        existingRequest.setVacationEndDate(LocalDate.of(2024, 8, 10));

        when(vacationRequestDAO.findByEmployee(validEmployeeId)).thenReturn(List.of(existingRequest));
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Collections.singleton(validEmployeeId));

        CustomException exception = assertThrows(CustomException.class, () -> employeeService.createRequest(validEmployeeId, request));
        assertEquals(VACATION_DAYS_EXCEEDED_ERROR + "20", exception.getMessage());
    }

    @Test
    public void testCreateRequestSuccess() {

        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(validEmployeeId));
        when(vacationRequestDAO.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(vacationRequestDAO).save(any(VacationRequestEntity.class));

        VacationRequestEntity createdRequest = employeeService.createRequest(validEmployeeId, request);
        assertNotNull(createdRequest);
        assertEquals(validEmployeeId, createdRequest.getEmployeeId());
        assertEquals(Status.PENDING, createdRequest.getStatus());
        assertNotNull(createdRequest.getId());
        assertEquals(request.getVacationStartDate(), createdRequest.getVacationStartDate());
        assertEquals(request.getVacationEndDate(), createdRequest.getVacationEndDate());
        assertEquals(validEmployeeId, createdRequest.getEmployeeId());

        verify(vacationRequestDAO).save(any(VacationRequestEntity.class));
        verify(vacationRequestDAO, times(3)).getEmployeeIds();
        verify(vacationRequestDAO, times(1)).findByEmployee(validEmployeeId);
    }

    @Test
    public void testCreateRequestWhenStartDateIsAfterEndDate() {
        VacationRequestEntity request1 = new VacationRequestEntity(UUID.randomUUID(), validEmployeeId, Status.PENDING, LocalDateTime.now(),
                LocalDate.of(2024, 5, 7), LocalDate.of(2024, 5, 5));

        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(validEmployeeId));

        CustomException exception = assertThrows(CustomException.class, () -> {
            employeeService.createRequest(validEmployeeId, request1);
        });
        assertEquals(CommonConstants.START_DATE_CANNOT_BE_AFTER_END_DATE, exception.getMessage());

        verify(vacationRequestDAO, times(1)).getEmployeeIds();
    }

    @Test
    public void testCreateRequestWhenVacationAlreadyExists() {
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(validEmployeeId));
        request.setVacationStartDate(LocalDate.now());
        request.setVacationEndDate(LocalDate.now().plusDays(5));

        when(vacationRequestDAO.findAll()).thenReturn(Collections.singletonList(request));

        CustomException exception = assertThrows(CustomException.class, () -> {
            employeeService.createRequest(validEmployeeId, request);
        });
        assertEquals(CommonConstants.VACATION_ALREADY_EXISTS, exception.getMessage());

        verify(vacationRequestDAO, times(1)).getEmployeeIds();
    }

    @Test
    public void testDeleteRequestSuccess() {
        UUID requestId = UUID.randomUUID();
        when(vacationRequestDAO.findById(requestId)).thenReturn(request);

        employeeService.deleteRequest(requestId);

        verify(vacationRequestDAO).delete(request);
    }

    @Test
    public void testDeleteRequestFailure() {
        UUID requestId = UUID.randomUUID();
        when(vacationRequestDAO.findById(requestId)).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> {
            employeeService.deleteRequest(requestId);
        });
        assertEquals(CommonConstants.VACATION_NOT_EXISTS, exception.getMessage());
    }

    @Test
    public void testSetVacationRequestFields() {
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(validEmployeeId));

        VacationRequestEntity vacationRequest = employeeService.setVacationRequestFields(validEmployeeId, request);
        assertNotNull(vacationRequest);
        assertEquals(validEmployeeId, vacationRequest.getEmployeeId());
        assertNotNull(vacationRequest.getId());
        assertEquals(Status.PENDING, vacationRequest.getStatus());
        assertEquals(request.getVacationStartDate(), vacationRequest.getVacationStartDate());
        assertEquals(request.getVacationEndDate(), vacationRequest.getVacationEndDate());

        verify(vacationRequestDAO, times(1)).getEmployeeIds();
    }

}
