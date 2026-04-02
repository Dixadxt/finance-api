package com.example.financeapi.controller;

import com.example.financeapi.dto.Dtos.*;
import com.example.financeapi.model.FinancialRecord;
import com.example.financeapi.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    // GET /api/records?type=INCOME&category=rent&from=2025-01-01&to=2025-12-31
    @GetMapping
    public ResponseEntity<List<RecordResponse>> getRecords(
            @RequestParam(required = false) FinancialRecord.RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(recordService.getRecords(type, category, from, to));
    }

    // GET /api/records/{id}
    @GetMapping("/{id}")
    public ResponseEntity<RecordResponse> getRecord(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.getRecordById(id));
    }

    // POST /api/records — ANALYST or ADMIN
    @PostMapping
    public ResponseEntity<RecordResponse> createRecord(@Valid @RequestBody CreateRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.createRecord(request));
    }

    // PUT /api/records/{id} — ANALYST or ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<RecordResponse> updateRecord(@PathVariable Long id,
                                                        @RequestBody UpdateRecordRequest request) {
        return ResponseEntity.ok(recordService.updateRecord(id, request));
    }

    // DELETE /api/records/{id} — ADMIN only (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
