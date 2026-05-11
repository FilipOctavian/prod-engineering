package ro.unibuc.prodeng.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class AutoServiceCatalogService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MechanicRepository mechanicRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PartRepository partRepository;

    public ClientEntity createClient(CreateClientRequest request) {
        clientRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new IllegalArgumentException("Client with email already exists: " + request.email());
        });

        return clientRepository.save(new ClientEntity(
                null,
                request.firstName(),
                request.lastName(),
                request.phone(),
                request.email(),
                request.address()
        ));
    }

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public CarEntity createCar(CreateCarRequest request) {
        ensureClientExists(request.clientId());
        carRepository.findByPlateNumber(request.plateNumber()).ifPresent(existing -> {
            throw new IllegalArgumentException("Car with plate number already exists: " + request.plateNumber());
        });

        return carRepository.save(new CarEntity(
                null,
                request.brand(),
                request.model(),
                request.fabricationYear(),
                request.plateNumber(),
                request.clientId()
        ));
    }

    public List<CarEntity> getCarsByClientId(String clientId) {
        ensureClientExists(clientId);
        return carRepository.findByClientId(clientId);
    }

    public MechanicEntity createMechanic(CreateMechanicRequest request) {
        return mechanicRepository.save(new MechanicEntity(
                null,
                request.firstName(),
                request.lastName(),
                request.phone(),
                0.0,
                0
        ));
    }

    public List<MechanicEntity> getAllMechanics() {
        return mechanicRepository.findAll();
    }

    public SupplierEntity createSupplier(CreateSupplierRequest request) {
        return supplierRepository.save(new SupplierEntity(
                null,
                request.name(),
                request.address(),
                request.phone()
        ));
    }

    public List<SupplierEntity> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public PartEntity createPart(CreatePartRequest request) {
        ensureSupplierExists(request.supplierId());
        return partRepository.save(new PartEntity(
                null,
                request.name(),
                request.availableStock(),
                request.unitPrice(),
                request.supplierId()
        ));
    }

    public List<PartEntity> getAllParts() {
        return partRepository.findAll();
    }

    public ClientEntity ensureClientExists(String clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client " + clientId));
    }

    public CarEntity ensureCarExists(String carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car " + carId));
    }

    public MechanicEntity ensureMechanicExists(String mechanicId) {
        return mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new EntityNotFoundException("Mechanic " + mechanicId));
    }

    public SupplierEntity ensureSupplierExists(String supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new EntityNotFoundException("Supplier " + supplierId));
    }

    public PartEntity ensurePartExists(String partId) {
        return partRepository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException("Part " + partId));
    }
}
