-- Insert sample plates
INSERT INTO plates (barcode, rows, columns, plate_type, created_date) VALUES 
('PLATE-001', 8, 12, '96_WELL', CURRENT_TIMESTAMP),
('PLATE-002', 8, 12, '96_WELL', CURRENT_TIMESTAMP),
('PLATE-003', 8, 12, '96_WELL', CURRENT_TIMESTAMP);

-- Insert wells for PLATE-001 (96 wells: A1-H12)
-- Row A
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('A1', 1, 0.0, 300.0), ('A2', 1, 0.0, 300.0), ('A3', 1, 0.0, 300.0), ('A4', 1, 0.0, 300.0),
('A5', 1, 0.0, 300.0), ('A6', 1, 0.0, 300.0), ('A7', 1, 0.0, 300.0), ('A8', 1, 0.0, 300.0),
('A9', 1, 0.0, 300.0), ('A10', 1, 0.0, 300.0), ('A11', 1, 0.0, 300.0), ('A12', 1, 0.0, 300.0);

-- Row B
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('B1', 1, 0.0, 300.0), ('B2', 1, 0.0, 300.0), ('B3', 1, 0.0, 300.0), ('B4', 1, 0.0, 300.0),
('B5', 1, 0.0, 300.0), ('B6', 1, 0.0, 300.0), ('B7', 1, 0.0, 300.0), ('B8', 1, 0.0, 300.0),
('B9', 1, 0.0, 300.0), ('B10', 1, 0.0, 300.0), ('B11', 1, 0.0, 300.0), ('B12', 1, 0.0, 300.0);

-- Row C
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('C1', 1, 0.0, 300.0), ('C2', 1, 0.0, 300.0), ('C3', 1, 0.0, 300.0), ('C4', 1, 0.0, 300.0),
('C5', 1, 0.0, 300.0), ('C6', 1, 0.0, 300.0), ('C7', 1, 0.0, 300.0), ('C8', 1, 0.0, 300.0),
('C9', 1, 0.0, 300.0), ('C10', 1, 0.0, 300.0), ('C11', 1, 0.0, 300.0), ('C12', 1, 0.0, 300.0);

-- Row D
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('D1', 1, 0.0, 300.0), ('D2', 1, 0.0, 300.0), ('D3', 1, 0.0, 300.0), ('D4', 1, 0.0, 300.0),
('D5', 1, 0.0, 300.0), ('D6', 1, 0.0, 300.0), ('D7', 1, 0.0, 300.0), ('D8', 1, 0.0, 300.0),
('D9', 1, 0.0, 300.0), ('D10', 1, 0.0, 300.0), ('D11', 1, 0.0, 300.0), ('D12', 1, 0.0, 300.0);

-- Row E
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('E1', 1, 0.0, 300.0), ('E2', 1, 0.0, 300.0), ('E3', 1, 0.0, 300.0), ('E4', 1, 0.0, 300.0),
('E5', 1, 0.0, 300.0), ('E6', 1, 0.0, 300.0), ('E7', 1, 0.0, 300.0), ('E8', 1, 0.0, 300.0),
('E9', 1, 0.0, 300.0), ('E10', 1, 0.0, 300.0), ('E11', 1, 0.0, 300.0), ('E12', 1, 0.0, 300.0);

-- Row F
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('F1', 1, 0.0, 300.0), ('F2', 1, 0.0, 300.0), ('F3', 1, 0.0, 300.0), ('F4', 1, 0.0, 300.0),
('F5', 1, 0.0, 300.0), ('F6', 1, 0.0, 300.0), ('F7', 1, 0.0, 300.0), ('F8', 1, 0.0, 300.0),
('F9', 1, 0.0, 300.0), ('F10', 1, 0.0, 300.0), ('F11', 1, 0.0, 300.0), ('F12', 1, 0.0, 300.0);

-- Row G
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('G1', 1, 0.0, 300.0), ('G2', 1, 0.0, 300.0), ('G3', 1, 0.0, 300.0), ('G4', 1, 0.0, 300.0),
('G5', 1, 0.0, 300.0), ('G6', 1, 0.0, 300.0), ('G7', 1, 0.0, 300.0), ('G8', 1, 0.0, 300.0),
('G9', 1, 0.0, 300.0), ('G10', 1, 0.0, 300.0), ('G11', 1, 0.0, 300.0), ('G12', 1, 0.0, 300.0);

-- Row H
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('H1', 1, 0.0, 300.0), ('H2', 1, 0.0, 300.0), ('H3', 1, 0.0, 300.0), ('H4', 1, 0.0, 300.0),
('H5', 1, 0.0, 300.0), ('H6', 1, 0.0, 300.0), ('H7', 1, 0.0, 300.0), ('H8', 1, 0.0, 300.0),
('H9', 1, 0.0, 300.0), ('H10', 1, 0.0, 300.0), ('H11', 1, 0.0, 300.0), ('H12', 1, 0.0, 300.0);

-- Insert wells for PLATE-002 (first row only for brevity)
INSERT INTO wells (position, plate_id, volume, max_volume) VALUES 
('A1', 2, 0.0, 300.0), ('A2', 2, 0.0, 300.0), ('A3', 2, 0.0, 300.0), ('A4', 2, 0.0, 300.0),
('A5', 2, 0.0, 300.0), ('A6', 2, 0.0, 300.0), ('A7', 2, 0.0, 300.0), ('A8', 2, 0.0, 300.0),
('A9', 2, 0.0, 300.0), ('A10', 2, 0.0, 300.0), ('A11', 2, 0.0, 300.0), ('A12', 2, 0.0, 300.0);

-- Insert sample reagents
INSERT INTO reagents (name, description, concentration, stock_volume, unit) VALUES 
('DMSO', 'Dimethyl sulfoxide - Common solvent', '100%', 50000.0, 'μL'),
('PBS Buffer', 'Phosphate-buffered saline pH 7.4', '1X', 100000.0, 'μL'),
('Trypsin-EDTA', 'Cell dissociation enzyme', '0.25%', 25000.0, 'μL'),
('FBS', 'Fetal Bovine Serum for cell culture', '100%', 50000.0, 'μL'),
('Penicillin-Streptomycin', 'Antibiotic mixture', '10000 U/mL', 10000.0, 'μL');

-- Insert sample dispense operations
INSERT INTO dispense_operations (plate_id, well_id, reagent_id, volume_dispensed, status, created_date, completed_date) VALUES 
(1, 1, 1, 50.0, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(1, 2, 1, 50.0, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2' HOUR, CURRENT_TIMESTAMP - INTERVAL '2' HOUR),
(1, 3, 2, 100.0, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1' HOUR, CURRENT_TIMESTAMP - INTERVAL '1' HOUR),
(1, 4, 2, 100.0, 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '5' MINUTE, NULL),
(1, 5, 3, 25.0, 'PENDING', CURRENT_TIMESTAMP, NULL);
