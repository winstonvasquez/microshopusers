package com.microshop.rrhh.application.command;

import com.microshop.rrhh.application.dto.evaluation.GoalRequestDto;
import com.microshop.rrhh.application.dto.training.TrainingParticipationRequestDto;
import com.microshop.rrhh.application.dto.training.TrainingRequestDto;
import com.microshop.rrhh.application.mapper.EvaluationMapper;
import com.microshop.rrhh.application.mapper.TrainingMapper;
import com.microshop.rrhh.application.mapper.TrainingParticipationMapper;
import com.microshop.rrhh.config.security.TenantContext;
import com.microshop.rrhh.domain.model.Goal;
import com.microshop.rrhh.domain.model.Training;
import com.microshop.rrhh.infrastructure.persistence.repository.EmployeeRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.GoalRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.TrainingParticipationRepository;
import com.microshop.rrhh.infrastructure.persistence.repository.TrainingRepository;
import com.microshop.users.application.MessageHelper;
import com.microshop.users.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** R2P-6: metas y capacitaciones en estado terminal no se editan (409). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EstadoTerminalGuardTest {

    @Mock private GoalRepository goalRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private EvaluationMapper evaluationMapper;
    @Mock private TenantContext tenantContext;
    @Mock private TrainingRepository trainingRepository;
    @Mock private TrainingParticipationRepository participationRepository;
    @Mock private TrainingMapper trainingMapper;
    @Mock private TrainingParticipationMapper participationMapper;
    @Mock private MessageHelper msg;

    private GoalCommandService goals() {
        return new GoalCommandService(goalRepository, employeeRepository, evaluationMapper, tenantContext);
    }

    private TrainingCommandService trainings() {
        return new TrainingCommandService(trainingRepository, participationRepository, employeeRepository,
                trainingMapper, participationMapper, tenantContext, msg);
    }

    private Goal meta(Goal.GoalStatus estado) {
        Goal g = new Goal();
        g.setEstado(estado);
        when(tenantContext.getCurrentTenantId()).thenReturn(1L);
        when(goalRepository.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(g));
        return g;
    }

    private void capacitacion(Training.TrainingStatus estado) {
        Training t = new Training();
        t.setEstado(estado);
        when(tenantContext.getCurrentTenantId()).thenReturn(1L);
        when(trainingRepository.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(t));
    }

    @Test
    void updateProgressNoRevivaMetaCancelada() {
        Goal g = meta(Goal.GoalStatus.CANCELADO);
        assertThatThrownBy(() -> goals().updateProgress(5L, new BigDecimal("100")))
                .isInstanceOf(ConflictException.class);
        assertThat(g.getEstado()).isEqualTo(Goal.GoalStatus.CANCELADO);
        verify(goalRepository, never()).save(any());
    }

    @Test
    void updateGoalRechazaMetaCompletada() {
        meta(Goal.GoalStatus.COMPLETADO);
        assertThatThrownBy(() -> goals().updateGoal(5L, mock(GoalRequestDto.class)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateProgressSigueFuncionandoEnMetaEnProgreso() {
        Goal g = meta(Goal.GoalStatus.EN_PROGRESO);
        when(goalRepository.save(g)).thenReturn(g);
        goals().updateProgress(5L, new BigDecimal("100"));
        assertThat(g.getEstado()).isEqualTo(Goal.GoalStatus.COMPLETADO);
    }

    @Test
    void updateTrainingRechazaCancelada() {
        capacitacion(Training.TrainingStatus.CANCELADO);
        assertThatThrownBy(() -> trainings().updateTraining(5L, mock(TrainingRequestDto.class)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void enrollParticipantRechazaCapacitacionCancelada() {
        capacitacion(Training.TrainingStatus.CANCELADO);
        TrainingParticipationRequestDto req = mock(TrainingParticipationRequestDto.class);
        when(req.trainingId()).thenReturn(5L);
        assertThatThrownBy(() -> trainings().enrollParticipant(req)).isInstanceOf(ConflictException.class);
        verify(participationRepository, never()).save(any());
    }
}
