-- ============================================================================
-- EXPANDED TEST DATA FOR TWO-PHASE OPTIMIZATION - WITH GERMAN SEMESTER NOTATION
-- ============================================================================
-- This dataset provides comprehensive test data for the internship optimization system
-- including support for WINTER (WiSe) and SUMMER (SoSe) semester re-optimization workflows.
-- 
-- DATA SUMMARY:
-- - 10 Schools (IDs: 15-24) across 3 zones, mix of GS and MS types
-- - 25 Teachers (IDs: 1001-1025) with diverse Course specializations
-- - 20 Students (matriculation: 115-134) with varied course preferences
-- - All teachers have 'active' status set to TRUE by default
-- 
-- TEACHER CONFIGURATION:
-- - Teachers 1001-1012: Available for PDP_I and ZSP (GS internships)
-- - Teachers 1013-1025: Available for PDP_II, ZSP, and SFP (MS internships)
-- - All teachers: max 3 praktika per year, 4 hours credit
-- - Teacher configs available for BOTH WiSe2025 and SoSe2025
-- 
-- STUDENT CONFIGURATION:
-- - 10 GS students (115, 117, 119, 121, 123, 125, 127, 129, 131, 133)
-- - 10 MS students (116, 118, 120, 122, 124, 126, 128, 130, 132, 134)
-- - Each student has main_course and 3 preference courses
-- - Each student has configs for BOTH winter and summer semesters (40 total configs)
-- - Year field uses German semester notation: "WiSe2025" (Wintersemester) and "SoSe2025" (Sommersemester)
-- 
-- OPTIMIZATION CAPACITY:
-- - 25 teachers × 3 praktika = 75 potential internship slots per semester
-- - 20 students requiring assignments per semester
-- - Sufficient capacity to find optimal solutions
-- 
-- REOPTIMIZATION TESTING WORKFLOW:
-- 1. Run Phase 2 optimization for WINTER: POST /api/phase2/optimize {"schoolYear": "WiSe2025"}
-- 2. Save WINTER results as baseline: POST /api/baselines/capture {"schoolYear": "WiSe2025"}
-- 3. Run reoptimization for SUMMER: POST /api/reoptimization/optimize {"schoolYear": "SoSe2025"}
-- 4. System automatically loads WiSe2025 baseline and preserves assignments where possible
-- ============================================================================


-- ============================================================================
-- COURSES - Must be created FIRST since they're referenced by foreign keys
-- ============================================================================
INSERT INTO `courses` (`id`, `name`, `active`) VALUES 
(1, 'COMPUTER_SCIENCE', true),
(2, 'ENGINEERING', true),
(3, 'BUSINESS', true),
(4, 'MEDICINE', true),
(5, 'LAW', true),
(6, 'ARTS', true),
(7, 'SCIENCES', true);

-- ============================================================================
-- SCHOOLS - Expanded to 10 schools across 3 zones
-- ============================================================================
INSERT INTO `schools` (`active`, `oepnv`, `id`, `address`, `name`, `zone`, `type`) VALUES
(1, 1, '15', '123 Main St', 'Central School', '1', 'MS'),
(1, 0, '16', '456 Elm St', 'North High', '2', 'GS'),
(1, 1, '17', '789 Oak St', 'East Academy', '3', 'MS'),
(1, 1, '18', '321 Maple Ave', 'West Elementary', '1', 'GS'),
(1, 0, '19', '654 Pine Rd', 'South Middle School', '2', 'MS'),
(1, 1, '20', '987 Cedar Blvd', 'Downtown Academy', '3', 'GS'),
(1, 1, '21', '147 Birch Ln', 'Riverside School', '1', 'MS'),
(1, 0, '22', '258 Walnut St', 'Hillside Elementary', '2', 'GS'),
(1, 1, '23', '369 Spruce Dr', 'Lakeside Middle', '3', 'MS'),
(1, 1, '24', '741 Ash Ct', 'Harbor View School', '1', 'GS'),
(1, 1, '101', '100 Test St', 'Test GS School Zone 1', '1', 'GS'),
(1, 1, '102', '200 Test Ave', 'Test MS School Zone 2', '2', 'MS'),
(1, 1, '103', '300 Test Blvd', 'Test GS School Zone 2', '2', 'GS');

