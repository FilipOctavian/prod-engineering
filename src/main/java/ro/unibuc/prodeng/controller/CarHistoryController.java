package ro.unibuc.prodeng.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.unibuc.prodeng.response.CarHistoryResponse;
import ro.unibuc.prodeng.response.CarHistorySummaryResponse;
import ro.unibuc.prodeng.service.CarHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/car-history")
public class CarHistoryController {

    private final CarHistoryService carHistoryService;

    public CarHistoryController(CarHistoryService carHistoryService) {
        this.carHistoryService = carHistoryService;
    }

    @GetMapping("/{carId}")
    public ResponseEntity<List<CarHistoryResponse>> getAllOrders(@PathVariable String carId) {
        return ResponseEntity.ok(carHistoryService.getAllOrders(carId));
    }

    @GetMapping("/{carId}/summary")
    public ResponseEntity<CarHistorySummaryResponse> getSummary(@PathVariable String carId) {
        return ResponseEntity.ok(carHistoryService.getSummary(carId));
    }

    @GetMapping("/{carId}/completed")
    public ResponseEntity<List<CarHistoryResponse>> getCompletedOrders(@PathVariable String carId) {
        return ResponseEntity.ok(carHistoryService.getCompletedOrders(carId));
    }

    @GetMapping("/{carId}/active")
    public ResponseEntity<CarHistoryResponse> getActiveOrder(@PathVariable String carId) {
        return ResponseEntity.ok(carHistoryService.getActiveOrder(carId));
    }
}
