package com.vacationtracker.ws.controller;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import com.vacationtracker.ws.service.ManagerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class ManagerControllerTest {

    private static final String BASE_PATH_URL = "/api/v1/managers/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerService managerService;

    private int employeeId;
    private VacationRequestEntity request;

    @BeforeEach
    void setUp() {
        employeeId = 1;
        request = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 4));
    }

    /*
        TESTS FOR GETTING ALL VACATION REQUESTS
     */

    @Test
    public void testGetAllVacationRequestsWithNoRequestParams() throws Exception {
        when(managerService.getAllVacationRequests()).thenReturn(Collections.singletonList(request));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "requests"))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(managerService, times(1)).getAllVacationRequests();

    }

    @Test
    public void testGetAllVacationRequestsWithRequestParam() throws Exception {
        when(managerService.getAllVacationRequestsWithStatuses(Set.of(Status.APPROVED, Status.PENDING)))
                .thenReturn(Collections.singletonList(request));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "requests?status=APPROVED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(managerService, times(1)).getAllVacationRequestsWithStatuses(Set.of(Status.APPROVED));
    }

    @Test
    public void testGetAllVacationRequestsWithInvalidStatusParam() throws Exception {
        String invalidStatus = "INVALID_STATUS";
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "requests?status=" + invalidStatus)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().is4xxClientError());

    }

    @Test
    public void testGetAllVacationRequestsWithInvalidURL() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "requestsssssssssss?status=PENDING")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(status().is4xxClientError());
    }

     /*
        TESTS FOR GETTING ALL VACATION REQUESTS BY EMPLOYEE
     */
     @Test
     void testGetVacationRequestsByEmployeeWithInvalidEmployeeId() throws Exception {
         int employeeId = -1;
         when(managerService.getVacationRequestsByEmployee(employeeId))
                 .thenThrow(new CustomException("Invalid Employee Id"));

         mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "employees/{employeeId}/requests", employeeId)
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().is4xxClientError())
                 .andExpect(result -> assertTrue(result.getResolvedException() instanceof CustomException))
                 .andExpect(result -> assertEquals("Invalid Employee Id",
                         result.getResolvedException().getMessage()));

         verify(managerService, times(1)).getVacationRequestsByEmployee(employeeId);
     }

    @Test
    void testGetVacationRequestsByEmployee() throws Exception {
        when(managerService.getVacationRequestsByEmployee(employeeId)).thenReturn(Collections.singletonList(request));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "employees/{employeeId}/requests", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(managerService, times(1)).getVacationRequestsByEmployee(employeeId);
    }

    /*
        TESTS FOR GETTING ALL OVERLAPPING VACATION REQUESTS
     */
    @Test
    void testGetOverlappingVacationRequests() throws Exception {
        when(managerService.getAllOverlappingRequests()).thenReturn(Collections.singletonList(request));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "overlapping-requests")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(managerService, times(1)).getAllOverlappingRequests();
    }

    @Test
    void testProcessRequestSuccess() throws Exception {
        UUID requestId = UUID.randomUUID();
        when(managerService.processRequest(eq(requestId), any(VacationRequestEntity.class))).thenReturn(request);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH_URL + "requests/" + requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")) // Assuming the JSON is an empty object for simplicity
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(managerService, times(1)).processRequest(eq(requestId), any(VacationRequestEntity.class));
    }

    @Test
    void testProcessRequestFailure() throws Exception {
        UUID requestId = UUID.randomUUID();
        when(managerService.processRequest(eq(requestId), any(VacationRequestEntity.class)))
                .thenThrow(new CustomException("Request not found"));

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH_URL + "requests/" + requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(status().is4xxClientError());

        verify(managerService, times(1)).processRequest(eq(requestId), any(VacationRequestEntity.class));
    }
}
