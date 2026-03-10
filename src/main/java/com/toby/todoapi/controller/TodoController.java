package com.toby.todoapi.controller;

import com.toby.todoapi.dto.TodoRequest;
import com.toby.todoapi.dto.TodoResponse;
import com.toby.todoapi.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/todos")
@SecurityRequirement(name = "bearerAuth")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping
    @Operation(summary = "Create New Task for user")
    public TodoResponse createTodo(@Valid @RequestBody TodoRequest request) {
        return todoService.createTodo(request);
    }

    @GetMapping
    @Operation(summary = "Get Todo list")
    public Page<TodoResponse> getTodos(
            @RequestParam(required = false) Boolean completed,
            @PageableDefault(size = 5, sort = "id") Pageable pageable
    ) {
        return todoService.getAllTodos(completed, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Todo base on task id")
    public TodoResponse getTodo(@PathVariable Long id) {
        return todoService.getTodoById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Todo base on task id")
    public TodoResponse updateTodo(@PathVariable Long id, @Valid @RequestBody TodoRequest request) {
        return todoService.updateTodo(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Todo base on task id")
    public void deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
    }
}