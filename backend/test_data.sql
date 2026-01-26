INSERT INTO `courses` (`id`, `name`, `active`) VALUES 
(1, 'COMPUTER_SCIENCE', true),
(2, 'ENGINEERING', true),
(3, 'BUSINESS', true),
(4, 'MEDICINE', true),
(5, 'LAW', true),
(6, 'ARTS', true),
(7, 'SCIENCES', true);

INSERT INTO `schools` (`active`, `oepnv`, `id`, `address`, `name`, `zone`, `type`) VALUES
(1, 'FOUR_A', 15, '123 Main St', 'Central School', '1', 'MS'),
(1, 'NA', 16, '456 Elm St', 'North High', '2', 'GS'),
(1, 'FOUR_B', 17, '789 Oak St', 'East Academy', '3', 'MS'),
(1, 'FOUR_A', 18, '321 Maple Ave', 'West Elementary', '1', 'GS'),
(1, 'NA', 19, '654 Pine Rd', 'South Middle School', '2', 'MS'),
(1, 'FOUR_B', 20, '987 Cedar Blvd', 'Downtown Academy', '3', 'GS'),
(1, 'FOUR_A', 21, '147 Birch Ln', 'Riverside School', '1', 'MS'),
(1, 'NA', 22, '258 Walnut St', 'Hillside Elementary', '2', 'GS'),
(1, 'FOUR_B', 23, '369 Spruce Dr', 'Lakeside Middle', '3', 'MS'),
(1, 'FOUR_A', 24, '741 Ash Ct', 'Harbor View School', '1', 'GS'),
(1, 'FOUR_A', 101, '100 Test St', 'Test GS School Zone 1', '1', 'GS'),
(1, 'FOUR_B', 102, '200 Test Ave', 'Test MS School Zone 2', '2', 'MS'),
(1, 'FOUR_A', 103, '300 Test Blvd', 'Test GS School Zone 2', '2', 'GS');

INSERT INTO teachers (school_id, teacher_id, email, first_name, last_name, main_subject_id, is_part_time, active) VALUES 
(15, 1001, 'john.doe@example.com', 'John', 'Doe', 1, 0, 1),
(16, 1002, 'mary.smith@example.com', 'Mary', 'Smith', 2, 0, 1),
(17, 1003, 'kevin.brown@example.com', 'Kevin', 'Brown', 3, 0, 1),
(15, 1004, 'lisa.white@example.com', 'Lisa', 'White', 4, 0, 1),
(16, 1005, 'peter.johnson@example.com', 'Peter', 'Johnson', 5, 0, 1),
(17, 1006, 'anna.davis@example.com', 'Anna', 'Davis', 6, 0, 1),
(15, 1007, 'mike.miller@example.com', 'Mike', 'Miller', 7, 0, 1),
(16, 1008, 'susan.moore@example.com', 'Susan', 'Moore', 1, 0, 1),
(17, 1009, 'david.taylor@example.com', 'David', 'Taylor', 3, 0, 1),
(15, 1010, 'emily.anderson@example.com', 'Emily', 'Anderson', 4, 0, 1),
(18, 1011, 'robert.wilson@example.com', 'Robert', 'Wilson', 6, 0, 1),
(19, 1012, 'sarah.martinez@example.com', 'Sarah', 'Martinez', 7, 0, 1),
(20, 1013, 'james.garcia@example.com', 'James', 'Garcia', 1, 0, 1),
(21, 1014, 'jennifer.rodriguez@example.com', 'Jennifer', 'Rodriguez', 2, 0, 1),
(22, 1015, 'william.lee@example.com', 'William', 'Lee', 3, 0, 1),
(23, 1016, 'elizabeth.walker@example.com', 'Elizabeth', 'Walker', 5, 0, 1),
(24, 1017, 'michael.hall@example.com', 'Michael', 'Hall', 4, 0, 1),
(15, 1018, 'linda.young@example.com', 'Linda', 'Young', 6, 0, 1),
(16, 1019, 'thomas.king@example.com', 'Thomas', 'King', 7, 0, 1),
(17, 1020, 'patricia.wright@example.com', 'Patricia', 'Wright', 1, 0, 1),
(18, 1021, 'christopher.lopez@example.com', 'Christopher', 'Lopez', 2, 0, 1),
(19, 1022, 'barbara.hill@example.com', 'Barbara', 'Hill', 3, 0, 1),
(20, 1023, 'daniel.scott@example.com', 'Daniel', 'Scott', 5, 0, 1),
(21, 1024, 'nancy.green@example.com', 'Nancy', 'Green', 4, 0, 1),
(22, 1025, 'matthew.adams@example.com', 'Matthew', 'Adams', 6, 0, 1),
(101, 2001, 'alice@test.com', 'Alice', 'Anderson', 1, 0, 1),
(101, 2002, 'bob@test.com', 'Bob', 'Brown', 3, 0, 1),
(102, 2003, 'carol@test.com', 'Carol', 'Clark', 2, 0, 1);

