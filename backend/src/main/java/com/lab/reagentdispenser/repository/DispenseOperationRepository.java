package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.DispenseOperation;
import com.lab.reagentdispenser.entity.Plate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DispenseOperationRepository extends JpaRepository<DispenseOperation, Long> {
	
	List<DispenseOperation> findByPlate(Plate plate);
	
	List<DispenseOperation> findByStatus(DispenseOperation.OperationStatus status);
}
