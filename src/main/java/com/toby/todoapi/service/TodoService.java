package com.toby.todoapi.service;

import com.toby.todoapi.dto.TodoRequest;
import com.toby.todoapi.dto.TodoResponse;
import com.toby.todoapi.exception.ResourceNotFoundException;
import com.toby.todoapi.model.Todo;
import com.toby.todoapi.model.User;
import com.toby.todoapi.repository.TodoRepository;
import com.toby.todoapi.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

//Servic層負責: 攞所有, create, update, delete todo
@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;


    public TodoService(TodoRepository todoRepository, UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Authenticated user not found")
                );
    }

    public Page<TodoResponse> getAllTodos(Boolean completed, Pageable pageable) {
        User currentUser = getCurrentUser();

        Page<Todo> todoPage;

        if (completed == null) {
            todoPage = todoRepository.findByUser(currentUser, pageable);
        } else {
            todoPage = todoRepository.findByUserAndCompleted(currentUser, completed, pageable);
        }

        return todoPage.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.isCompleted()
        ));
    }

    public TodoResponse createTodo(TodoRequest request) {
        User currentUser = getCurrentUser();

        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setCompleted(request.isCompleted());
        todo.setUser(currentUser);

        Todo savedTodo = todoRepository.save(todo);

        return new TodoResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.isCompleted()
        );
    }

    public TodoResponse getTodoById(Long id) {
        User currentUser = getCurrentUser();

        Todo todo = todoRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Todo with id " + id + " not found")
                );

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.isCompleted()
        );
    }

    public TodoResponse updateTodo(Long id, TodoRequest request) {
        User currentUser = getCurrentUser();

        Todo todo = todoRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Todo with id " + id + " not found")
                );

        todo.setTitle(request.getTitle());
        todo.setCompleted(request.isCompleted());

        Todo updatedTodo = todoRepository.save(todo);

        return new TodoResponse(
                updatedTodo.getId(),
                updatedTodo.getTitle(),
                updatedTodo.isCompleted()
        );
    }

    public void deleteTodo(Long id) {
        User currentUser = getCurrentUser();

        Todo todo = todoRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Todo with id " + id + " not found")
                );

        todoRepository.delete(todo);
    }
}