INSERT INTO students (birth_date, matriculation_nbr, oriented, registred, city, country, description, email, first_name, house_nbr, last_name, phone, postal_code, semester_city, semester_country, semester_house_nbr, semester_postal_code, semester_street, street, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type) VALUES
('2004-04-12', 115, b'1', b'1', 'Tunis', 'Tunisia', NULL, 'student1@example.com', 'Imen', '16', 'Ben Youssef', '6516512330', '1000', NULL, NULL, NULL, NULL, 'Linden St', 'Linden St', 6, 7, 4, 3, 'GS'),
('2003-07-22', 116, b'0', b'1', 'Berlin', 'Germany', NULL, 'student2@example.com', 'Lukas', '12', 'Schmidt', '491512345678', '10115', NULL, NULL, NULL, NULL, 'Maple St', 'Maple St', 1, 2, 3, 5, 'MS'),
('2005-01-30', 117, b'1', b'0', 'Paris', 'France', NULL, 'student3@example.com', 'Claire', '5', 'Dubois', '33123456789', '75001', NULL, NULL, NULL, NULL, 'Oak St', 'Oak St', 3, 4, 6, 7, 'GS'),
('2004-11-10', 118, b'1', b'1', 'Rome', 'Italy', NULL, 'student4@example.com', 'Marco', '8', 'Rossi', '390612345678', '00100', NULL, NULL, NULL, NULL, 'Pine St', 'Pine St', 7, 5, 6, 1, 'MS'),
('2003-03-18', 119, b'0', b'0', 'Madrid', 'Spain', NULL, 'student5@example.com', 'Sofia', '21', 'Gonzalez', '34912345678', '28001', NULL, NULL, NULL, NULL, 'Birch St', 'Birch St', 4, 3, 2, 6, 'GS'),
('2005-08-25', 120, b'1', b'1', 'Lisbon', 'Portugal', NULL, 'student6@example.com', 'Miguel', '3', 'Silva', '351912345678', '1000-001', NULL, NULL, NULL, NULL, 'Cedar St', 'Cedar St', 5, 6, 7, 3, 'MS'),
('2004-06-14', 121, b'0', b'1', 'Vienna', 'Austria', NULL, 'student7@example.com', 'Anna', '11', 'Muller', '43123456789', '1010', NULL, NULL, NULL, NULL, 'Spruce St', 'Spruce St', 1, 2, 5, 4, 'GS'),
('2003-12-02', 122, b'1', b'0', 'Zurich', 'Switzerland', NULL, 'student8@example.com', 'Luca', '7', 'Meier', '41791234567', '8001', NULL, NULL, NULL, NULL, 'Willow St', 'Willow St', 3, 6, 7, 7, 'MS'),
('2005-05-05', 123, b'0', b'1', 'Brussels', 'Belgium', NULL, 'student9@example.com', 'Emma', '9', 'Peeters', '32475123456', '1000', NULL, NULL, NULL, NULL, 'Fir St', 'Fir St', 6, 1, 3, 5, 'GS'),
('2004-09-17', 124, b'1', b'1', 'Amsterdam', 'Netherlands', NULL, 'student10@example.com', 'Jan', '14', 'De Vries', '31201234567', '1012', NULL, NULL, NULL, NULL, 'Elm St', 'Elm St', 2, 7, 6, 1, 'MS'),
('2003-02-28', 125, b'0', b'0', 'Oslo', 'Norway', NULL, 'student11@example.com', 'Nora', '6', 'Hansen', '4720123456', '0150', NULL, NULL, NULL, NULL, 'Poplar St', 'Poplar St', 5, 4, 3, 6, 'GS'),
('2005-10-11', 126, b'1', b'1', 'Helsinki', 'Finland', NULL, 'student12@example.com', 'Aino', '2', 'Korhonen', '35891234567', '00100', NULL, NULL, NULL, NULL, 'Aspen St', 'Aspen St', 4, 6, 7, 1, 'MS'),
('2004-04-01', 127, b'0', b'1', 'Copenhagen', 'Denmark', NULL, 'student13@example.com', 'Freja', '10', 'Jensen', '451234567', '1000', NULL, NULL, NULL, NULL, 'Cypress St', 'Cypress St', 7, 3, 2, 6, 'GS'),
('2003-07-07', 128, b'1', b'0', 'Stockholm', 'Sweden', NULL, 'student14@example.com', 'Erik', '18', 'Lindberg', '4612345678', '111 22', NULL, NULL, NULL, NULL, 'Maple St', 'Maple St', 2, 5, 4, 1, 'MS'),
('2005-03-20', 129, b'0', b'1', 'Reykjavik', 'Iceland', NULL, 'student15@example.com', 'Bjorn', '1', 'Sigurdsson', '3541234567', '101', NULL, NULL, NULL, NULL, 'Oak St', 'Oak St', 3, 1, 6, 7, 'GS'),
('2004-08-30', 130, b'1', b'1', 'Dublin', 'Ireland', NULL, 'student16@example.com', 'Aoife', '20', 'OBrien', '35312345678', '1000', NULL, NULL, NULL, NULL, 'Pine St', 'Pine St', 1, 2, 3, 4, 'MS'),
('2003-11-05', 131, b'0', b'0', 'London', 'UK', NULL, 'student17@example.com', 'Harry', '15', 'Smith', '441234567890', 'SW1A', NULL, NULL, NULL, NULL, 'Birch St', 'Birch St', 5, 6, 7, 3, 'GS'),
('2005-06-18', 132, b'1', b'1', 'Edinburgh', 'UK', NULL, 'student18@example.com', 'Isla', '8', 'Brown', '441234567891', 'EH1', NULL, NULL, NULL, NULL, 'Cedar St', 'Cedar St', 6, 4, 1, 2, 'MS'),
('2004-12-12', 133, b'0', b'1', 'Manchester', 'UK', NULL, 'student19@example.com', 'Oliver', '9', 'Taylor', '441234567892', 'M1', NULL, NULL, NULL, NULL, 'Spruce St', 'Spruce St', 7, 3, 5, 6, 'GS'),
('2003-05-09', 134, b'1', b'0', 'Birmingham', 'UK', NULL, 'student20@example.com', 'Amelia', '11', 'Wilson', '441234567893', 'B1', NULL, NULL, NULL, NULL, 'Willow St', 'Willow St', 4, 6, 1, 3, 'MS');

