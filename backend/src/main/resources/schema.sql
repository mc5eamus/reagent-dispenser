-- Drop tables if they exist
DROP TABLE IF EXISTS dispense_operations;
DROP TABLE IF EXISTS wells;
DROP TABLE IF EXISTS plates;
DROP TABLE IF EXISTS reagents;

-- Create plates table
CREATE TABLE plates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    barcode VARCHAR(255) NOT NULL UNIQUE,
    rows INT NOT NULL DEFAULT 8,
    columns INT NOT NULL DEFAULT 12,
    plate_type VARCHAR(50) NOT NULL DEFAULT '96_WELL',
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create wells table
CREATE TABLE wells (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position VARCHAR(10) NOT NULL,
    plate_id BIGINT NOT NULL,
    volume DOUBLE,
    max_volume DOUBLE NOT NULL DEFAULT 300.0,
    FOREIGN KEY (plate_id) REFERENCES plates(id) ON DELETE CASCADE,
    CONSTRAINT unique_plate_position UNIQUE (plate_id, position)
);

-- Create reagents table
CREATE TABLE reagents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    concentration VARCHAR(100),
    stock_volume DOUBLE NOT NULL DEFAULT 0.0,
    unit VARCHAR(10) NOT NULL DEFAULT 'μL'
);

-- Create dispense_operations table
CREATE TABLE dispense_operations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_id BIGINT NOT NULL,
    well_id BIGINT NOT NULL,
    reagent_id BIGINT NOT NULL,
    volume_dispensed DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP,
    error_message VARCHAR(1000),
    FOREIGN KEY (plate_id) REFERENCES plates(id),
    FOREIGN KEY (well_id) REFERENCES wells(id),
    FOREIGN KEY (reagent_id) REFERENCES reagents(id)
);

-- Create indexes
CREATE INDEX idx_plate_barcode ON plates(barcode);
CREATE INDEX idx_well_plate ON wells(plate_id);
CREATE INDEX idx_operation_status ON dispense_operations(status);
CREATE INDEX idx_operation_plate ON dispense_operations(plate_id);
