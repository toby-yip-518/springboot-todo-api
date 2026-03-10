package com.toby.todoapi.service;

import com.toby.todoapi.dto.TodoRequest;
import com.toby.todoapi.dto.TodoResponse;
import com.toby.todoapi.exception.ResourceNotFoundException;
import com.toby.todoapi.model.Todo;
import com.toby.todoapi.model.User;
import com.toby.todoapi.repository.TodoRepository;
import com.toby.todoapi.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTodoSuccess() {
        mockAuthenticatedUser("john@doe.com");

        User currentUser = new User(1L, "John Doe", "john@doe.com", "hashedPassword");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(currentUser));

        TodoRequest request = new TodoRequest();
        request.setTitle("Learn testing");
        request.setCompleted(false);

        Todo savedTodo = new Todo(1L, "Learn testing", false, currentUser);
        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        TodoResponse response = todoService.createTodo(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Learn testing", response.getTitle());
        assertFalse(response.isCompleted());

        verify(userRepository).findByEmail("john@doe.com");
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    void getAllTodosWithoutFilter() {
        mockAuthenticatedUser("john@doe.com");

        User currentUser = new User(1L, "John Doe", "john@doe.com", "hashedPassword");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(currentUser));

        Pageable pageable = PageRequest.of(0, 5, Sort.by("id").ascending());

        List<Todo> todos = List.of(
                new Todo(1L, "Task 1", false, currentUser),
                new Todo(2L, "Task 2", true, currentUser)
        );

        Page<Todo> todoPage = new PageImpl<>(todos, pageable, todos.size());

        when(todoRepository.findByUser(currentUser, pageable)).thenReturn(todoPage);

        Page<TodoResponse> responsePage = todoService.getAllTodos(null, pageable);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getContent().size());
        assertEquals("Task 1", responsePage.getContent().get(0).getTitle());
        assertEquals("Task 2", responsePage.getContent().get(1).getTitle());

        verify(todoRepository).findByUser(currentUser, pageable);
        verify(todoRepository, never()).findByUserAndCompleted(any(), anyBoolean(), any());
    }

    @Test
    void getAllTodosWithCompletedFilter() {
        mockAuthenticatedUser("john@doe.com");

        User currentUser = new User(1L, "John Doe", "john@doe.com", "hashedPassword");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(currentUser));

        Pageable pageable = PageRequest.of(0, 5);

        List<Todo> completedTodos = List.of(
                new Todo(2L, "Completed Task", true, currentUser)
        );

        Page<Todo> todoPage = new PageImpl<>(completedTodos, pageable, completedTodos.size());

        when(todoRepository.findByUserAndCompleted(currentUser, true, pageable)).thenReturn(todoPage);

        Page<TodoResponse> responsePage = todoService.getAllTodos(true, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getContent().size());
        assertEquals("Completed Task", responsePage.getContent().get(0).getTitle());
        assertTrue(responsePage.getContent().get(0).isCompleted());

        verify(todoRepository).findByUserAndCompleted(currentUser, true, pageable);
        verify(todoRepository, never()).findByUser(currentUser, pageable);
    }

    @Test
    void getTodoByIdNotFound() {
        mockAuthenticatedUser("john@doe.com");

        User currentUser = new User(1L, "John Doe", "john@doe.com", "hashedPassword");
        when(userRepository.findByEmail("john@doe.com")).thenReturn(Optional.of(currentUser));

        when(todoRepository.findByIdAndUser(99L, currentUser)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> todoService.getTodoById(99L)
        );

        assertEquals("Todo with id 99 not found", exception.getMessage());

        verify(todoRepository).findByIdAndUser(99L, currentUser);
    }

    private void mockAuthenticatedUser(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }
}