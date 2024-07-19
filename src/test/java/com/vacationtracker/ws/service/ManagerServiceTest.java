import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.utils.CommonConstants;
import com.vacationtracker.ws.dao.VacationRequestDAO;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import com.vacationtracker.ws.service.ManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {

    @Mock
    private VacationRequestDAO vacationRequestDAO;

    @InjectMocks
    private ManagerService managerService;

    private int employeeId;
    private VacationRequestEntity request;

    @BeforeEach
    public void setUp() {
        employeeId = 1;
        request = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 4));
    }

    @Test
    public void testGetAllVacationRequests() {
        when(vacationRequestDAO.findAll()).thenReturn(Collections.singletonList(request));

        List<VacationRequestEntity> requests = managerService.getAllVacationRequests();

        assertEquals(1, requests.size());
        assertEquals(request, requests.get(0));

        verify(vacationRequestDAO, times(1)).findAll();
    }

    @Test
    public void testGetAllVacationRequestsWithStatus() {
        VacationRequestEntity approvedRequest = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.APPROVED,
                LocalDateTime.now(), LocalDate.of(2024, 5, 5), LocalDate.of(2024, 5, 10));

        when(vacationRequestDAO.findAll()).thenReturn(Arrays.asList(request, approvedRequest));

        Set<Status> statuses = new HashSet<>(Collections.singletonList(Status.APPROVED));
        List<VacationRequestEntity> requests = managerService.getAllVacationRequestsWithStatuses(statuses);

        assertEquals(1, requests.size());
        assertEquals(approvedRequest, requests.get(0));

        verify(vacationRequestDAO, times(1)).findAll();
    }

    @Test
    public void testGetAllVacationRequestsWithMultipleStatuses() {
        VacationRequestEntity approvedRequest = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.APPROVED,
                LocalDateTime.now(), LocalDate.of(2024, 5, 5), LocalDate.of(2024, 5, 10));
        VacationRequestEntity pendingRequest = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 5), LocalDate.of(2024, 5, 10));

        when(vacationRequestDAO.findAll()).thenReturn(Arrays.asList(pendingRequest, approvedRequest));

        Set<Status> statuses = new HashSet<>(List.of(Status.APPROVED, Status.PENDING));
        List<VacationRequestEntity> requests = managerService.getAllVacationRequestsWithStatuses(statuses);

        assertEquals(2, requests.size());

        verify(vacationRequestDAO, times(1)).findAll();
    }

    @Test
    public void testGetVacationRequestsByEmployeeWithValidEmployeeId() {
        when(vacationRequestDAO.findAll()).thenReturn(Collections.singletonList(request));
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(employeeId));

        List<VacationRequestEntity> requests = managerService.getVacationRequestsByEmployee(employeeId);

        assertEquals(1, requests.size());
        assertEquals(request, requests.get(0));

        verify(vacationRequestDAO, times(1)).findAll();
        verify(vacationRequestDAO, times(1)).getEmployeeIds();
    }

    @Test
    public void testGetVacationRequestsByEmployeeWithInvalidEmployeeId() {
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(2));

        CustomException exception = assertThrows(CustomException.class, () -> {
            managerService.getVacationRequestsByEmployee(employeeId);
        });
        assertEquals(CommonConstants.EMPLOYEE_ID_DOES_NOT_EXIST, exception.getMessage());
    }

    @Test
    public void testGetVacationRequestsByEmployeeWithNoRequests() {
        int newEmployeeId = 11;
        when(vacationRequestDAO.findAll()).thenReturn(Collections.singletonList(request));
        when(vacationRequestDAO.getEmployeeIds()).thenReturn(Set.of(newEmployeeId));

        List<VacationRequestEntity> requests = managerService.getVacationRequestsByEmployee(newEmployeeId);

        assertTrue(requests.isEmpty());

        verify(vacationRequestDAO, times(1)).findAll();
        verify(vacationRequestDAO, times(1)).getEmployeeIds();
    }

    @Test
    public void testGetAllOverlappingRequests() {
        VacationRequestEntity otherRequest = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.APPROVED, LocalDateTime.now(), LocalDate.now(), LocalDate.now().plusDays(10));
        request.setVacationEndDate(LocalDate.now().plusDays(6));

        when(vacationRequestDAO.findAllByMultipleStatuses(Set.of(Status.APPROVED, Status.PENDING)))
                .thenReturn(Arrays.asList(request, otherRequest));

        List<VacationRequestEntity> overlappingRequests = managerService.getAllOverlappingRequests();

        assertEquals(2, overlappingRequests.size());
        assertTrue(overlappingRequests.contains(request));
        assertTrue(overlappingRequests.contains(otherRequest));

        verify(vacationRequestDAO, times(1)).findAllByMultipleStatuses(Set.of(Status.APPROVED, Status.PENDING));

    }

    @Test
    public void testAreOverlappingRequests() {
        VacationRequestEntity req1 = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5));
        VacationRequestEntity req2 = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 4), LocalDate.of(2024, 5, 10));

        assertTrue(managerService.areOverlappingRequests(req1, req2));
    }

    @Test
    public void testAreNotOverlappingRequests() {
        VacationRequestEntity req1 = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 5));
        VacationRequestEntity req2 = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 10));

        assertFalse(managerService.areOverlappingRequests(req1, req2));
    }

    @Test
    public void testAreOverlappingRequestsForSameStartDateForOneRequestAndSameEndDateForOtherRequest() {
        VacationRequestEntity request1 = new VacationRequestEntity();
        request1.setVacationStartDate(LocalDate.of(2024, 8, 1));
        request1.setVacationEndDate(LocalDate.of(2024, 8, 10));

        VacationRequestEntity request2 = new VacationRequestEntity();
        request2.setVacationStartDate(LocalDate.of(2024, 8, 10));
        request2.setVacationEndDate(LocalDate.of(2024, 8, 15));

        assertTrue(managerService.areOverlappingRequests(request1, request2));
    }

    @Test
    public void testProcessRequestSuccess() {
        UUID validRequestId = UUID.randomUUID();
        when(vacationRequestDAO.findById(validRequestId)).thenReturn(request);
        doNothing().when(vacationRequestDAO).update(any(VacationRequestEntity.class));

        request.setStatus(Status.APPROVED);
        request.setResolvedBy(100); //managerId
        VacationRequestEntity processedRequest = managerService.processRequest(validRequestId, request);

        assertEquals(Status.APPROVED, processedRequest.getStatus());
        assertEquals(100, processedRequest.getResolvedBy());

        verify(vacationRequestDAO).update(any(VacationRequestEntity.class));
    }

    @Test
    public void testProcessRequestFailure() {
        UUID validRequestId = UUID.randomUUID();

        CustomException exception = assertThrows(CustomException.class, () -> {
            managerService.processRequest(validRequestId, request);
        });
        assertEquals(CommonConstants.MISSING_OR_INVALID_MANAGER_ID, exception.getMessage());
    }

}
