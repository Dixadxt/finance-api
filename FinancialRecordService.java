package com.example.financeapi.service;

import com.example.financeapi.dto.Dtos.*;
import com.example.financeapi.exception.ResourceNotFoundException;
import com.example.financeapi.model.FinancialRecord;
import com.example.financeapi.model.User;
import com.example.financeapi.repository.FinancialRecordRepository;
import com.example.financeapi.repository.FinancialRecordSpecification;
import com.example.financeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public List<RecordResponse> getRecords(FinancialRecord.RecordType type,
                                            String category,
                                            LocalDate from,
                                            LocalDate to) {
        return recordRepository
                .findAll(FinancialRecordSpecification.withFilters(type, category, from, to))
                .stream()
                .map(RecordResponse::from)
                .collect(Collectors.toList());
    }

    public RecordResponse getRecordById(Long id) {
        return RecordResponse.from(findActiveRecord(id));
    }

    @Transactional
    public RecordResponse createRecord(CreateRecordRequest request) {
        User currentUser = getCurrentUser();
        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();
        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional
    public RecordResponse updateRecord(Long id, UpdateRecordRequest request) {
        FinancialRecord record = findActiveRecord(id);

        if (request.getAmount() != null) record.setAmount(request.getAmount());
        if (request.getType() != null) record.setType(request.getType());
        if (request.getCategory() != null) record.setCategory(request.getCategory().trim());
        if (request.getDate() != null) record.setDate(request.getDate());
        if (request.getNotes() != null) record.setNotes(request.getNotes());

        return RecordResponse.from(recordRepository.save(record));
    }

    @Transactional
    public void deleteRecord(Long id) {
        FinancialRecord record = findActiveRecord(id);
        record.setDeleted(true); // soft delete
        recordRepository.save(record);
    }

    private FinancialRecord findActiveRecord(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found with id: " + id));
        if (record.isDeleted()) {
            throw new ResourceNotFoundException("Record not found with id: " + id);
        }
        return record;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
