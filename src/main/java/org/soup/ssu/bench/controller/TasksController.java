package org.soup.ssu.bench.controller;

import lombok.RequiredArgsConstructor;
import org.soup.ssu.bench.config.security.AuthenticatedUser;
import org.soup.ssu.bench.config.security.AuthenticatedUserContext;
import org.soup.ssu.bench.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.TasksApi;
import ssu.bench.model.CreateTaskRequest;
import ssu.bench.model.PageTaskResponse;
import ssu.bench.model.TaskResponse;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;

@RestController
@RequiredArgsConstructor
public class TasksController implements TasksApi {
    private final AuthenticatedUserContext authenticatedUserContext;
    private final TaskService taskService;

    @Override
    public ResponseEntity<PageTaskResponse> getListTasks(Integer page, Integer size, TaskStatusEnum status) {
        PageTaskResponse pageTaskResponse = taskService.getListTasks(page, size, status);
        return ResponseEntity.ok(pageTaskResponse);
    }

    @Override
    public ResponseEntity<TaskResponse> getTaskById(BigInteger taskId) {
        TaskResponse taskResponse = taskService.getTaskById(taskId);
        return ResponseEntity.ok(taskResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') || hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponse> postCancelTask(BigInteger taskId) {
        AuthenticatedUser user = authenticatedUserContext.getAuthenticatedUser();
        TaskResponse taskResponse = taskService.cancelTask(taskId, user.id());
        return ResponseEntity.ok(taskResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') || hasRole('EXECUTOR')")
    public ResponseEntity<TaskResponse> postCompleteTask(BigInteger taskId) {
        AuthenticatedUser user = authenticatedUserContext.getAuthenticatedUser();
        TaskResponse taskResponse = taskService.completeTask(taskId, user.id());
        return ResponseEntity.ok(taskResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') || hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponse> postConfirmTask(BigInteger taskId) {
        AuthenticatedUser user = authenticatedUserContext.getAuthenticatedUser();
        TaskResponse taskResponse = taskService.confirmTask(taskId, user.id());
        return ResponseEntity.ok(taskResponse);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') || hasRole('CUSTOMER')")
    public ResponseEntity<TaskResponse> postCreateTask(CreateTaskRequest createTaskRequest) {
        AuthenticatedUser user = authenticatedUserContext.getAuthenticatedUser();
        TaskResponse taskResponse = taskService.createTask(user.id(), createTaskRequest);
        return ResponseEntity.ok(taskResponse);
    }
}
