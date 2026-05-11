package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.CarEntity;
import ro.unibuc.prodeng.model.ClientEntity;
import ro.unibuc.prodeng.model.MechanicEntity;
import ro.unibuc.prodeng.model.PartEntity;
import ro.unibuc.prodeng.model.SupplierEntity;
import ro.unibuc.prodeng.repository.CarRepository;
import ro.unibuc.prodeng.repository.ClientRepository;
import ro.unibuc.prodeng.repository.MechanicRepository;
import ro.unibuc.prodeng.repository.PartRepository;
import ro.unibuc.prodeng.repository.SupplierRepository;
import ro.unibuc.prodeng.request.CreateCarRequest;
import ro.unibuc.prodeng.request.CreateClientRequest;
import ro.unibuc.prodeng.request.CreateMechanicRequest;
import ro.unibuc.prodeng.request.CreatePartRequest;
import ro.unibuc.prodeng.request.CreateSupplierRequest;

@ExtendWith(MockitoExtension.class)
class AutoServiceCatalogServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private MechanicRepository mechanicRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private PartRepository partRepository;

    @InjectMocks
    private AutoServiceCatalogService autoServiceCatalogService;

    @Test
    void createClient_whenEmailIsUnique_savesClient() {
        CreateClientRequest request = new CreateClientRequest("Ana", "Popescu", "0711", "ana@example.com", "Bucharest");
        ClientEntity savedClient = new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest");

        when(clientRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());
        when(clientRepository.save(any(ClientEntity.class))).thenReturn(savedClient);

        ClientEntity result = autoServiceCatalogService.createClient(request);

        assertSame(savedClient, result);
        verify(clientRepository).save(any(ClientEntity.class));
    }

    @Test
    void createClient_whenEmailAlreadyExists_throwsException() {
        CreateClientRequest request = new CreateClientRequest("Ana", "Popescu", "0711", "ana@example.com", "Bucharest");
        when(clientRepository.findByEmail("ana@example.com"))
                .thenReturn(Optional.of(new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest")));

        assertThrows(IllegalArgumentException.class, () -> autoServiceCatalogService.createClient(request));
    }

    @Test
    void createCar_whenClientExistsAndPlateIsUnique_savesCar() {
        CreateCarRequest request = new CreateCarRequest("Dacia", "Logan", 2022, "B-10-ABC", "client-1");
        CarEntity savedCar = new CarEntity("car-1", "Dacia", "Logan", 2022, "B-10-ABC", "client-1");

        when(clientRepository.findById("client-1")).thenReturn(Optional.of(new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest")));
        when(carRepository.findByPlateNumber("B-10-ABC")).thenReturn(Optional.empty());
        when(carRepository.save(any(CarEntity.class))).thenReturn(savedCar);

        CarEntity result = autoServiceCatalogService.createCar(request);

        assertSame(savedCar, result);
    }

    @Test
    void createCar_whenPlateAlreadyExists_throwsException() {
        CreateCarRequest request = new CreateCarRequest("Dacia", "Logan", 2022, "B-10-ABC", "client-1");

        when(clientRepository.findById("client-1")).thenReturn(Optional.of(new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest")));
        when(carRepository.findByPlateNumber("B-10-ABC"))
                .thenReturn(Optional.of(new CarEntity("car-1", "Dacia", "Logan", 2022, "B-10-ABC", "client-1")));

        assertThrows(IllegalArgumentException.class, () -> autoServiceCatalogService.createCar(request));
    }

    @Test
    void getCarsByClientId_whenClientExists_returnsCars() {
        CarEntity firstCar = new CarEntity("car-1", "Dacia", "Logan", 2022, "B-10-ABC", "client-1");
        CarEntity secondCar = new CarEntity("car-2", "Ford", "Focus", 2019, "B-20-XYZ", "client-1");

        when(clientRepository.findById("client-1")).thenReturn(Optional.of(new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest")));
        when(carRepository.findByClientId("client-1")).thenReturn(List.of(firstCar, secondCar));

        List<CarEntity> result = autoServiceCatalogService.getCarsByClientId("client-1");

        assertEquals(2, result.size());
        assertSame(firstCar, result.get(0));
    }

    @Test
    void createMechanic_savesMechanic() {
        CreateMechanicRequest request = new CreateMechanicRequest("Ion", "Ionescu", "0722");
        MechanicEntity savedMechanic = new MechanicEntity("mech-1", "Ion", "Ionescu", "0722", 0.0, 0);

        when(mechanicRepository.save(any(MechanicEntity.class))).thenReturn(savedMechanic);

        MechanicEntity result = autoServiceCatalogService.createMechanic(request);

        assertSame(savedMechanic, result);
    }

    @Test
    void createSupplier_savesSupplier() {
        CreateSupplierRequest request = new CreateSupplierRequest("Auto Parts", "Bucharest", "021");
        SupplierEntity savedSupplier = new SupplierEntity("sup-1", "Auto Parts", "Bucharest", "021");

        when(supplierRepository.save(any(SupplierEntity.class))).thenReturn(savedSupplier);

        SupplierEntity result = autoServiceCatalogService.createSupplier(request);

        assertSame(savedSupplier, result);
    }

    @Test
    void createPart_whenSupplierExists_savesPart() {
        CreatePartRequest request = new CreatePartRequest("Filter", 10, BigDecimal.valueOf(30), "sup-1");
        PartEntity savedPart = new PartEntity("part-1", "Filter", 10, BigDecimal.valueOf(30), "sup-1");

        when(supplierRepository.findById("sup-1")).thenReturn(Optional.of(new SupplierEntity("sup-1", "Auto Parts", "Bucharest", "021")));
        when(partRepository.save(any(PartEntity.class))).thenReturn(savedPart);

        PartEntity result = autoServiceCatalogService.createPart(request);

        assertSame(savedPart, result);
    }

    @Test
    void ensurePartExists_whenMissing_throwsException() {
        when(partRepository.findById("part-404")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> autoServiceCatalogService.ensurePartExists("part-404"));
    }

    @Test
    void getAllMethods_returnRepositoryData() {
        when(clientRepository.findAll()).thenReturn(List.of(new ClientEntity("client-1", "Ana", "Popescu", "0711", "ana@example.com", "Bucharest")));
        when(mechanicRepository.findAll()).thenReturn(List.of(new MechanicEntity("mech-1", "Ion", "Ionescu", "0722", 0.0, 0)));
        when(supplierRepository.findAll()).thenReturn(List.of(new SupplierEntity("sup-1", "Auto Parts", "Bucharest", "021")));
        when(partRepository.findAll()).thenReturn(List.of(new PartEntity("part-1", "Filter", 10, BigDecimal.ONE, "sup-1")));

        assertEquals(1, autoServiceCatalogService.getAllClients().size());
        assertEquals(1, autoServiceCatalogService.getAllMechanics().size());
        assertEquals(1, autoServiceCatalogService.getAllSuppliers().size());
        assertEquals(1, autoServiceCatalogService.getAllParts().size());
    }
}