-- ============================================================================
-- TEACHERS - Now using course IDs with 'active' field set to TRUE
-- ============================================================================
INSERT INTO teachers (school_id, teacher_id, email, first_name, last_name, main_subject_id, is_part_time, active) VALUES 
(15, 1001, 'john.doe@example.com', 'John', 'Doe', 1, 0, 1),           -- COMPUTER_SCIENCE
(16, 1002, 'mary.smith@example.com', 'Mary', 'Smith', 2, 0, 1),       -- ENGINEERING
(17, 1003, 'kevin.brown@example.com', 'Kevin', 'Brown', 3, 0, 1),     -- BUSINESS
(15, 1004, 'lisa.white@example.com', 'Lisa', 'White', 4, 0, 1),       -- MEDICINE
(16, 1005, 'peter.johnson@example.com', 'Peter', 'Johnson', 5, 0, 1), -- LAW 
(17, 1006, 'anna.davis@example.com', 'Anna', 'Davis', 6, 0, 1),       -- ARTS 
(15, 1007, 'mike.miller@example.com', 'Mike', 'Miller', 7, 0, 1),     -- SCIENCES
(16, 1008, 'susan.moore@example.com', 'Susan', 'Moore', 1, 0, 1),     -- COMPUTER_SCIENCE
(17, 1009, 'david.taylor@example.com', 'David', 'Taylor', 3, 0, 1),   -- BUSINESS
(15, 1010, 'emily.anderson@example.com', 'Emily', 'Anderson', 4, 0, 1), -- MEDICINE
(18, 1011, 'robert.wilson@example.com', 'Robert', 'Wilson', 6, 0, 1),   -- ARTS
(19, 1012, 'sarah.martinez@example.com', 'Sarah', 'Martinez', 7, 0, 1), -- SCIENCES
(20, 1013, 'james.garcia@example.com', 'James', 'Garcia', 1, 0, 1),     -- COMPUTER_SCIENCE 
(21, 1014, 'jennifer.rodriguez@example.com', 'Jennifer', 'Rodriguez', 2, 0, 1), -- ENGINEERING
(22, 1015, 'william.lee@example.com', 'William', 'Lee', 3, 0, 1),       -- BUSINESS
(23, 1016, 'elizabeth.walker@example.com', 'Elizabeth', 'Walker', 5, 0, 1), -- LAW
(24, 1017, 'michael.hall@example.com', 'Michael', 'Hall', 4, 0, 1),     -- MEDICINE
(15, 1018, 'linda.young@example.com', 'Linda', 'Young', 6, 0, 1),       -- ARTS
(16, 1019, 'thomas.king@example.com', 'Thomas', 'King', 7, 0, 1),       -- SCIENCES
(17, 1020, 'patricia.wright@example.com', 'Patricia', 'Wright', 1, 0, 1), -- COMPUTER_SCIENCE
(18, 1021, 'christopher.lopez@example.com', 'Christopher', 'Lopez', 2, 0, 1), -- ENGINEERING
(19, 1022, 'barbara.hill@example.com', 'Barbara', 'Hill', 3, 0, 1),     -- BUSINESS 
(20, 1023, 'daniel.scott@example.com', 'Daniel', 'Scott', 5, 0, 1),     -- LAW
(21, 1024, 'nancy.green@example.com', 'Nancy', 'Green', 4, 0, 1),       -- MEDICINE
(22, 1025, 'matthew.adams@example.com', 'Matthew', 'Adams', 6, 0, 1),   -- ARTS
(101, 2001, 'alice@test.com', 'Alice', 'Anderson', 1, 0, 1),  -- COMPUTER_SCIENCE 
(101, 2002, 'bob@test.com', 'Bob', 'Brown', 3, 0, 1),         -- BUSINESS
(102, 2003, 'carol@test.com', 'Carol', 'Clark', 2, 0, 1);     -- ENGINEERING

