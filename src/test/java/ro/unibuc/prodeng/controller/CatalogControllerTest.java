package ro.unibuc.prodeng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import ro.unibuc.prodeng.request.ReceiveDeliveryRequest;
import ro.unibuc.prodeng.service.AutoServiceCatalogService;
import ro.unibuc.prodeng.service.InventoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
class CatalogControllerTest {

    @Mock
    private AutoServiceCatalogService catalogService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ClientController clientController;

    @InjectMocks
    private CarController carController;

    @InjectMocks
    private MechanicController mechanicController;

    @InjectMocks
    private PartController partController;

    @InjectMocks
    private SupplierController supplierController;

    @InjectMocks
    private DeliveryController deliveryController;

    private MockMvc clientMvc;
    private MockMvc carMvc;
    private MockMvc mechanicMvc;
    private MockMvc partMvc;
    private MockMvc supplierMvc;
    private MockMvc deliveryMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        clientMvc = MockMvcBuilders.standaloneSetup(clientController).build();
        carMvc = MockMvcBuilders.standaloneSetup(carController).build();
        mechanicMvc = MockMvcBuilders.standaloneSetup(mechanicController).build();
        partMvc = MockMvcBuilders.standaloneSetup(partController).build();
        supplierMvc = MockMvcBuilders.standaloneSetup(supplierController).build();
        deliveryMvc = MockMvcBuilders.standaloneSetup(deliveryController).build();
    }

    @Test
    void testCreateClient_validRequestProvided_returnsCreatedClient() throws Exception {
        CreateClientRequest request = new CreateClientRequest("John", "Doe", "0700000000", "john@example.com", "Str. Main 1");
        ClientEntity saved = new ClientEntity("c-1", "John", "Doe", "0700000000", "john@example.com", "Str. Main 1");

        when(catalogService.createClient(any(CreateClientRequest.class))).thenReturn(saved);

        clientMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("c-1")))
                .andExpect(jsonPath("$.firstName", is("John")));

        verify(catalogService, times(1)).createClient(any(CreateClientRequest.class));
    }

    @Test
    void testGetAllClients_withClients_returnsList() throws Exception {
        List<ClientEntity> clients = Arrays.asList(
                new ClientEntity("c-1", "John", "Doe", "0700000000", "john@example.com", "Str. A")
        );
        when(catalogService.getAllClients()).thenReturn(clients);

        clientMvc.perform(get("/api/clients").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(catalogService, times(1)).getAllClients();
    }

    @Test
    void testCreateCar_validRequestProvided_returnsCreatedCar() throws Exception {
        CreateCarRequest request = new CreateCarRequest("Toyota", "Corolla", 2020, "B-123-XYZ", "c-1");
        CarEntity saved = new CarEntity("car-1", "Toyota", "Corolla", 2020, "B-123-XYZ", "c-1");

        when(catalogService.createCar(any(CreateCarRequest.class))).thenReturn(saved);

        carMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("car-1")))
                .andExpect(jsonPath("$.brand", is("Toyota")));

        verify(catalogService, times(1)).createCar(any(CreateCarRequest.class));
    }

    @Test
    void testGetCarsByClient_clientWithCars_returnsList() throws Exception {
        List<CarEntity> cars = Arrays.asList(
                new CarEntity("car-1", "Toyota", "Corolla", 2020, "B-123-XYZ", "c-1")
        );
        when(catalogService.getCarsByClientId("c-1")).thenReturn(cars);

        carMvc.perform(get("/api/cars/by-client/{clientId}", "c-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(catalogService, times(1)).getCarsByClientId("c-1");
    }

    @Test
    void testCreateMechanic_validRequestProvided_returnsCreatedMechanic() throws Exception {
        CreateMechanicRequest request = new CreateMechanicRequest("Ion", "Popescu", "0711111111");
        MechanicEntity saved = new MechanicEntity("m-1", "Ion", "Popescu", "0711111111", 0.0, 0);

        when(catalogService.createMechanic(any(CreateMechanicRequest.class))).thenReturn(saved);

        mechanicMvc.perform(post("/api/mechanics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("m-1")));

        verify(catalogService, times(1)).createMechanic(any(CreateMechanicRequest.class));
    }

    @Test
    void testGetAllMechanics_withMechanics_returnsList() throws Exception {
        List<MechanicEntity> mechanics = Arrays.asList(
                new MechanicEntity("m-1", "Ion", "Popescu", "0711111111", 4.5, 2)
        );
        when(catalogService.getAllMechanics()).thenReturn(mechanics);

        mechanicMvc.perform(get("/api/mechanics").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(catalogService, times(1)).getAllMechanics();
    }

    @Test
    void testCreatePart_validRequestProvided_returnsCreatedPart() throws Exception {
        CreatePartRequest request = new CreatePartRequest("Oil Filter", 50, BigDecimal.valueOf(12), "sup-1");
        PartEntity saved = new PartEntity("p-1", "Oil Filter", 50, BigDecimal.valueOf(12), "sup-1");

        when(catalogService.createPart(any(CreatePartRequest.class))).thenReturn(saved);

        partMvc.perform(post("/api/parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("p-1")));

        verify(catalogService, times(1)).createPart(any(CreatePartRequest.class));
    }

    @Test
    void testGetAllParts_withParts_returnsList() throws Exception {
        List<PartEntity> parts = Arrays.asList(
                new PartEntity("p-1", "Oil Filter", 50, BigDecimal.valueOf(12), "sup-1")
        );
        when(catalogService.getAllParts()).thenReturn(parts);

        partMvc.perform(get("/api/parts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(catalogService, times(1)).getAllParts();
    }

    @Test
    void testCreateSupplier_validRequestProvided_returnsCreatedSupplier() throws Exception {
        CreateSupplierRequest request = new CreateSupplierRequest("AutoParts SRL", "Str. Industriei 5", "0733333333");
        SupplierEntity saved = new SupplierEntity("s-1", "AutoParts SRL", "Str. Industriei 5", "0733333333");

        when(catalogService.createSupplier(any(CreateSupplierRequest.class))).thenReturn(saved);

        supplierMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("s-1")));

        verify(catalogService, times(1)).createSupplier(any(CreateSupplierRequest.class));
    }

    @Test
    void testGetAllSuppliers_withSuppliers_returnsList() throws Exception {
        List<SupplierEntity> suppliers = Arrays.asList(
                new SupplierEntity("s-1", "AutoParts SRL", "Str. Industriei 5", "0733333333")
        );
        when(catalogService.getAllSuppliers()).thenReturn(suppliers);

        supplierMvc.perform(get("/api/suppliers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(catalogService, times(1)).getAllSuppliers();
    }

    @Test
    void testReceiveDelivery_validRequestProvided_returnsUpdatedPart() throws Exception {
        ReceiveDeliveryRequest request = new ReceiveDeliveryRequest("p-1", "s-1", 10, LocalDateTime.now());
        PartEntity updated = new PartEntity("p-1", "Oil Filter", 60, BigDecimal.valueOf(12), "s-1");

        when(inventoryService.receiveDelivery(any(ReceiveDeliveryRequest.class))).thenReturn(updated);

        deliveryMvc.perform(patch("/api/deliveries/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableStock", is(60)));

        verify(inventoryService, times(1)).receiveDelivery(any(ReceiveDeliveryRequest.class));
    }
}
