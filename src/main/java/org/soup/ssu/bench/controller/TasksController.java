package org.soup.ssu.bench.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ssu.bench.endpoint.TasksApi;
import ssu.bench.model.CreateTaskRequest;
import ssu.bench.model.PageTaskResponse;
import ssu.bench.model.TaskResponse;
import ssu.bench.model.TaskStatusEnum;

import java.math.BigInteger;

@RestController
public class TasksController implements TasksApi {
    @Override
    public ResponseEntity<PageTaskResponse> getListTasks(Integer page, Integer size, TaskStatusEnum status) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> getTaskById(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> postCancelTask(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> postCompleteTask(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> postConfirmTask(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> postCreateTask(CreateTaskRequest createTaskRequest) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> postPublishTask(BigInteger taskId) {
        return null;
    }
}