-- ============================================================================
-- STUDENTS - Now using course IDs instead of enum strings
-- ============================================================================
INSERT INTO students (birth_date, matriculation_nbr, oriented, registred, city, country, description, email, first_name, house_nbr, last_name, phone, postal_code, semester_city, semester_country, semester_house_nbr, semester_postal_code, semester_street, street, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type) VALUES
('2004-04-12', 115, b'1', b'1', 'Tunis', 'Tunisia', NULL, 'student1@example.com', 'Imen', '16', 'Ben Youssef', '6516512330', '1000', NULL, NULL, NULL, NULL, 'Linden St', 'Linden St', 6, 7, 4, 3, 'GS'),  -- ARTS, SCIENCES, MEDICINE, BUSINESS
('2003-07-22', 116, b'0', b'1', 'Berlin', 'Germany', NULL, 'student2@example.com', 'Lukas', '12', 'Schmidt', '491512345678', '10115', NULL, NULL, NULL, NULL, 'Maple St', 'Maple St', 1, 2, 3, 5, 'MS'),  -- COMPUTER_SCIENCE, ENGINEERING, BUSINESS, LAW
('2005-01-30', 117, b'1', b'0', 'Paris', 'France', NULL, 'student3@example.com', 'Claire', '5', 'Dubois', '33123456789', '75001', NULL, NULL, NULL, NULL, 'Oak St', 'Oak St', 3, 4, 6, 7, 'GS'),  -- BUSINESS, MEDICINE, ARTS, SCIENCES
('2004-11-10', 118, b'1', b'1', 'Rome', 'Italy', NULL, 'student4@example.com', 'Marco', '8', 'Rossi', '390612345678', '00100', NULL, NULL, NULL, NULL, 'Pine St', 'Pine St', 7, 5, 6, 1, 'MS'),  -- SCIENCES, LAW, ARTS, COMPUTER_SCIENCE
('2003-03-18', 119, b'0', b'0', 'Madrid', 'Spain', NULL, 'student5@example.com', 'Sofia', '21', 'Gonzalez', '34912345678', '28001', NULL, NULL, NULL, NULL, 'Birch St', 'Birch St', 4, 3, 2, 6, 'GS'),  -- MEDICINE, BUSINESS, ENGINEERING, ARTS
('2005-08-25', 120, b'1', b'1', 'Lisbon', 'Portugal', NULL, 'student6@example.com', 'Miguel', '3', 'Silva', '351912345678', '1000-001', NULL, NULL, NULL, NULL, 'Cedar St', 'Cedar St', 5, 6, 7, 3, 'MS'),  -- LAW, ARTS, SCIENCES, BUSINESS
('2004-06-14', 121, b'0', b'1', 'Vienna', 'Austria', NULL, 'student7@example.com', 'Anna', '11', 'Muller', '43123456789', '1010', NULL, NULL, NULL, NULL, 'Spruce St', 'Spruce St', 1, 2, 5, 4, 'GS'),  -- COMPUTER_SCIENCE, ENGINEERING, LAW, MEDICINE
('2003-12-02', 122, b'1', b'0', 'Zurich', 'Switzerland', NULL, 'student8@example.com', 'Luca', '7', 'Meier', '41791234567', '8001', NULL, NULL, NULL, NULL, 'Willow St', 'Willow St', 3, 6, 7, 7, 'MS'),  -- BUSINESS, ARTS, SCIENCES, SCIENCES
('2005-05-05', 123, b'0', b'1', 'Brussels', 'Belgium', NULL, 'student9@example.com', 'Emma', '9', 'Peeters', '32475123456', '1000', NULL, NULL, NULL, NULL, 'Fir St', 'Fir St', 6, 1, 3, 5, 'GS'),  -- ARTS, COMPUTER_SCIENCE, BUSINESS, LAW
('2004-09-17', 124, b'1', b'1', 'Amsterdam', 'Netherlands', NULL, 'student10@example.com', 'Jan', '14', 'De Vries', '31201234567', '1012', NULL, NULL, NULL, NULL, 'Elm St', 'Elm St', 2, 7, 6, 1, 'MS'),  -- ENGINEERING, SCIENCES, ARTS, COMPUTER_SCIENCE
('2003-02-28', 125, b'0', b'0', 'Oslo', 'Norway', NULL, 'student11@example.com', 'Nora', '6', 'Hansen', '4720123456', '0150', NULL, NULL, NULL, NULL, 'Poplar St', 'Poplar St', 5, 4, 3, 6, 'GS'),  -- LAW, MEDICINE, BUSINESS, ARTS
('2005-10-11', 126, b'1', b'1', 'Helsinki', 'Finland', NULL, 'student12@example.com', 'Aino', '2', 'Korhonen', '35891234567', '00100', NULL, NULL, NULL, NULL, 'Aspen St', 'Aspen St', 4, 6, 7, 1, 'MS'),  -- MEDICINE, ARTS, SCIENCES, COMPUTER_SCIENCE
('2004-04-01', 127, b'0', b'1', 'Copenhagen', 'Denmark', NULL, 'student13@example.com', 'Freja', '10', 'Jensen', '451234567', '1000', NULL, NULL, NULL, NULL, 'Cypress St', 'Cypress St', 7, 3, 2, 6, 'GS'),  -- SCIENCES, BUSINESS, ENGINEERING, ARTS
('2003-07-07', 128, b'1', b'0', 'Stockholm', 'Sweden', NULL, 'student14@example.com', 'Erik', '18', 'Lindberg', '4612345678', '111 22', NULL, NULL, NULL, NULL, 'Maple St', 'Maple St', 2, 5, 4, 1, 'MS'),  -- ENGINEERING, LAW, MEDICINE, COMPUTER_SCIENCE
('2005-03-20', 129, b'0', b'1', 'Reykjavik', 'Iceland', NULL, 'student15@example.com', 'Bjorn', '1', 'Sigurdsson', '3541234567', '101', NULL, NULL, NULL, NULL, 'Oak St', 'Oak St', 3, 1, 6, 7, 'GS'),  -- BUSINESS, COMPUTER_SCIENCE, ARTS, SCIENCES
('2004-08-30', 130, b'1', b'1', 'Dublin', 'Ireland', NULL, 'student16@example.com', 'Aoife', '20', 'OBrien', '35312345678', '1000', NULL, NULL, NULL, NULL, 'Pine St', 'Pine St', 1, 2, 3, 4, 'MS'),  -- COMPUTER_SCIENCE, ENGINEERING, BUSINESS, MEDICINE
('2003-11-05', 131, b'0', b'0', 'London', 'UK', NULL, 'student17@example.com', 'Harry', '15', 'Smith', '441234567890', 'SW1A', NULL, NULL, NULL, NULL, 'Birch St', 'Birch St', 5, 6, 7, 3, 'GS'),  -- LAW, ARTS, SCIENCES, BUSINESS
('2005-06-18', 132, b'1', b'1', 'Edinburgh', 'UK', NULL, 'student18@example.com', 'Isla', '8', 'Brown', '441234567891', 'EH1', NULL, NULL, NULL, NULL, 'Cedar St', 'Cedar St', 6, 4, 1, 2, 'MS'),  -- ARTS, MEDICINE, COMPUTER_SCIENCE, ENGINEERING
('2004-12-12', 133, b'0', b'1', 'Manchester', 'UK', NULL, 'student19@example.com', 'Oliver', '9', 'Taylor', '441234567892', 'M1', NULL, NULL, NULL, NULL, 'Spruce St', 'Spruce St', 7, 3, 5, 6, 'GS'),  -- SCIENCES, BUSINESS, LAW, ARTS
('2003-05-09', 134, b'1', b'0', 'Birmingham', 'UK', NULL, 'student20@example.com', 'Amelia', '11', 'Wilson', '441234567893', 'B1', NULL, NULL, NULL, NULL, 'Willow St', 'Willow St', 4, 6, 1, 3, 'MS'),  -- MEDICINE, ARTS, COMPUTER_SCIENCE, BUSINESS
-- NEW STUDENTS for SoSe2025 (REDUCED TO 3 for minimal testing)
('2005-09-15', 201, b'1', b'1', 'Hamburg', 'Germany', NULL, 'student21@example.com', 'Felix', '22', 'Wagner', '491601234567', '20095', NULL, NULL, NULL, NULL, 'Bahnhof St', 'Bahnhof St', 1, 3, 5, 2, 'GS'),  -- COMPUTER_SCIENCE, BUSINESS, LAW, ENGINEERING
('2004-02-20', 202, b'0', b'1', 'Lyon', 'France', NULL, 'student22@example.com', 'Marie', '13', 'Martin', '33601234567', '69001', NULL, NULL, NULL, NULL, 'Rue St', 'Rue St', 4, 6, 7, 1, 'GS'),  -- MEDICINE, ARTS, SCIENCES, COMPUTER_SCIENCE
('2005-12-08', 203, b'1', b'0', 'Milan', 'Italy', NULL, 'student23@example.com', 'Giulia', '17', 'Ferrari', '393401234567', '20121', NULL, NULL, NULL, NULL, 'Via St', 'Via St', 2, 1, 4, 3, 'GS');  -- ENGINEERING, COMPUTER_SCIENCE, MEDICINE, BUSINESS

