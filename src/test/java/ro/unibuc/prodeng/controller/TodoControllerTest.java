package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ro.unibuc.prodeng.request.AssignTodoRequest;
import ro.unibuc.prodeng.request.CreateTodoRequest;
import ro.unibuc.prodeng.request.EditTodoRequest;
import ro.unibuc.prodeng.response.TodoResponse;
import ro.unibuc.prodeng.service.TodoService;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(todoController).build();
    }

    @Test
    void getTodosByUserEmail_returnsTodos() throws Exception {
        when(todoService.getTodosByUserEmail("ana@example.com")).thenReturn(List.of(
                new TodoResponse("todo-1", "Task 1", false, "Ana", "ana@example.com")));

        mockMvc.perform(get("/api/todos").param("assigneeEmail", "ana@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getTodoById_returnsTodo() throws Exception {
        when(todoService.getTodoById("todo-1")).thenReturn(new TodoResponse("todo-1", "Task 1", false, "Ana", "ana@example.com"));

        mockMvc.perform(get("/api/todos/todo-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("todo-1")));
    }

    @Test
    void createTodo_returnsCreatedTodo() throws Exception {
        CreateTodoRequest request = new CreateTodoRequest("Task 1", "ana@example.com");
        when(todoService.createTodo(any(CreateTodoRequest.class))).thenReturn(new TodoResponse("todo-1", "Task 1", false, "Ana", "ana@example.com"));

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("todo-1")));
    }

    @Test
    void setDone_returnsUpdatedTodo() throws Exception {
        when(todoService.setDone("todo-1", true)).thenReturn(new TodoResponse("todo-1", "Task 1", true, "Ana", "ana@example.com"));

        mockMvc.perform(patch("/api/todos/todo-1/done")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done", is(true)));
    }

    @Test
    void assign_returnsUpdatedTodo() throws Exception {
        AssignTodoRequest request = new AssignTodoRequest("bob@example.com");
        when(todoService.assign(any(String.class), any(AssignTodoRequest.class)))
                .thenReturn(new TodoResponse("todo-1", "Task 1", false, "Bob", "bob@example.com"));

        mockMvc.perform(patch("/api/todos/todo-1/assignee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeName", is("Bob")));
    }

    @Test
    void edit_returnsUpdatedTodo() throws Exception {
        EditTodoRequest request = new EditTodoRequest("Updated Task");
        when(todoService.edit(any(String.class), any(EditTodoRequest.class)))
                .thenReturn(new TodoResponse("todo-1", "Updated Task", false, "Ana", "ana@example.com"));

        mockMvc.perform(patch("/api/todos/todo-1/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated Task")));
    }

    @Test
    void deleteTodo_returnsNoContent() throws Exception {
        doNothing().when(todoService).deleteTodo("todo-1");

        mockMvc.perform(delete("/api/todos/todo-1"))
                .andExpect(status().isNoContent());
    }
}
