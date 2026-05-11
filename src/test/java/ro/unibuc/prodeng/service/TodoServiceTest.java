package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.TodoEntity;
import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.TodoRepository;
import ro.unibuc.prodeng.request.AssignTodoRequest;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.EditTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TodoService todoService;

    @Test
    void getTodosByUserEmail_whenUserExists_returnsMappedTodos() {
        UserEntity user = new UserEntity("user-1", "Ana", "ana@example.com");
        when(userService.getUserEntityByEmail("ana@example.com")).thenReturn(user);
        when(todoRepository.findByAssignedUserId("user-1")).thenReturn(List.of(
                new TodoEntity("todo-1", "Task 1", false, "user-1"),
                new TodoEntity("todo-2", "Task 2", true, "user-1")
        ));

        List<TodoResponse> result = todoService.getTodosByUserEmail("ana@example.com");

        assertEquals(2, result.size());
        assertEquals("Ana", result.getFirst().assigneeName());
    }

    @Test
    void getTodoById_whenTodoExists_returnsMappedTodo() {
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(new TodoEntity("todo-1", "Task 1", false, "user-1")));
        when(userService.getUserEntityById("user-1")).thenReturn(new UserEntity("user-1", "Ana", "ana@example.com"));

        TodoResponse result = todoService.getTodoById("todo-1");

        assertEquals("todo-1", result.id());
        assertEquals("ana@example.com", result.assigneeEmail());
    }

    @Test
    void createTodo_whenAssigneeExists_savesTodo() {
        CreateTodoRequest request = new CreateTodoRequest("New Task", "ana@example.com");
        when(userService.getUserEntityByEmail("ana@example.com")).thenReturn(new UserEntity("user-1", "Ana", "ana@example.com"));
        when(todoRepository.save(any(TodoEntity.class))).thenReturn(new TodoEntity("todo-1", "New Task", false, "user-1"));

        TodoResponse result = todoService.createTodo(request);

        assertEquals("todo-1", result.id());
        assertEquals(false, result.done());
    }

    @Test
    void setDone_whenTodoExists_updatesStatus() {
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(new TodoEntity("todo-1", "Task 1", false, "user-1")));
        when(todoRepository.save(any(TodoEntity.class))).thenReturn(new TodoEntity("todo-1", "Task 1", true, "user-1"));
        when(userService.getUserEntityById("user-1")).thenReturn(new UserEntity("user-1", "Ana", "ana@example.com"));

        TodoResponse result = todoService.setDone("todo-1", true);

        assertEquals(true, result.done());
    }

    @Test
    void assign_whenNewAssigneeExists_updatesTodoAssignee() {
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(new TodoEntity("todo-1", "Task 1", false, "user-1")));
        when(userService.getUserEntityByEmail("bob@example.com")).thenReturn(new UserEntity("user-2", "Bob", "bob@example.com"));
        when(todoRepository.save(any(TodoEntity.class))).thenReturn(new TodoEntity("todo-1", "Task 1", false, "user-2"));

        TodoResponse result = todoService.assign("todo-1", new AssignTodoRequest("bob@example.com"));

        assertEquals("Bob", result.assigneeName());
        assertEquals("bob@example.com", result.assigneeEmail());
    }

    @Test
    void edit_whenTodoExists_updatesDescription() {
        when(todoRepository.findById("todo-1")).thenReturn(Optional.of(new TodoEntity("todo-1", "Task 1", false, "user-1")));
        when(todoRepository.save(any(TodoEntity.class))).thenReturn(new TodoEntity("todo-1", "Updated Task", false, "user-1"));
        when(userService.getUserEntityById("user-1")).thenReturn(new UserEntity("user-1", "Ana", "ana@example.com"));

        TodoResponse result = todoService.edit("todo-1", new EditTodoRequest("Updated Task"));

        assertEquals("Updated Task", result.description());
    }

    @Test
    void deleteTodo_whenTodoExists_deletesTodo() {
        when(todoRepository.existsById("todo-1")).thenReturn(true);

        todoService.deleteTodo("todo-1");

        verify(todoRepository).deleteById("todo-1");
    }

    @Test
    void deleteTodo_whenTodoDoesNotExist_throwsException() {
        when(todoRepository.existsById("todo-404")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> todoService.deleteTodo("todo-404"));
    }
}
