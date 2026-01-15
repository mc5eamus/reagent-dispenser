package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.Plate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlateRepository extends JpaRepository<Plate, Long> {
	
	Optional<Plate> findByBarcode(String barcode);
	
	boolean existsByBarcode(String barcode);
}