-- ============================================================================
-- TEACHER PLANNING CONFIGURATIONS (WiSe2025) - WINTER SEMESTER
-- ============================================================================
INSERT INTO teacher_pl_configs (teacher_id, school_year, max_praktika_per_year, total_hours_credit, availability_status, active) VALUES
(1001, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1002, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1003, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1004, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1005, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1006, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1007, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1008, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1009, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1010, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1011, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1012, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1013, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1014, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1015, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1016, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1017, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1018, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1019, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1020, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1021, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1022, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1023, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1024, 'WiSe2025', 3, 4, 'AVAILABLE', 1),
(1025, 'WiSe2025', 3, 4, 'AVAILABLE', 1);

-- ============================================================================
-- TEACHER PLANNING CONFIGURATIONS (SoSe2025) - SUMMER SEMESTER  
-- MINIMAL CHANGES: Only 1 teacher drops out for testing
-- ============================================================================
INSERT INTO teacher_pl_configs (teacher_id, school_year, max_praktika_per_year, total_hours_credit, availability_status, active) VALUES
(1001, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1002, 'SoSe2025', 3, 4, 'UNAVAILABLE', 0),  -- DROPPED OUT (illness) - only dropout for testing
(1003, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1004, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1005, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1006, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1007, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1008, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1009, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1010, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1011, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1012, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1013, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1014, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1015, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1016, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1017, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1018, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1019, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1020, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1021, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1022, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1023, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1024, 'SoSe2025', 3, 4, 'AVAILABLE', 1),
(1025, 'SoSe2025', 3, 4, 'AVAILABLE', 1);

