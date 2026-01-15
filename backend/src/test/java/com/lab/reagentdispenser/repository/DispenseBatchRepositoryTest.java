package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.DispenseBatch;
import com.lab.reagentdispenser.entity.Plate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DispenseBatchRepositoryTest {

	@Autowired
	private DispenseBatchRepository batchRepository;

	@Autowired
	private PlateRepository plateRepository;

	@Test
	void shouldSaveAndFindBatchById() {
		// Arrange
		Plate plate = Plate.builder()
				.barcode("TEST-PLATE-001")
				.rows(8)
				.columns(12)
				.plateType("96_WELL")
				.build();
		Plate savedPlate = plateRepository.save(plate);

		DispenseBatch batch = DispenseBatch.builder()
				.plate(savedPlate)
				.status(DispenseBatch.BatchStatus.PLANNED)
				.build();

		// Act
		DispenseBatch savedBatch = batchRepository.save(batch);
		DispenseBatch foundBatch = batchRepository.findById(savedBatch.getId()).orElse(null);

		// Assert
		assertThat(savedBatch.getId()).isNotNull();
		assertThat(foundBatch).isNotNull();
		assertThat(foundBatch.getStatus()).isEqualTo(DispenseBatch.BatchStatus.PLANNED);
		assertThat(foundBatch.getPlate().getBarcode()).isEqualTo("TEST-PLATE-001");
	}

	@Test
	void shouldFindBatchesByPlate() {
		// Arrange
		Plate plate1 = plateRepository.save(Plate.builder().barcode("PLATE-1").build());
		Plate plate2 = plateRepository.save(Plate.builder().barcode("PLATE-2").build());

		batchRepository.save(DispenseBatch.builder().plate(plate1).build());
		batchRepository.save(DispenseBatch.builder().plate(plate1).build());
		batchRepository.save(DispenseBatch.builder().plate(plate2).build());

		// Act
		List<DispenseBatch> plate1Batches = batchRepository.findByPlate(plate1);

		// Assert
		assertThat(plate1Batches).hasSize(2);
	}

	@Test
	void shouldFindBatchesByStatus() {
		// Arrange
		Plate plate = plateRepository.save(Plate.builder().barcode("PLATE-1").build());

		batchRepository.save(DispenseBatch.builder()
				.plate(plate)
				.status(DispenseBatch.BatchStatus.PLANNED)
				.build());
		batchRepository.save(DispenseBatch.builder()
				.plate(plate)
				.status(DispenseBatch.BatchStatus.PLANNED)
				.build());
		batchRepository.save(DispenseBatch.builder()
				.plate(plate)
				.status(DispenseBatch.BatchStatus.COMPLETED)
				.build());

		// Act
		List<DispenseBatch> plannedBatches = batchRepository.findByStatus(DispenseBatch.BatchStatus.PLANNED);

		// Assert
		assertThat(plannedBatches).hasSize(2);
	}
}
