package com.lab.reagentdispenser.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Barcode is required")
	@Column(nullable = false, unique = true)
	private String barcode;

	@Min(value = 1, message = "Rows must be at least 1")
	@Column(nullable = false)
	@Builder.Default
	private Integer rows = 8;

	@Min(value = 1, message = "Columns must be at least 1")
	@Column(nullable = false)
	@Builder.Default
	private Integer columns = 12;

	@Column(name = "plate_type", nullable = false)
	@Builder.Default
	private String plateType = "96_WELL";

	@Column(name = "created_date", nullable = false)
	@Builder.Default
	private LocalDateTime createdDate = LocalDateTime.now();

	@OneToMany(mappedBy = "plate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@Builder.Default
	private List<Well> wells = new ArrayList<>();
}