-- ============================================================================
-- TEACHER SUBJECT SPECIALIZATIONS (WiSe2025) - WINTER SEMESTER
-- Uses course IDs instead of enum strings
-- ============================================================================
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1001 AND school_year = 'WiSe2025';  -- COMPUTER_SCIENCE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1002 AND school_year = 'WiSe2025';  -- ENGINEERING

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1003 AND school_year = 'WiSe2025';  -- BUSINESS

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1004 AND school_year = 'WiSe2025';  -- MEDICINE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1005 AND school_year = 'WiSe2025';  -- LAW

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1006 AND school_year = 'WiSe2025';  -- ARTS

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1007 AND school_year = 'WiSe2025';  -- SCIENCES

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1008 AND school_year = 'WiSe2025';  -- COMPUTER_SCIENCE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1009 AND school_year = 'WiSe2025';  -- BUSINESS

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1010 AND school_year = 'WiSe2025';  -- MEDICINE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1011 AND school_year = 'WiSe2025';  -- ARTS

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1012 AND school_year = 'WiSe2025';  -- SCIENCES

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1013 AND school_year = 'WiSe2025';  -- COMPUTER_SCIENCE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1014 AND school_year = 'WiSe2025';  -- ENGINEERING

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1015 AND school_year = 'WiSe2025';  -- BUSINESS

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1016 AND school_year = 'WiSe2025';  -- LAW

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1017 AND school_year = 'WiSe2025';  -- MEDICINE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1018 AND school_year = 'WiSe2025';  -- ARTS

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1019 AND school_year = 'WiSe2025';  -- SCIENCES

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1020 AND school_year = 'WiSe2025';  -- COMPUTER_SCIENCE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1021 AND school_year = 'WiSe2025';  -- ENGINEERING

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1022 AND school_year = 'WiSe2025';  -- BUSINESS

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1023 AND school_year = 'WiSe2025';  -- LAW

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1024 AND school_year = 'WiSe2025';  -- MEDICINE

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1025 AND school_year = 'WiSe2025';  -- ARTS

-- ============================================================================
-- TEACHER SUBJECT SPECIALIZATIONS (SoSe2025) - SUMMER SEMESTER
-- ============================================================================
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1001 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1002 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1003 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1004 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1005 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1006 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1007 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1008 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1009 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1010 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1011 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1012 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1013 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1014 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1015 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1016 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1017 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1018 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1019 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1020 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1021 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1022 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1023 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1024 AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1025 AND school_year = 'SoSe2025';

-- ============================================================================
-- TEACHER INTERNSHIP PREFERENCES (WiSe2025) - WINTER SEMESTER
-- ============================================================================

-- Teachers 1001-1012: Prefer PDP_I and ZSP (for GS students)
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_I' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012) AND school_year = 'WiSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012) AND school_year = 'WiSe2025';

-- Teachers 1013-1025: Prefer PDP_II, ZSP, and SFP (for MS students)
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_II' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'WiSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'WiSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'SFP' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'WiSe2025';

-- ============================================================================
-- TEACHER INTERNSHIP PREFERENCES (SoSe2025) - SUMMER SEMESTER
-- ============================================================================

-- Teachers 1001-1012: Prefer PDP_I and ZSP (for GS students)
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_I' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012) AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012) AND school_year = 'SoSe2025';

-- Teachers 1013-1025: Prefer PDP_II, ZSP, and SFP (for MS students)
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_II' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'SoSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'SFP' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'SoSe2025';

