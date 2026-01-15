package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.Reagent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReagentRepositoryTest {

	@Autowired
	private ReagentRepository reagentRepository;

	@Test
	void shouldSaveAndFindReagentByName() {
		// Arrange
		Reagent reagent = Reagent.builder()
				.name("Test Reagent")
				.description("Test Description")
				.concentration("10 mM")
				.stockVolume(1000.0)
				.unit("μL")
				.build();

		// Act
		Reagent savedReagent = reagentRepository.save(reagent);
		Optional<Reagent> foundReagent = reagentRepository.findByName("Test Reagent");

		// Assert
		assertThat(savedReagent.getId()).isNotNull();
		assertThat(foundReagent).isPresent();
		assertThat(foundReagent.get().getName()).isEqualTo("Test Reagent");
		assertThat(foundReagent.get().getConcentration()).isEqualTo("10 mM");
		assertThat(foundReagent.get().getStockVolume()).isEqualTo(1000.0);
	}

	@Test
	void shouldUseDefaultValues() {
		// Arrange
		Reagent reagent = Reagent.builder()
				.name("Minimal Reagent")
				.build();

		// Act
		Reagent savedReagent = reagentRepository.save(reagent);

		// Assert
		assertThat(savedReagent.getStockVolume()).isEqualTo(0.0);
		assertThat(savedReagent.getUnit()).isEqualTo("μL");
	}
}
