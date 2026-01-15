package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.Plate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PlateRepositoryTest {

	@Autowired
	private PlateRepository plateRepository;

	@Test
	void shouldSaveAndFindPlateByBarcode() {
		// Arrange
		Plate plate = Plate.builder()
				.barcode("TEST-PLATE-001")
				.rows(8)
				.columns(12)
				.plateType("96_WELL")
				.build();

		// Act
		Plate savedPlate = plateRepository.save(plate);
		Optional<Plate> foundPlate = plateRepository.findByBarcode("TEST-PLATE-001");

		// Assert
		assertThat(savedPlate.getId()).isNotNull();
		assertThat(foundPlate).isPresent();
		assertThat(foundPlate.get().getBarcode()).isEqualTo("TEST-PLATE-001");
		assertThat(foundPlate.get().getRows()).isEqualTo(8);
		assertThat(foundPlate.get().getColumns()).isEqualTo(12);
	}

	@Test
	void shouldReturnEmptyWhenBarcodeNotFound() {
		// Act
		Optional<Plate> foundPlate = plateRepository.findByBarcode("NON-EXISTENT");

		// Assert
		assertThat(foundPlate).isEmpty();
	}

	@Test
	void shouldCheckIfBarcodeExists() {
		// Arrange
		Plate plate = Plate.builder()
				.barcode("EXIST-TEST-001")
				.rows(8)
				.columns(12)
				.build();
		plateRepository.save(plate);

		// Act & Assert
		assertThat(plateRepository.existsByBarcode("EXIST-TEST-001")).isTrue();
		assertThat(plateRepository.existsByBarcode("NON-EXISTENT")).isFalse();
	}
}
