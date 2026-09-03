package com.ilu.system.operator.service;

import com.ilu.system.operator.dto.CreateOperatorRequest;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.Team;
import com.ilu.system.operator.repository.*;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperatorWorkflowScenariosTest {

    @Mock
    private OperatorRepository operatorRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private com.ilu.system.structure.repository.WorkstationRepository workstationRepository;
    @Mock
    private WorkstationFormationRepository workstationFormationRepository;
    @Mock
    private FormationAssignmentRepository formationAssignmentRepository;
    @Mock
    private RecyclagePlanningRepository recyclagePlanningRepository;

    private OperatorService operatorService;

    @BeforeEach
    void setUp() {
        operatorService = new OperatorService(
                operatorRepository,
                teamRepository,
                projectRepository,
                zoneRepository,
                workstationRepository,
                workstationFormationRepository,
                formationAssignmentRepository,
                recyclagePlanningRepository
        );
    }

    @Test
    @DisplayName("Scenario 1: RH creates an operator and assigns Team (Chef d'Équipe)")
    void testRhCreateOperatorWithTeam() {
        CreateOperatorRequest req = new CreateOperatorRequest();
        req.setEmployeeId("EMP-1001");
        req.setLastName("Benali");
        req.setFirstName("Amine");
        req.setRole("Opérateur");
        req.setTeamId(1L);

        Team team = new Team();
        team.setId(1L);
        team.setTeamLeader("Hassan B.");
        team.setTeamLeaderEmployeeId("TL-001");

        when(operatorRepository.existsByEmployeeId("EMP-1001")).thenReturn(false);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(operatorRepository.save(any(Operator.class))).thenAnswer(inv -> inv.getArgument(0));

        Operator result = operatorService.createOperator(req);

        assertNotNull(result);
        assertEquals("EMP-1001", result.getEmployeeId());
        assertEquals("Benali", result.getLastName());
        assertEquals(team, result.getTeam());
        assertTrue(result.getActive());
    }

    @Test
    @DisplayName("Scenario 2: Chef d'Équipe claims an unassigned operator")
    void testClaimUnassignedOperator() {
        Operator op = new Operator();
        op.setId(10L);
        op.setEmployeeId("EMP-2002");
        op.setLastName("El Fassi");
        op.setFirstName("Nadia");
        op.setActive(true);

        Team chefTeam = new Team();
        chefTeam.setId(2L);
        chefTeam.setTeamLeader("Karim M.");

        when(operatorRepository.findById(10L)).thenReturn(Optional.of(op));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(chefTeam));
        when(operatorRepository.save(any(Operator.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateOperatorRequest updateReq = new CreateOperatorRequest();
        updateReq.setTeamId(2L);

        Operator updated = operatorService.updateOperator(10L, updateReq);

        assertNotNull(updated);
        assertEquals(chefTeam, updated.getTeam());
        assertEquals("Karim M.", updated.getTeam().getTeamLeader());
    }

    @Test
    @DisplayName("Scenario 3: Deactivating an operator soft deletes pending recyclage plannings")
    void testDeactivateOperatorCleansPlannings() {
        Operator op = new Operator();
        op.setId(5L);
        op.setEmployeeId("EMP-3003");
        op.setActive(true);

        when(operatorRepository.findById(5L)).thenReturn(Optional.of(op));
        when(operatorRepository.save(any(Operator.class))).thenAnswer(inv -> inv.getArgument(0));

        operatorService.deactivateOperator(5L);

        assertFalse(op.getActive());
        assertNotNull(op.getExitDate());
        verify(recyclagePlanningRepository, times(1)).deleteByOperator_IdAndStatusIn(eq(5L), any());
    }
}
