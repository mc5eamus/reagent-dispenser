package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.Plate;
import com.lab.reagentdispenser.entity.Well;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WellRepository extends JpaRepository<Well, Long> {
	
	List<Well> findByPlate(Plate plate);
	
	Optional<Well> findByPlateAndPosition(Plate plate, String position);
}
