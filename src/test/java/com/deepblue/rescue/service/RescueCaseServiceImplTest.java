package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.rescue.dto.response.RescueCaseResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.RescueCaseMapper;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.service.impl.RescueCaseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository repository;

    @Mock
    private RescueCaseMapper mapper;

    @InjectMocks
    private RescueCaseServiceImpl service;

    private RescueCase buildRescueCase(RescueStatus status) {
        return new RescueCase("RC-001", LocalDate.now(), "Bahía Concha", status);
    }

    private RescueCaseResponse buildResponse(RescueStatus status) {
        return new RescueCaseResponse(
                1L, "RC-001", LocalDate.now(), "Bahía Concha", status, "CTR-001", "AN-001");
    }

    // TEST 1: caso existente → retorna DTO
    @Test
    void deberiaRetornarCasoCuandoExiste() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.ADMITTED);
        RescueCaseResponse response = buildResponse(RescueStatus.ADMITTED);

        when(repository.findByCaseCode("RC-001")).thenReturn(Optional.of(rescueCase));
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        RescueCaseResponse result = service.findByCode("RC-001");

        assertThat(result.caseCode()).isEqualTo("RC-001");
    }

    // TEST 2: caso inexistente → ResourceNotFoundException
    @Test
    void deberiaLanzarExcepcionCuandoCasoNoExiste() {
        when(repository.findByCaseCode("RC-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("RC-999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // TEST 3: transición válida → llama a save()
    @Test
    void deberiaCambiarEstadoCuandoTransicionEsValida() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.ADMITTED);
        RescueCaseResponse response = buildResponse(RescueStatus.UNDER_EVALUATION);
        ChangeRescueStatusRequest request = new ChangeRescueStatusRequest(RescueStatus.UNDER_EVALUATION);

        when(repository.findByCaseCode("RC-001")).thenReturn(Optional.of(rescueCase));
        when(repository.save(rescueCase)).thenReturn(rescueCase);
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        RescueCaseResponse result = service.changeStatus("RC-001", request);

        assertThat(result.status()).isEqualTo(RescueStatus.UNDER_EVALUATION);
        verify(repository).save(rescueCase);
    }

    // TEST 4: transición inválida → BusinessRuleException y NUNCA save()
    @Test
    void noDeberiaCambiarEstadoCuandoTransicionEsInvalida() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.ADMITTED);
        ChangeRescueStatusRequest request = new ChangeRescueStatusRequest(RescueStatus.RELEASED);

        when(repository.findByCaseCode("RC-001")).thenReturn(Optional.of(rescueCase));

        assertThatThrownBy(() -> service.changeStatus("RC-001", request))
                .isInstanceOf(BusinessRuleException.class);

        verify(repository, never()).save(any());
    }

    // Extra: findByStatus
    @Test
    void deberiaRetornarCasosPorEstado() {
        RescueCase rescueCase = buildRescueCase(RescueStatus.IN_REHABILITATION);
        RescueCaseResponse response = buildResponse(RescueStatus.IN_REHABILITATION);

        when(repository.findByStatusOrderByRescueDateAsc(RescueStatus.IN_REHABILITATION))
                .thenReturn(List.of(rescueCase));
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        List<RescueCaseResponse> result = service.findByStatus(RescueStatus.IN_REHABILITATION);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).caseCode()).isEqualTo("RC-001");
    }
}