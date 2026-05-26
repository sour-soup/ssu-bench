package org.soup.ssu.bench.repository;

import org.junit.jupiter.api.Test;
import org.soup.ssu.bench.RepositoryTest;
import org.soup.ssu.bench.repository.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.soup.ssu.bench.generator.EntityGenerator.buildTaskEntity;

@Import(TaskRepository.class)
public class TaskRepositoryTest extends RepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void givenTask_whenCreateTask_thenReturnTaskWithId() {
        // given
        TaskEntity taskEntity = buildTaskEntity();

        // when
        TaskEntity insertedTask = taskRepository.createTask(taskEntity);

        // then
        assertNotNull(insertedTask.id());
        assertEqualsTask(taskEntity.withId(insertedTask.id()), insertedTask);
    }

    @Test
    void givenTaskInDb_whenUpdateExecutor_thenReturnUpdatedTask() {
        // given
        BigInteger newExecutor = BigInteger.valueOf(1334);
        TaskEntity taskEntity = taskRepository.createTask(buildTaskEntity());

        // when
        TaskEntity insertedTask = taskRepository.updateExecutor(taskEntity.id(), newExecutor);

        // then
        assertEqualsTask(taskEntity.withExecutorId(newExecutor), insertedTask);
    }

    @Test
    void givenTaskInDb_whenUpdateStatus_thenReturnUpdatedTask() {
        // given
        String newStatus = "accepted";
        TaskEntity taskEntity = taskRepository.createTask(buildTaskEntity());

        // when
        TaskEntity insertedTask = taskRepository.updateStatus(taskEntity.id(), newStatus);

        // then
        assertEqualsTask(taskEntity.withStatus(newStatus), insertedTask);
    }

    @Test
    void givenTaskInDb_whenGetTaskById_thenReturnTask() {
        // given
        TaskEntity taskEntity = taskRepository.createTask(buildTaskEntity());

        // when
        Optional<TaskEntity> result = taskRepository.getTaskById(taskEntity.id());

        // then
        assertTrue(result.isPresent());
        assertEqualsTask(taskEntity, result.get());
    }

    @Test
    void givenEmptyDb_whenGetTaskById_thenReturnEmpty() {
        // when
        Optional<TaskEntity> result = taskRepository.getTaskById(BigInteger.valueOf(999));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void givenTaskInDb_whenGetTasks_thenReturnTask() {
        // given
        TaskEntity taskEntity = taskRepository.createTask(buildTaskEntity());

        // when
        List<TaskEntity> result = taskRepository.getTasks(0, 10, taskEntity.status());

        // then
        assertThat(result).containsExactly(taskEntity);
    }

    @Test
    void givenTasksInDb_whenGetTasks_thenReturnLimitTasks() {
        // given
        taskRepository.createTask(buildTaskEntity());
        taskRepository.createTask(buildTaskEntity());
        taskRepository.createTask(buildTaskEntity());

        // when
        List<TaskEntity> result = taskRepository.getTasks(0, 2, TaskStatusEnum.PUBLISHED.getValue());

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    void givenTasksInDb_whenGetTasks_thenReturnOffsetTasks() {
        // given
        taskRepository.createTask(buildTaskEntity());
        taskRepository.createTask(buildTaskEntity());
        taskRepository.createTask(buildTaskEntity());

        // when
        List<TaskEntity> result = taskRepository.getTasks(1, 2, TaskStatusEnum.PUBLISHED.getValue());

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    void givenEmptyDb_whenGetTasks_thenReturnEmptyList() {
        // when
        List<TaskEntity> result = taskRepository.getTasks(0, 1, TaskStatusEnum.PUBLISHED.getValue());

        // then
        assertThat(result).isEmpty();
    }

    private static void assertEqualsTask(TaskEntity expected, TaskEntity actual) {
        assertEquals(expected.id(), actual.id());
        assertEquals(expected.title(), actual.title());
        assertEquals(expected.description(), actual.description());
        assertEquals(expected.status(), actual.status());
        assertEquals(expected.executorId(), actual.executorId());
    }
}