INSERT INTO teacher_pl_configs (teacher_id, school_year, max_praktika_per_year, total_hours_credit, active) VALUES
(1001, 'WiSe2025', 3, 4, 1),
(1002, 'WiSe2025', 3, 4, 1),
(1003, 'WiSe2025', 3, 4, 1),
(1004, 'WiSe2025', 3, 4, 1),
(1005, 'WiSe2025', 3, 4, 1),
(1006, 'WiSe2025', 3, 4, 1),
(1007, 'WiSe2025', 3, 4, 1),
(1008, 'WiSe2025', 3, 4, 1),
(1009, 'WiSe2025', 3, 4, 1),
(1010, 'WiSe2025', 3, 4, 1),
(1011, 'WiSe2025', 3, 4, 1),
(1012, 'WiSe2025', 3, 4, 1),
(1013, 'WiSe2025', 3, 4, 1),
(1014, 'WiSe2025', 3, 4, 1),
(1015, 'WiSe2025', 3, 4, 1),
(1016, 'WiSe2025', 3, 4, 1),
(1017, 'WiSe2025', 3, 4, 1),
(1018, 'WiSe2025', 3, 4, 1),
(1019, 'WiSe2025', 3, 4, 1),
(1020, 'WiSe2025', 3, 4, 1),
(1021, 'WiSe2025', 3, 4, 1),
(1022, 'WiSe2025', 3, 4, 1),
(1023, 'WiSe2025', 3, 4, 1),
(1024, 'WiSe2025', 3, 4, 1),
(1025, 'WiSe2025', 3, 4, 1);

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1001 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1002 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1003 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1004 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1005 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1006 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1007 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1008 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1009 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1010 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1011 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1012 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1013 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1014 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1015 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1016 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1017 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1018 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id = 1019 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 1020 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 1021 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 1022 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id = 1023 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id = 1024 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id = 1025 AND school_year = 'WiSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_I' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012) AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012) AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_II' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'SFP' FROM teacher_pl_configs WHERE teacher_id IN (1013, 1014, 1015, 1016, 1017, 1018, 1019, 1020, 1021, 1022, 1023, 1024, 1025) AND school_year = 'WiSe2025';

INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(115, 'WiSe2025', 6, 7, 4, 3, 'GS', 1, 0, 1, 0),
(117, 'WiSe2025', 3, 4, 6, 7, 'GS', 1, 0, 0, 0),
(119, 'WiSe2025', 4, 3, 2, 6, 'GS', 0, 0, 1, 0),
(121, 'WiSe2025', 1, 2, 5, 4, 'GS', 1, 0, 1, 0),
(123, 'WiSe2025', 6, 1, 3, 5, 'GS', 1, 0, 0, 0),
(125, 'WiSe2025', 5, 4, 3, 6, 'GS', 0, 0, 1, 0),
(127, 'WiSe2025', 7, 3, 2, 6, 'GS', 1, 0, 0, 0),
(129, 'WiSe2025', 3, 1, 6, 7, 'GS', 1, 0, 1, 0),
(131, 'WiSe2025', 5, 6, 7, 3, 'GS', 0, 0, 1, 0),
(133, 'WiSe2025', 7, 3, 5, 6, 'GS', 1, 0, 0, 0);

INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(116, 'WiSe2025', 1, 2, 3, 5, 'MS', 0, 1, 1, 0),
(118, 'WiSe2025', 7, 5, 6, 1, 'MS', 0, 1, 0, 1),
(120, 'WiSe2025', 5, 6, 7, 3, 'MS', 0, 1, 1, 0),
(122, 'WiSe2025', 3, 6, 7, 7, 'MS', 0, 1, 0, 1),
(124, 'WiSe2025', 2, 7, 6, 1, 'MS', 0, 1, 1, 0),
(126, 'WiSe2025', 4, 6, 7, 1, 'MS', 0, 1, 0, 0),
(128, 'WiSe2025', 2, 5, 4, 1, 'MS', 0, 1, 1, 1),
(130, 'WiSe2025', 1, 2, 3, 4, 'MS', 0, 1, 0, 0),
(132, 'WiSe2025', 6, 4, 1, 2, 'MS', 0, 1, 1, 0),
(134, 'WiSe2025', 4, 6, 1, 3, 'MS', 0, 1, 0, 1);