-- ============================================================================
-- STUDENT CONFIGURATIONS - WITH SEMESTER SUPPORT USING YEAR FIELD
-- ============================================================================
-- NOTE: Student configs use year field with semester notation:
-- - "WiSe2025" = Wintersemester (Winter Semester) 2025
-- - "SoSe2025" = Sommersemester (Summer Semester) 2025
-- 
-- This enables realistic testing of the reoptimization workflow where:
-- 1. WiSe2025 configs get optimized and saved as baseline with semester="winter"
-- 2. SoSe2025 configs may have different internship requirements
-- 3. Re-optimization preserves WiSe2025 assignments where possible
-- ============================================================================

-- ============================================================================
-- WiSe2025 (WINTER 2025) - GS STUDENTS
-- ============================================================================
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(115, 'WiSe2025', 6, 7, 4, 3, 'GS', 1, 0, 1, 0),  -- ARTS, SCIENCES, MEDICINE, BUSINESS - needs PDP_I and ZSP
(117, 'WiSe2025', 3, 4, 6, 7, 'GS', 1, 0, 0, 0),  -- BUSINESS, MEDICINE, ARTS, SCIENCES - needs PDP_I
(119, 'WiSe2025', 4, 3, 2, 6, 'GS', 0, 0, 1, 0),  -- MEDICINE, BUSINESS, ENGINEERING, ARTS - needs ZSP
(121, 'WiSe2025', 1, 2, 5, 4, 'GS', 1, 0, 1, 0),  -- COMPUTER_SCIENCE, ENGINEERING, LAW, MEDICINE - needs PDP_I and ZSP
(123, 'WiSe2025', 6, 1, 3, 5, 'GS', 1, 0, 0, 0),  -- ARTS, COMPUTER_SCIENCE, BUSINESS, LAW - needs PDP_I
(125, 'WiSe2025', 5, 4, 3, 6, 'GS', 0, 0, 1, 0),  -- LAW, MEDICINE, BUSINESS, ARTS - needs ZSP
(127, 'WiSe2025', 7, 3, 2, 6, 'GS', 1, 0, 0, 0),  -- SCIENCES, BUSINESS, ENGINEERING, ARTS - needs PDP_I
(129, 'WiSe2025', 3, 1, 6, 7, 'GS', 1, 0, 1, 0),  -- BUSINESS, COMPUTER_SCIENCE, ARTS, SCIENCES - needs PDP_I and ZSP
(131, 'WiSe2025', 5, 6, 7, 3, 'GS', 0, 0, 1, 0),  -- LAW, ARTS, SCIENCES, BUSINESS - needs ZSP
(133, 'WiSe2025', 7, 3, 5, 6, 'GS', 1, 0, 0, 0);  -- SCIENCES, BUSINESS, LAW, ARTS - needs PDP_I

-- ============================================================================
-- WiSe2025 (WINTER 2025) - MS STUDENTS
-- ============================================================================
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(116, 'WiSe2025', 1, 2, 3, 5, 'MS', 0, 1, 1, 0),  -- COMPUTER_SCIENCE, ENGINEERING, BUSINESS, LAW - needs PDP_II and ZSP
(118, 'WiSe2025', 7, 5, 6, 1, 'MS', 0, 1, 0, 1),  -- SCIENCES, LAW, ARTS, COMPUTER_SCIENCE - needs PDP_II and SFP
(120, 'WiSe2025', 5, 6, 7, 3, 'MS', 0, 1, 1, 0),  -- LAW, ARTS, SCIENCES, BUSINESS - needs PDP_II and ZSP
(122, 'WiSe2025', 3, 6, 7, 7, 'MS', 0, 1, 0, 1),  -- BUSINESS, ARTS, SCIENCES, SCIENCES - needs PDP_II and SFP
(124, 'WiSe2025', 2, 7, 6, 1, 'MS', 0, 1, 1, 0),  -- ENGINEERING, SCIENCES, ARTS, COMPUTER_SCIENCE - needs PDP_II and ZSP
(126, 'WiSe2025', 4, 6, 7, 1, 'MS', 0, 1, 0, 0),  -- MEDICINE, ARTS, SCIENCES, COMPUTER_SCIENCE - needs PDP_II
(128, 'WiSe2025', 2, 5, 4, 1, 'MS', 0, 1, 1, 1),  -- ENGINEERING, LAW, MEDICINE, COMPUTER_SCIENCE - needs PDP_II, ZSP, and SFP
(130, 'WiSe2025', 1, 2, 3, 4, 'MS', 0, 1, 0, 0),  -- COMPUTER_SCIENCE, ENGINEERING, BUSINESS, MEDICINE - needs PDP_II
(132, 'WiSe2025', 6, 4, 1, 2, 'MS', 0, 1, 1, 0),  -- ARTS, MEDICINE, COMPUTER_SCIENCE, ENGINEERING - needs PDP_II and ZSP
(134, 'WiSe2025', 4, 6, 1, 3, 'MS', 0, 1, 0, 1);  -- MEDICINE, ARTS, COMPUTER_SCIENCE, BUSINESS - needs PDP_II and SFP

