package com.vacationtracker.ws.controller;

import com.vacationtracker.entity.enums.Status;
import com.vacationtracker.entity.VacationRequestEntity;
import com.vacationtracker.ws.exceptionhandler.CustomException;
import com.vacationtracker.ws.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class EmployeeControllerTest {

    public static final String BASE_PATH_URL = "/api/v1/employees/";

    int employeeId;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private VacationRequestEntity request;

    @BeforeEach
    void setUp() {
        employeeId = 1;
        request = new VacationRequestEntity(UUID.randomUUID(), employeeId, Status.PENDING,
                LocalDateTime.now(), LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 4));
    }

     /*
        TESTS FOR GETTING ALL VACATION REQUESTS FOR EMPLOYEE
     */
    @Test
    void testGetRequestsWithoutRequestParam() throws Exception {
        when(employeeService.getRequests(employeeId)).thenReturn(Collections.singletonList(request));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "{employeeId}/requests", employeeId))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(employeeService, times(1)).getRequests(employeeId);

    }

    @Test
    void testGetRequestsWithRequestParam() throws Exception {
        when(employeeService.getRequestsByStatus(employeeId, new HashSet<>(List.of(Status.PENDING))))
                .thenReturn(Collections.singletonList(request));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "{employeeId}/requests?status=PENDING", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(employeeService, times(1)).getRequestsByStatus(employeeId, new HashSet<>(Collections.singletonList(Status.PENDING)));
    }

    @Test
    void testGetRequestsWithInvalidStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "{employeeId}/requests?status=INVALID", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(status().is4xxClientError());
    }

     /*
        TESTS FOR GETTING REMAINING VACATION DAYS FOR EMPLOYEE
     */

    @Test
    void testGetRemainingVacationDays() throws Exception {
        when(employeeService.getRemainingVacationDays(employeeId)).thenReturn(15);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_PATH_URL + "{employeeId}/remaining-days", employeeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("15"));
        verify(employeeService, times(1)).getRemainingVacationDays(employeeId);
    }

     /*
        TESTS FOR CREATING A VACATION REQUEST
     */

    @Test
    void testCreateRequest() throws Exception {
        when(employeeService.createRequest(eq(employeeId), any(VacationRequestEntity.class)))
                .thenReturn(request);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH_URL + "{employeeId}/requests", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"id\":\"" + request.getId() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        verify(employeeService, times(1)).createRequest(eq(employeeId), any(VacationRequestEntity.class));
    }

    @Test
    void testCreateRequestFailure() throws Exception {
        when(employeeService.createRequest(eq(employeeId), any(VacationRequestEntity.class)))
                .thenThrow(new CustomException("Failed to create request"));

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_PATH_URL + "{employeeId}/requests", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + request.getId() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CustomException))
                .andExpect(result -> assertEquals("Failed to create request",
                        result.getResolvedException().getMessage()));

        verify(employeeService, times(1)).createRequest(eq(employeeId), any(VacationRequestEntity.class));
    }

     /*
        TESTS FOR DELETING A VACATION REQUEST
     */

    @Test
    void testDeleteRequest() throws Exception {
        UUID requestId = UUID.randomUUID();
        when(employeeService.deleteRequest(requestId)).thenReturn(request);

        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_PATH_URL + "requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(employeeService, times(1)).deleteRequest(requestId);
    }

    @Test
    void testDeleteRequestFailure() throws Exception {
        UUID requestId = UUID.randomUUID();
        when(employeeService.deleteRequest(requestId)).thenThrow(new CustomException("Bad Request"));

        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_PATH_URL + "requests/{requestId}", requestId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CustomException))
                .andExpect(result -> assertEquals("Bad Request",
                        result.getResolvedException().getMessage()));

        verify(employeeService, times(1)).deleteRequest(requestId);
    }

}