INSERT INTO completed_internships (student_id, id, school_id, teacher_id, description, course_id, type) VALUES
(115, 1, 15, 1001, 'Summer internship in IT', 1, 'PDP_I'),
(116, 2, 16, 1002, 'Engineering project work', 2, 'PDP_II'),
(117, 3, 17, 1003, 'Business case study', 3, 'ZSP'),
(118, 4, 15, 1004, 'Medical research assistant', 4, 'SFP'),
(119, 5, 16, 1005, 'Law firm internship', 5, 'PDP_I'),
(120, 6, 17, 1006, 'Arts exhibition preparation', 6, 'PDP_II'),
(121, 7, 15, 1007, 'Science lab work', 7, 'ZSP'),
(122, 8, 16, 1008, 'Software development project', 1, 'PDP_I'),
(123, 9, 17, 1009, 'Business consultancy internship', 3, 'PDP_II'),
(124, 10, 15, 1010, 'Medical observation program', 4, 'SFP'),
(125, 11, 15, 1001, 'Law research assistant', 5, 'PDP_I'),
(126, 12, 16, 1002, 'Arts portfolio development', 6, 'PDP_II'),
(127, 13, 17, 1003, 'Scientific experiment internship', 7, 'ZSP'),
(128, 14, 15, 1004, 'Software engineering internship', 1, 'PDP_II'),
(129, 15, 16, 1005, 'Business analytics project', 3, 'ZSP'),
(130, 16, 17, 1006, 'Medical lab rotation', 4, 'SFP'),
(131, 17, 15, 1007, 'Law case research', 5, 'PDP_I'),
(132, 18, 16, 1008, 'Arts curation internship', 6, 'PDP_II'),
(133, 19, 17, 1009, 'Scientific field work', 7, 'ZSP'),
(134, 20, 15, 1010, 'Computer science project', 1, 'SFP');

INSERT INTO students (birth_date, matriculation_nbr, oriented, registred, city, country, email, first_name, house_nbr, last_name, phone, postal_code, street, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type) VALUES
('2005-01-01', 201, b'1', b'1', 'Berlin', 'Germany', 'student201@test.com', 'David', '1', 'Davis', '123456', '10001', 'Test St', 1, 3, 6, 7, 'GS'),
('2005-02-02', 202, b'1', b'1', 'Berlin', 'Germany', 'student202@test.com', 'Eva', '2', 'Evans', '123457', '10002', 'Test Ave', 3, 1, 6, 7, 'GS'),
('2005-03-03', 203, b'1', b'1', 'Berlin', 'Germany', 'student203@test.com', 'Frank', '3', 'Fisher', '123458', '10003', 'Test Rd', 2, 1, 3, 7, 'MS');

INSERT INTO teacher_pl_configs (teacher_id, school_year, max_praktika_per_year, total_hours_credit, active) VALUES
(2001, 'WiSe2025', 3, 4, 1),
(2002, 'WiSe2025', 3, 4, 1),
(2003, 'WiSe2025', 3, 4, 1);

INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id = 2001 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id = 2002 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id = 2003 AND school_year = 'WiSe2025';

INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_I' FROM teacher_pl_configs WHERE teacher_id = 2001 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id = 2001 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_I' FROM teacher_pl_configs WHERE teacher_id = 2002 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id = 2002 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_II' FROM teacher_pl_configs WHERE teacher_id = 2003 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id = 2003 AND school_year = 'WiSe2025';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'SFP' FROM teacher_pl_configs WHERE teacher_id = 2003 AND school_year = 'WiSe2025';

INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(201, 'WiSe2025', 1, 3, 6, 7, 'GS', 1, 0, 0, 0);
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(202, 'WiSe2025', 3, 1, 6, 7, 'GS', 0, 0, 1, 0);
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(203, 'WiSe2025', 2, 1, 3, 7, 'MS', 0, 1, 0, 0);
