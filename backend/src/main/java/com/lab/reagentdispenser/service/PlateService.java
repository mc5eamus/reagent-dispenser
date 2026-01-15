package com.lab.reagentdispenser.service;

import com.lab.reagentdispenser.dto.PlateDTO;
import com.lab.reagentdispenser.dto.WellDTO;
import com.lab.reagentdispenser.entity.Plate;
import com.lab.reagentdispenser.entity.Well;
import com.lab.reagentdispenser.repository.PlateRepository;
import com.lab.reagentdispenser.repository.WellRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlateService {

	private final PlateRepository plateRepository;
	private final WellRepository wellRepository;

	public List<PlateDTO> getAllPlates() {
		log.info("Retrieving all plates");
		return plateRepository.findAll().stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public PlateDTO getPlateById(Long id) {
		log.info("Retrieving plate by id: {}", id);
		Plate plate = plateRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Plate not found with id: " + id));
		return convertToDTO(plate);
	}

	public PlateDTO getPlateByBarcode(String barcode) {
		log.info("Retrieving plate by barcode: {}", barcode);
		Plate plate = plateRepository.findByBarcode(barcode)
				.orElseThrow(() -> new IllegalArgumentException("Plate not found with barcode: " + barcode));
		return convertToDTO(plate);
	}

	@Transactional
	public PlateDTO createPlate(PlateDTO plateDTO) {
		log.info("Creating new plate with barcode: {}", plateDTO.getBarcode());
		
		if (plateRepository.existsByBarcode(plateDTO.getBarcode())) {
			throw new IllegalArgumentException("Plate with barcode already exists: " + plateDTO.getBarcode());
		}

		Plate plate = Plate.builder()
				.barcode(plateDTO.getBarcode())
				.rows(plateDTO.getRows() != null ? plateDTO.getRows() : 8)
				.columns(plateDTO.getColumns() != null ? plateDTO.getColumns() : 12)
				.plateType(plateDTO.getPlateType() != null ? plateDTO.getPlateType() : "96_WELL")
				.createdDate(LocalDateTime.now())
				.build();

		Plate savedPlate = plateRepository.save(plate);
		
		// Create wells for the plate
		createWellsForPlate(savedPlate);
		
		log.info("Created plate with id: {}", savedPlate.getId());
		return convertToDTO(savedPlate);
	}

	@Transactional
	public PlateDTO updatePlate(Long id, PlateDTO plateDTO) {
		log.info("Updating plate with id: {}", id);
		
		Plate plate = plateRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Plate not found with id: " + id));

		if (!plate.getBarcode().equals(plateDTO.getBarcode()) && 
			plateRepository.existsByBarcode(plateDTO.getBarcode())) {
			throw new IllegalArgumentException("Plate with barcode already exists: " + plateDTO.getBarcode());
		}

		plate.setBarcode(plateDTO.getBarcode());
		plate.setRows(plateDTO.getRows());
		plate.setColumns(plateDTO.getColumns());
		plate.setPlateType(plateDTO.getPlateType());

		Plate updatedPlate = plateRepository.save(plate);
		log.info("Updated plate with id: {}", updatedPlate.getId());
		return convertToDTO(updatedPlate);
	}

	@Transactional
	public void deletePlate(Long id) {
		log.info("Deleting plate with id: {}", id);
		
		if (!plateRepository.existsById(id)) {
			throw new IllegalArgumentException("Plate not found with id: " + id);
		}

		plateRepository.deleteById(id);
		log.info("Deleted plate with id: {}", id);
	}

	public List<WellDTO> getWellsForPlate(Long plateId) {
		log.info("Retrieving wells for plate id: {}", plateId);
		Plate plate = plateRepository.findById(plateId)
				.orElseThrow(() -> new IllegalArgumentException("Plate not found with id: " + plateId));
		
		return wellRepository.findByPlate(plate).stream()
				.map(this::convertWellToDTO)
				.collect(Collectors.toList());
	}

	private void createWellsForPlate(Plate plate) {
		log.info("Creating wells for plate: {}", plate.getBarcode());
		
		for (int row = 0; row < plate.getRows(); row++) {
			char rowLetter = (char) ('A' + row);
			for (int col = 1; col <= plate.getColumns(); col++) {
				String position = rowLetter + String.valueOf(col);
				Well well = Well.builder()
						.position(position)
						.plate(plate)
						.volume(0.0)
						.maxVolume(300.0)
						.build();
				wellRepository.save(well);
			}
		}
		
		log.info("Created {} wells for plate", plate.getRows() * plate.getColumns());
	}

	private PlateDTO convertToDTO(Plate plate) {
		return PlateDTO.builder()
				.id(plate.getId())
				.barcode(plate.getBarcode())
				.rows(plate.getRows())
				.columns(plate.getColumns())
				.plateType(plate.getPlateType())
				.createdDate(plate.getCreatedDate())
				.build();
	}

	private WellDTO convertWellToDTO(Well well) {
		return WellDTO.builder()
				.id(well.getId())
				.position(well.getPosition())
				.plateId(well.getPlate().getId())
				.volume(well.getVolume())
				.maxVolume(well.getMaxVolume())
				.build();
	}
}
