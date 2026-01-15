package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.DispenseBatch;
import com.lab.reagentdispenser.entity.Plate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispenseBatchRepository extends JpaRepository<DispenseBatch, Long> {
	
	List<DispenseBatch> findByPlate(Plate plate);
	
	List<DispenseBatch> findByStatus(DispenseBatch.BatchStatus status);
}
