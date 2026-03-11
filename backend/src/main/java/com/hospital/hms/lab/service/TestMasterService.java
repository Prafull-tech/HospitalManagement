package com.hospital.hms.lab.service;

import com.hospital.hms.common.exception.ResourceNotFoundException;
import com.hospital.hms.lab.dto.TestMasterRequestDto;
import com.hospital.hms.lab.dto.TestMasterResponseDto;
import com.hospital.hms.lab.entity.TestMaster;
import com.hospital.hms.lab.exception.DuplicateTestCodeException;
import com.hospital.hms.lab.repository.TestMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for TestMaster CRUD operations, panel expansion, and duplicate code validation.
 */
@Service
public class TestMasterService {

    private final TestMasterRepository testMasterRepository;

    public TestMasterService(TestMasterRepository testMasterRepository) {
        this.testMasterRepository = testMasterRepository;
    }

    @Transactional
    public TestMasterResponseDto create(TestMasterRequestDto request, String performedBy) {
        if (testMasterRepository.existsByTestCodeIgnoreCase(request.getTestCode())) {
            throw new DuplicateTestCodeException("Test code already exists: " + request.getTestCode());
        }
        TestMaster entity = new TestMaster();
        applyRequest(request, entity);
        entity.setCreatedByUser(performedBy);
        entity = testMasterRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public TestMasterResponseDto update(Long id, TestMasterRequestDto request, String performedBy) {
        TestMaster entity = testMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test master not found: " + id));
        if (!entity.getTestCode().equalsIgnoreCase(request.getTestCode()) &&
                testMasterRepository.existsByTestCodeIgnoreCase(request.getTestCode())) {
            throw new DuplicateTestCodeException("Test code already exists: " + request.getTestCode());
        }
        applyRequest(request, entity);
        entity = testMasterRepository.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void softDelete(Long id, String performedBy) {
        TestMaster entity = testMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test master not found: " + id));
        entity.setActive(false);
        testMasterRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<TestMasterResponseDto> listAll() {
        return testMasterRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TestMasterResponseDto> listActive() {
        return testMasterRepository.findByActiveTrueOrderByTestNameAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TestMasterResponseDto findById(Long id) {
        TestMaster entity = testMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test master not found: " + id));
        return toDto(entity);
    }

    /**
     * Expand a test panel into individual test codes.
     * Returns list of test codes that are part of this panel.
     */
    @Transactional(readOnly = true)
    public List<String> expandPanel(String panelTestCode) {
        TestMaster panel = testMasterRepository.findByTestCodeIgnoreCase(panelTestCode)
                .orElseThrow(() -> new ResourceNotFoundException("Panel not found: " + panelTestCode));
        if (!Boolean.TRUE.equals(panel.getIsPanel())) {
            throw new IllegalArgumentException("Test code is not a panel: " + panelTestCode);
        }
        if (panel.getPanelTestCodes() == null || panel.getPanelTestCodes().trim().isEmpty()) {
            return List.of();
        }
        return List.of(panel.getPanelTestCodes().split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void applyRequest(TestMasterRequestDto request, TestMaster entity) {
        entity.setTestCode(request.getTestCode().trim());
        entity.setTestName(request.getTestName().trim());
        entity.setCategory(request.getCategory());
        entity.setSampleType(request.getSampleType());
        entity.setNormalTATMinutes(request.getNormalTATMinutes());
        entity.setPrice(request.getPrice());
        entity.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);
        entity.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : com.hospital.hms.lab.entity.PriorityLevel.ROUTINE);
        entity.setIsPanel(request.getIsPanel() != null ? request.getIsPanel() : Boolean.FALSE);
        entity.setPanelTestCodes(request.getPanelTestCodes() != null ? request.getPanelTestCodes().trim() : null);
        entity.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        entity.setNormalRange(request.getNormalRange() != null ? request.getNormalRange().trim() : null);
        entity.setUnit(request.getUnit() != null ? request.getUnit().trim() : null);
        entity.setInstructions(request.getInstructions() != null ? request.getInstructions().trim() : null);
    }

    private TestMasterResponseDto toDto(TestMaster t) {
        TestMasterResponseDto dto = new TestMasterResponseDto();
        dto.setId(t.getId());
        dto.setTestCode(t.getTestCode());
        dto.setTestName(t.getTestName());
        dto.setCategory(t.getCategory());
        dto.setSampleType(t.getSampleType());
        dto.setNormalTATMinutes(t.getNormalTATMinutes());
        dto.setPrice(t.getPrice());
        dto.setActive(t.getActive());
        dto.setPriorityLevel(t.getPriorityLevel());
        dto.setIsPanel(t.getIsPanel());
        dto.setPanelTestCodes(t.getPanelTestCodes());
        dto.setDescription(t.getDescription());
        dto.setNormalRange(t.getNormalRange());
        dto.setUnit(t.getUnit());
        dto.setInstructions(t.getInstructions());
        dto.setCreatedByUser(t.getCreatedByUser());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }
}
