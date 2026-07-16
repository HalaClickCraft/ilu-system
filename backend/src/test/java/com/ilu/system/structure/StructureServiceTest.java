package com.ilu.system.structure;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StructureServiceTest {

    @Test
    void shouldSeedDefaultProjectsWhenRepositoryIsEmpty() {
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        ZoneRepository zoneRepository = mock(ZoneRepository.class);
        PosteTravailRepository posteTravailRepository = mock(PosteTravailRepository.class);

        when(projectRepository.findAll()).thenReturn(List.of());
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(zoneRepository.save(any(Zone.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(posteTravailRepository.save(any(PosteTravail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StructureService service = new StructureService(projectRepository, zoneRepository, posteTravailRepository);

        service.seedDefaultStructureIfNeeded();

        verify(projectRepository, atLeast(5)).save(any(Project.class));
    }
}
