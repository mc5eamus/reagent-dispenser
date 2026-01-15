package com.lab.reagentdispenser.repository;

import com.lab.reagentdispenser.entity.Reagent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReagentRepository extends JpaRepository<Reagent, Long> {
	
	Optional<Reagent> findByName(String name);
}
