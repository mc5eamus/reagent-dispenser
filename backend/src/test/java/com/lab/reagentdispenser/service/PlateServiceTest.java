package com.lab.reagentdispenser.service;

import com.lab.reagentdispenser.dto.PlateDTO;
import com.lab.reagentdispenser.dto.WellDTO;
import com.lab.reagentdispenser.entity.Plate;
import com.lab.reagentdispenser.entity.Well;
import com.lab.reagentdispenser.repository.DispenseOperationRepository;
import com.lab.reagentdispenser.repository.PlateRepository;
import com.lab.reagentdispenser.repository.WellRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PlateServiceTest {

    @Mock
    private PlateRepository plateRepository;

    @Mock
    private WellRepository wellRepository;

    @Mock
    private DispenseOperationRepository dispenseOperationRepository;

    @InjectMocks
    private PlateService plateService;

    private List<Plate> hundredPlates;

    @BeforeEach
    void setUp() {
        hundredPlates = generateHundredPlates();
        // Use lenient stubbing to avoid UnnecessaryStubbingException
        lenient().when(dispenseOperationRepository.findByWellId(anyLong())).thenReturn(new ArrayList<>());
    }

    @Test
    void getAllPlates_ReturnsAllHundredPlates() {
        // Arrange
        when(plateRepository.findAll()).thenReturn(hundredPlates);

        // Act
        List<PlateDTO> result = plateService.getAllPlates();

        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        
        // Verify first plate
        PlateDTO firstPlate = result.get(0);
        assertEquals("PLATE001", firstPlate.getBarcode());
        assertEquals(8, firstPlate.getRows());
        assertEquals(12, firstPlate.getColumns());
        assertEquals("96_WELL", firstPlate.getPlateType());
        
        // Verify last plate
        PlateDTO lastPlate = result.get(99);
        assertEquals("PLATE100", lastPlate.getBarcode());
        
        // Verify repository interaction
        verify(plateRepository, times(1)).findAll();
    }

    @Test
    void getAllPlates_ReturnsEmptyList_WhenNoPlates() {
        // Arrange
        when(plateRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<PlateDTO> result = plateService.getAllPlates();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(plateRepository, times(1)).findAll();
    }

    @Test
    void getPlateById_ReturnsCorrectPlate_FromHundredPlates() {
        // Arrange
        Long plateId = 42L;
        Plate expectedPlate = hundredPlates.stream()
                .filter(plate -> plate.getId().equals(plateId))
                .findFirst()
                .orElseThrow();
        
        when(plateRepository.findById(plateId)).thenReturn(Optional.of(expectedPlate));

        // Act
        PlateDTO result = plateService.getPlateById(plateId);

        // Assert
        assertNotNull(result);
        assertEquals(plateId, result.getId());
        assertEquals("PLATE042", result.getBarcode());
        assertEquals(8, result.getRows());
        assertEquals(12, result.getColumns());
        assertEquals("96_WELL", result.getPlateType());
        
        verify(plateRepository, times(1)).findById(plateId);
    }

    @Test
    void getPlateById_ThrowsException_WhenIdNotFound() {
        // Arrange
        Long nonExistentId = 999L;
        when(plateRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> plateService.getPlateById(nonExistentId)
        );

        assertEquals("Plate not found with id: 999", exception.getMessage());
        verify(plateRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void getPlateByBarcode_ReturnsCorrectPlate_FromHundredPlates() {
        // Arrange
        String barcode = "PLATE075";
        Plate expectedPlate = hundredPlates.stream()
                .filter(plate -> plate.getBarcode().equals(barcode))
                .findFirst()
                .orElseThrow();
        
        when(plateRepository.findByBarcode(barcode)).thenReturn(Optional.of(expectedPlate));

        // Act
        PlateDTO result = plateService.getPlateByBarcode(barcode);

        // Assert
        assertNotNull(result);
        assertEquals(75L, result.getId());
        assertEquals(barcode, result.getBarcode());
        assertEquals(8, result.getRows());
        assertEquals(12, result.getColumns());
        
        verify(plateRepository, times(1)).findByBarcode(barcode);
    }

    @Test
    void getPlateByBarcode_ThrowsException_WhenBarcodeNotFound() {
        // Arrange
        String nonExistentBarcode = "NONEXISTENT";
        when(plateRepository.findByBarcode(nonExistentBarcode)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> plateService.getPlateByBarcode(nonExistentBarcode)
        );

        assertEquals("Plate not found with barcode: NONEXISTENT", exception.getMessage());
        verify(plateRepository, times(1)).findByBarcode(nonExistentBarcode);
    }

    @Test
    void getAllPlates_EachPlateHasWells() {
        // Arrange
        when(plateRepository.findAll()).thenReturn(hundredPlates);

        // Act
        List<PlateDTO> result = plateService.getAllPlates();

        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        
        // Verify each plate has wells
        for (PlateDTO plate : result) {
            assertNotNull(plate.getWells());
            assertEquals(96, plate.getWells().size(), 
                    "Plate " + plate.getBarcode() + " should have 96 wells");
            
            // Verify well positions are correct (A1 to H12)
            List<WellDTO> wells = plate.getWells();
            assertTrue(wells.stream().anyMatch(w -> "A1".equals(w.getPosition())), 
                    "Should contain well A1");
            assertTrue(wells.stream().anyMatch(w -> "H12".equals(w.getPosition())), 
                    "Should contain well H12");
            
            // Verify well default values
            WellDTO firstWell = wells.get(0);
            assertEquals(0.0, firstWell.getVolume());
            assertEquals(300.0, firstWell.getMaxVolume());
            assertEquals(0, firstWell.getOperationCount());
        }
    }

    @Test
    void getAllPlates_VerifiesRepositoryCalledOnce() {
        // Arrange
        when(plateRepository.findAll()).thenReturn(hundredPlates);

        // Act
        plateService.getAllPlates();

        // Assert
        verify(plateRepository, times(1)).findAll();
        // Verify dispense operations are queried for each well (N+1 pattern demonstration)
        verify(dispenseOperationRepository, times(9600)).findByWellId(anyLong()); // 100 plates × 96 wells
    }

    @Test
    void getAllPlates_HandlesLargeDatasetPerformance() {
        // Arrange
        when(plateRepository.findAll()).thenReturn(hundredPlates);
        long startTime = System.currentTimeMillis();

        // Act
        List<PlateDTO> result = plateService.getAllPlates();
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        
        // Log execution time for performance awareness (in real scenarios this would be logged)
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 5000, "Service should handle 100 plates within reasonable time");
        
        // Verify total wells processed
        int totalWells = result.stream()
                .mapToInt(plate -> plate.getWells().size())
                .sum();
        assertEquals(9600, totalWells, "Should process 9600 wells total (100 × 96)");
    }

    @Test
    void getWellsForPlate_ReturnsWellsFromHundredPlateDataset() {
        // Arrange
        Long plateId = 25L;
        Plate targetPlate = hundredPlates.stream()
                .filter(plate -> plate.getId().equals(plateId))
                .findFirst()
                .orElseThrow();
        
        when(plateRepository.findById(plateId)).thenReturn(Optional.of(targetPlate));
        when(wellRepository.findByPlate(targetPlate)).thenReturn(targetPlate.getWells());

        // Act
        List<WellDTO> result = plateService.getWellsForPlate(plateId);

        // Assert
        assertNotNull(result);
        assertEquals(96, result.size());
        
        // Verify well positions
        assertTrue(result.stream().anyMatch(w -> "A1".equals(w.getPosition())));
        assertTrue(result.stream().anyMatch(w -> "H12".equals(w.getPosition())));
        
        verify(plateRepository, times(1)).findById(plateId);
        verify(wellRepository, times(1)).findByPlate(targetPlate);
    }

    /**
     * Helper method to generate 100 mock plates with wells
     */
    private List<Plate> generateHundredPlates() {
        List<Plate> plates = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            List<Well> wells = generateWellsForPlateNumber(i);
            
            Plate plate = Plate.builder()
                    .id((long) i)
                    .barcode(String.format("PLATE%03d", i))  // PLATE001 to PLATE100
                    .rows(8)
                    .columns(12)
                    .plateType("96_WELL")
                    .createdDate(LocalDateTime.now().minusDays(100 - i)) // Stagger creation dates
                    .wells(wells)
                    .build();
            
            // Set plate reference in wells
            wells.forEach(well -> well.setPlate(plate));
            plates.add(plate);
        }
        return plates;
    }

    /**
     * Helper method to generate 96 wells for a specific plate number
     */
    private List<Well> generateWellsForPlateNumber(int plateNumber) {
        List<Well> wells = new ArrayList<>();
        String[] rowLabels = {"A", "B", "C", "D", "E", "F", "G", "H"};
        long baseWellId = (plateNumber - 1) * 96; // Ensure unique well IDs across plates
        
        for (int rowIndex = 0; rowIndex < rowLabels.length; rowIndex++) {
            String row = rowLabels[rowIndex];
            for (int col = 1; col <= 12; col++) {
                long wellId = baseWellId + (rowIndex * 12) + col;
                wells.add(Well.builder()
                        .id(wellId)
                        .position(row + col)
                        .volume(0.0)
                        .maxVolume(300.0)
                        .build());
            }
        }
        return wells;
    }
}