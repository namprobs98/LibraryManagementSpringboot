package com.librarymanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.librarymanagement.dto.ApiResponse;
import com.librarymanagement.service.StorageMode;
import com.librarymanagement.service.services.StorageService;

/**
 * StorageController - REST API để xem và chuyển đổi storage mode.
 * Tương ứng với phần chooseStorage() trong ConsoleRunner.
 *
 * Base URL: /api/storage
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * GET /api/storage/mode - Lấy storage mode hiện tại
     */
    @GetMapping("/mode")
    public ResponseEntity<ApiResponse<StorageMode>> getCurrentMode() {
        return ResponseEntity.ok(ApiResponse.ok(storageService.getCurrentMode()));
    }

    /**
     * POST /api/storage/switch?mode=TXT - Chuyển storage mode
     * Các giá trị hợp lệ: DATABASE | MEMORY | TXT | EXCEL
     */
    @PostMapping("/switch")
    public ResponseEntity<ApiResponse<StorageMode>> switchMode(@RequestParam String mode) {
        try {
            StorageMode storageMode = StorageMode.valueOf(mode.toUpperCase());
            storageService.switchMode(storageMode);
            return ResponseEntity.ok(ApiResponse.ok(
                    "Storage mode switched to: " + storageMode, storageMode));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid mode: " + mode
                            + ". Valid values: DATABASE, MEMORY, TXT, EXCEL"));
        }
    }

    /**
     * POST /api/storage/persist - Kích hoạt persist thủ công ra TXT/Excel
     */
    @PostMapping("/persist")
    public ResponseEntity<ApiResponse<Void>> persistNow() {
        storageService.persistCurrentIfNeeded();
        return ResponseEntity.ok(ApiResponse.ok(
                "Persisted to: " + storageService.getCurrentMode(), null));
    }
}
