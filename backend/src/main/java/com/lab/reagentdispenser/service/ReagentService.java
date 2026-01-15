package com.lab.reagentdispenser.service;

import com.lab.reagentdispenser.dto.ReagentDTO;
import com.lab.reagentdispenser.entity.Reagent;
import com.lab.reagentdispenser.repository.ReagentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReagentService {

	private final ReagentRepository reagentRepository;

	public List<ReagentDTO> getAllReagents() {
		log.info("Retrieving all reagents");
		return reagentRepository.findAll().stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}

	public ReagentDTO getReagentById(Long id) {
		log.info("Retrieving reagent by id: {}", id);
		Reagent reagent = reagentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Reagent not found with id: " + id));
		return convertToDTO(reagent);
	}

	@Transactional
	public ReagentDTO createReagent(ReagentDTO reagentDTO) {
		log.info("Creating new reagent: {}", reagentDTO.getName());
		
		Reagent reagent = Reagent.builder()
				.name(reagentDTO.getName())
				.description(reagentDTO.getDescription())
				.concentration(reagentDTO.getConcentration())
				.stockVolume(reagentDTO.getStockVolume() != null ? reagentDTO.getStockVolume() : 0.0)
				.unit(reagentDTO.getUnit() != null ? reagentDTO.getUnit() : "μL")
				.build();

		Reagent savedReagent = reagentRepository.save(reagent);
		log.info("Created reagent with id: {}", savedReagent.getId());
		return convertToDTO(savedReagent);
	}

	@Transactional
	public ReagentDTO updateReagent(Long id, ReagentDTO reagentDTO) {
		log.info("Updating reagent with id: {}", id);
		
		Reagent reagent = reagentRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Reagent not found with id: " + id));

		reagent.setName(reagentDTO.getName());
		reagent.setDescription(reagentDTO.getDescription());
		reagent.setConcentration(reagentDTO.getConcentration());
		reagent.setStockVolume(reagentDTO.getStockVolume());
		reagent.setUnit(reagentDTO.getUnit());

		Reagent updatedReagent = reagentRepository.save(reagent);
		log.info("Updated reagent with id: {}", updatedReagent.getId());
		return convertToDTO(updatedReagent);
	}

	@Transactional
	public void deleteReagent(Long id) {
		log.info("Deleting reagent with id: {}", id);
		
		if (!reagentRepository.existsById(id)) {
			throw new IllegalArgumentException("Reagent not found with id: " + id);
		}

		reagentRepository.deleteById(id);
		log.info("Deleted reagent with id: {}", id);
	}

	private ReagentDTO convertToDTO(Reagent reagent) {
		return ReagentDTO.builder()
				.id(reagent.getId())
				.name(reagent.getName())
				.description(reagent.getDescription())
				.concentration(reagent.getConcentration())
				.stockVolume(reagent.getStockVolume())
				.unit(reagent.getUnit())
				.build();
	}
}