-- ============================================================================
-- SoSe2025 (SUMMER 2025) - GS STUDENTS (EXISTING - IDENTICAL to winter)
-- ============================================================================
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(115, 'SoSe2025', 6, 7, 4, 3, 'GS', 1, 0, 1, 0),  -- IDENTICAL: ARTS, SCIENCES, MEDICINE, BUSINESS - PDP_I + ZSP
(117, 'SoSe2025', 3, 4, 6, 7, 'GS', 1, 0, 0, 0),  -- IDENTICAL: BUSINESS, MEDICINE, ARTS, SCIENCES - PDP_I
(119, 'SoSe2025', 4, 3, 2, 6, 'GS', 0, 0, 1, 0),  -- IDENTICAL: MEDICINE, BUSINESS, ENGINEERING, ARTS - ZSP
(121, 'SoSe2025', 1, 2, 5, 4, 'GS', 1, 0, 1, 0),  -- IDENTICAL: COMPUTER_SCIENCE, ENGINEERING, LAW, MEDICINE - PDP_I + ZSP
(123, 'SoSe2025', 6, 1, 3, 5, 'GS', 1, 0, 0, 0),  -- IDENTICAL: ARTS, COMPUTER_SCIENCE, BUSINESS, LAW - PDP_I
(125, 'SoSe2025', 5, 4, 3, 6, 'GS', 0, 0, 1, 0),  -- IDENTICAL: LAW, MEDICINE, BUSINESS, ARTS - ZSP
(127, 'SoSe2025', 7, 3, 2, 6, 'GS', 1, 0, 0, 0),  -- IDENTICAL: SCIENCES, BUSINESS, ENGINEERING, ARTS - PDP_I
(129, 'SoSe2025', 3, 1, 6, 7, 'GS', 1, 0, 1, 0),  -- IDENTICAL: BUSINESS, COMPUTER_SCIENCE, ARTS, SCIENCES - PDP_I + ZSP
(131, 'SoSe2025', 5, 6, 7, 3, 'GS', 0, 0, 1, 0),  -- IDENTICAL: LAW, ARTS, SCIENCES, BUSINESS - ZSP
(133, 'SoSe2025', 7, 3, 5, 6, 'GS', 1, 0, 0, 0);  -- IDENTICAL: SCIENCES, BUSINESS, LAW, ARTS - PDP_I

-- ============================================================================
-- SoSe2025 (SUMMER 2025) - MS STUDENTS (EXISTING - IDENTICAL to winter)
-- ============================================================================
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(116, 'SoSe2025', 1, 2, 3, 5, 'MS', 0, 1, 1, 0),  -- IDENTICAL: COMPUTER_SCIENCE, ENGINEERING, BUSINESS, LAW - PDP_II + ZSP
(118, 'SoSe2025', 7, 5, 6, 1, 'MS', 0, 1, 0, 1),  -- IDENTICAL: SCIENCES, LAW, ARTS, COMPUTER_SCIENCE - PDP_II + SFP
(120, 'SoSe2025', 5, 6, 7, 3, 'MS', 0, 1, 1, 0),  -- IDENTICAL: LAW, ARTS, SCIENCES, BUSINESS - PDP_II + ZSP
(122, 'SoSe2025', 3, 6, 7, 7, 'MS', 0, 1, 0, 1),  -- IDENTICAL: BUSINESS, ARTS, SCIENCES, SCIENCES - PDP_II + SFP
(124, 'SoSe2025', 2, 7, 6, 1, 'MS', 0, 1, 1, 0),  -- IDENTICAL: ENGINEERING, SCIENCES, ARTS, COMPUTER_SCIENCE - PDP_II + ZSP
(126, 'SoSe2025', 4, 6, 7, 1, 'MS', 0, 1, 0, 0),  -- IDENTICAL: MEDICINE, ARTS, SCIENCES, COMPUTER_SCIENCE - PDP_II
(128, 'SoSe2025', 2, 5, 4, 1, 'MS', 0, 1, 1, 1),  -- IDENTICAL: ENGINEERING, LAW, MEDICINE, COMPUTER_SCIENCE - PDP_II + ZSP + SFP
(130, 'SoSe2025', 1, 2, 3, 4, 'MS', 0, 1, 0, 0),  -- IDENTICAL: COMPUTER_SCIENCE, ENGINEERING, BUSINESS, MEDICINE - PDP_II
(132, 'SoSe2025', 6, 4, 1, 2, 'MS', 0, 1, 1, 0),  -- IDENTICAL: ARTS, MEDICINE, COMPUTER_SCIENCE, ENGINEERING - PDP_II + ZSP
(134, 'SoSe2025', 4, 6, 1, 3, 'MS', 0, 1, 0, 1);  -- IDENTICAL: MEDICINE, ARTS, COMPUTER_SCIENCE, BUSINESS - PDP_II + SFP

