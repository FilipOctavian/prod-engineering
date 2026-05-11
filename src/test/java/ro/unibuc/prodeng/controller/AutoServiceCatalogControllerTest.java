package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import ro.unibuc.prodeng.model.CarEntity;
import ro.unibuc.prodeng.model.ClientEntity;
import ro.unibuc.prodeng.model.MechanicEntity;
import ro.unibuc.prodeng.model.PartEntity;
import ro.unibuc.prodeng.model.SupplierEntity;
import ro.unibuc.prodeng.request.CreateCarRequest;
import ro.unibuc.prodeng.request.CreateClientRequest;
import ro.unibuc.prodeng.request.CreateMechanicRequest;
import ro.unibuc.prodeng.request.CreatePartRequest;
import ro.unibuc.prodeng.request.CreateSupplierRequest;
import ro.unibuc.prodeng.service.AutoServiceCatalogService;

@ExtendWith(MockitoExtension.class)
class AutoServiceCatalogControllerTest {

    @Mock
    private AutoServiceCatalogService catalogService;

    @InjectMocks
    private ClientController clientController;

    @InjectMocks
    private CarController carController;

    @InjectMocks
    private MechanicController mechanicController;

    @InjectMocks
    private SupplierController supplierController;

    @InjectMocks
    private PartController partController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                clientController,
                carController,
                mechanicController,
                supplierController,
                partController
        ).build();
    }

    @Test
    void createClient_returnsCreatedClient() throws Exception {
        CreateClientRequest request = new CreateClientRequest("Ana", "Popescu", "0711", "ana@example.com", "Bucharest");
        ClientEntity response = new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest");

        when(catalogService.createClient(any(CreateClientRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("client-1")))
                .andExpect(jsonPath("$.email", is("ana@example.com")));

        verify(catalogService).createClient(any(CreateClientRequest.class));
    }

    @Test
    void getAllClients_returnsClients() throws Exception {
        when(catalogService.getAllClients()).thenReturn(List.of(
                new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest")));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createCar_returnsCreatedCar() throws Exception {
        CreateCarRequest request = new CreateCarRequest("Dacia", "Logan", 2022, "B-10-ABC", "client-1");
        CarEntity response = new CarEntity("car-1", "Dacia", "Logan", 2022, "B-10-ABC", "client-1");

        when(catalogService.createCar(any(CreateCarRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("car-1")))
                .andExpect(jsonPath("$.plateNumber", is("B-10-ABC")));
    }

    @Test
    void getCarsByClient_returnsCars() throws Exception {
        when(catalogService.getCarsByClientId("client-1")).thenReturn(List.of(
                new CarEntity("car-1", "Dacia", "Logan", 2022, "B-10-ABC", "client-1")));

        mockMvc.perform(get("/api/cars/by-client/client-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createMechanic_returnsCreatedMechanic() throws Exception {
        CreateMechanicRequest request = new CreateMechanicRequest("Ion", "Ionescu", "0722");
        MechanicEntity response = new MechanicEntity("mech-1", "Ion", "Ionescu", "0722");

        when(catalogService.createMechanic(any(CreateMechanicRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/mechanics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("mech-1")));
    }

    @Test
    void getAllMechanics_returnsMechanics() throws Exception {
        when(catalogService.getAllMechanics()).thenReturn(List.of(
                new MechanicEntity("mech-1", "Ion", "Ionescu", "0722")));

        mockMvc.perform(get("/api/mechanics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createSupplier_returnsCreatedSupplier() throws Exception {
        CreateSupplierRequest request = new CreateSupplierRequest("Auto Parts", "Bucharest", "021");
        SupplierEntity response = new SupplierEntity("sup-1", "Auto Parts", "Bucharest", "021");

        when(catalogService.createSupplier(any(CreateSupplierRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("sup-1")));
    }

    @Test
    void getAllSuppliers_returnsSuppliers() throws Exception {
        when(catalogService.getAllSuppliers()).thenReturn(List.of(
                new SupplierEntity("sup-1", "Auto Parts", "Bucharest", "021")));

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createPart_returnsCreatedPart() throws Exception {
        CreatePartRequest request = new CreatePartRequest("Filter", 10, BigDecimal.valueOf(30), "sup-1");
        PartEntity response = new PartEntity("part-1", "Filter", 10, BigDecimal.valueOf(30), "sup-1");

        when(catalogService.createPart(any(CreatePartRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("part-1")));
    }

    @Test
    void getAllParts_returnsParts() throws Exception {
        when(catalogService.getAllParts()).thenReturn(List.of(
                new PartEntity("part-1", "Filter", 10, BigDecimal.valueOf(30), "sup-1")));

        mockMvc.perform(get("/api/parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
