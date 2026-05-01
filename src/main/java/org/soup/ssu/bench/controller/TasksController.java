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
    public ResponseEntity<PageTaskResponse> tasksGet(Integer page, Integer size, TaskStatusEnum status) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> tasksPost(CreateTaskRequest createTaskRequest) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> tasksTaskIdCancelPost(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> tasksTaskIdCompletePost(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> tasksTaskIdConfirmPost(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> tasksTaskIdGet(BigInteger taskId) {
        return null;
    }

    @Override
    public ResponseEntity<TaskResponse> tasksTaskIdPublishPost(BigInteger taskId) {
        return null;
    }
}
