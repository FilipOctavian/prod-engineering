package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.model.UserEntity;
import ro.unibuc.prodeng.repository.UserRepository;
import ro.unibuc.prodeng.request.CreateUserRequest;
import ro.unibuc.prodeng.response.UserResponse;
import ro.unibuc.prodeng.exception.EntityNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetAllUsers_withMultipleUsers_returnsAllUsers() {
        // Arrange
        List<UserEntity> users = Arrays.asList(
                new UserEntity("1", "Alice", "alice@example.com"),
                new UserEntity("2", "Bob", "bob@example.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).name());
        assertEquals("Bob", result.get(1).name());
    }

    @Test
    void testGetUserById_existingUserRequested_returnsUser() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.getUserById("1");

        // Assert
        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
    }

    @Test
    void testGetUserById_nonExistingUserRequested_throwsEntityNotFoundException() {
        // Arrange
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById("999"));
    }

    @Test
    void testCreateUser_newUserWithValidData_createsAndReturnsUser() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("Alice", "alice@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            // Simulate MongoDB generating an ID for new entities
            String id = "generated-id-123";
            return new UserEntity(id, entity.name(), entity.email());
        });

        // Act
        UserResponse result = userService.createUser(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testChangeName_existingUserRequested_changesNameSuccessfully() throws EntityNotFoundException {
        // Arrange
        UserEntity existing = new UserEntity("1", "Alice", "alice@example.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            // Simulate MongoDB generating an ID for new entities
            String id = entity.id() == null ? "generated-id-123" : entity.id();
            return new UserEntity(id, entity.name(), entity.email());
        });

        // Act
        UserResponse result = userService.changeName("1", "Alicia");

        // Assert
        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("Alicia", result.name());
        assertEquals("alice@example.com", result.email());
    }

    @Test
    void testChangeName_nonExistingUserRequested_throwsEntityNotFoundException() {
        // Arrange
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.changeName("999", "NewName"));
    }

    @Test
    void testDeleteUser_existingUserRequested_deletesSuccessfully() throws EntityNotFoundException {
        // Arrange
        when(userRepository.existsById("1")).thenReturn(true);

        // Act
        userService.deleteUser("1");

        // Assert
        verify(userRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteUser_nonExistingUserRequested_throwsEntityNotFoundException() {
        // Arrange
        when(userRepository.existsById("999")).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser("999"));
    }

    @Test
    void testCreateUser_duplicateEmail_throwsIllegalArgumentException() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("Alice", "alice@example.com");
        when(userRepository.findByEmail("alice@example.com"))
                .thenReturn(Optional.of(new UserEntity("1", "Alice", "alice@example.com")));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void testGetUserByEmail_existingUserRequested_returnsUser() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        // Act
        UserResponse result = userService.getUserByEmail("alice@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("Alice", result.name());
        assertEquals("alice@example.com", result.email());
    }

    @Test
    void testGetUserByEmail_nonExistingUserRequested_throwsEntityNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.getUserByEmail("ghost@example.com"));
    }

    @Test
    void testGetUserEntityById_existingUserRequested_returnsUserEntity() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com");
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userService.getUserEntityById("1");

        // Assert
        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("Alice", result.name());
    }

    @Test
    void testGetUserEntityByEmail_existingUserRequested_returnsUserEntity() throws EntityNotFoundException {
        // Arrange
        UserEntity user = new UserEntity("1", "Alice", "alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        // Act
        UserEntity result = userService.getUserEntityByEmail("alice@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("alice@example.com", result.email());
    }
}