-- ============================================================================
-- SoSe2025 (SUMMER 2025) - NEW STUDENTS joining the program (REDUCED TO 3)
-- ============================================================================
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(201, 'SoSe2025', 1, 3, 5, 2, 'GS', 1, 0, 0, 0),  -- NEW: COMPUTER_SCIENCE, BUSINESS, LAW, ENGINEERING - PDP_I
(202, 'SoSe2025', 4, 6, 7, 1, 'GS', 1, 0, 1, 0),  -- NEW: MEDICINE, ARTS, SCIENCES, COMPUTER_SCIENCE - PDP_I + ZSP
(203, 'SoSe2025', 2, 1, 4, 3, 'GS', 0, 0, 1, 0);  -- NEW: ENGINEERING, COMPUTER_SCIENCE, MEDICINE, BUSINESS - ZSP

-- ============================================================================
-- COMPLETED INTERNSHIPS - Using course IDs
-- ============================================================================
INSERT INTO completed_internships (start_date, end_date, student_id, id, school_id, teacher_id, description, course_id) VALUES
('2024-06-01', '2024-08-31', 115, 1, 15, 1001, 'Summer internship in IT', 1),              -- COMPUTER_SCIENCE
('2024-07-01', '2024-09-30', 116, 2, 16, 1002, 'Engineering project work', 2),             -- ENGINEERING
('2024-05-15', '2024-08-15', 117, 3, 17, 1003, 'Business case study', 3),                  -- BUSINESS
('2024-06-10', '2024-09-10', 118, 4, 15, 1004, 'Medical research assistant', 4),           -- MEDICINE
('2024-07-05', '2024-10-05', 119, 5, 16, 1005, 'Law firm internship', 5),                  -- LAW
('2024-06-20', '2024-09-20', 120, 6, 17, 1006, 'Arts exhibition preparation', 6),          -- ARTS
('2024-07-15', '2024-10-15', 121, 7, 15, 1007, 'Science lab work', 7),                     -- SCIENCES
('2024-05-25', '2024-08-25', 122, 8, 16, 1008, 'Software development project', 1),         -- COMPUTER_SCIENCE
('2024-06-05', '2024-09-05', 123, 9, 17, 1009, 'Business consultancy internship', 3),      -- BUSINESS
('2024-07-10', '2024-10-10', 124, 10, 15, 1010, 'Medical observation program', 4),         -- MEDICINE
('2024-06-01', '2024-08-31', 125, 11, 15, 1001, 'Law research assistant', 5),              -- LAW
('2024-07-01', '2024-09-30', 126, 12, 16, 1002, 'Arts portfolio development', 6),          -- ARTS
('2024-05-15', '2024-08-15', 127, 13, 17, 1003, 'Scientific experiment internship', 7),    -- SCIENCES
('2024-06-10', '2024-09-10', 128, 14, 15, 1004, 'Software engineering internship', 1),     -- COMPUTER_SCIENCE
('2024-07-05', '2024-10-05', 129, 15, 16, 1005, 'Business analytics project', 3),          -- BUSINESS
('2024-06-20', '2024-09-20', 130, 16, 17, 1006, 'Medical lab rotation', 4),                -- MEDICINE
('2024-07-15', '2024-10-15', 131, 17, 15, 1007, 'Law case research', 5),                   -- LAW
('2024-05-25', '2024-08-25', 132, 18, 16, 1008, 'Arts curation internship', 6),            -- ARTS
('2024-06-05', '2024-09-05', 133, 19, 17, 1009, 'Scientific field work', 7),               -- SCIENCES
('2024-07-10', '2024-10-10', 134, 20, 15, 1010, 'Computer science project', 1);            -- COMPUTER_SCIENCE
