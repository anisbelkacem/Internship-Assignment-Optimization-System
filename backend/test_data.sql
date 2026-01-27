INSERT INTO `courses` (`id`, `name`, `active`) VALUES 
(1, 'COMPUTER_SCIENCE', true),
(2, 'ENGINEERING', true),
(3, 'BUSINESS', true),
(4, 'MEDICINE', true),
(5, 'LAW', true),
(6, 'ARTS', true),
(7, 'SCIENCES', true);

-- Schools in Bavaria near Passau (within 150km radius)
INSERT INTO `schools` (`active`, `oepnv`, `id`, `address`, `name`, `zone`, `type`) VALUES
-- Passau area schools
(1, 'FOUR_A', 15, 'Innstraße 12', 'Grundschule Passau-Innstadt', '1', 'GS'),
(1, 'FOUR_B', 16, 'Spitalhofstraße 40', 'Mittelschule Passau-St.Nikola', '1', 'MS'),
(1, 'FOUR_A', 17, 'Neuburger Straße 96', 'Grundschule Passau-Neustift', '1', 'GS'),
(1, 'NA', 18, 'Grubweg 68', 'Mittelschule Passau-Grubweg', '2', 'MS'),

-- Vilshofen schools (20km from Passau)
(1, 'FOUR_B', 19, 'Kapuzinerstraße 17', 'Grundschule Vilshofen', '2', 'GS'),
(1, 'FOUR_A', 20, 'Jahnstraße 2', 'Mittelschule Vilshofen', '2', 'MS'),

-- Pocking schools (30km from Passau)
(1, 'NA', 21, 'Schulstraße 12', 'Grundschule Pocking', '2', 'GS'),
(1, 'FOUR_B', 22, 'Indlinger Straße 16', 'Mittelschule Pocking', '3', 'MS'),

-- Waldkirchen schools (35km from Passau)
(1, 'FOUR_A', 23, 'Ringmauerstraße 14', 'Grundschule Waldkirchen', '3', 'GS'),
(1, 'FOUR_B', 24, 'Lärchenweg 8', 'Mittelschule Waldkirchen', '3', 'MS'),

-- Freyung schools (40km from Passau)
(1, 'FOUR_A', 25, 'St.-Gunther-Straße 52', 'Grundschule Freyung', '3', 'GS'),
(1, 'NA', 26, 'Jahnstraße 10', 'Mittelschule Freyung', '3', 'MS'),

-- Grafenau schools (45km from Passau)
(1, 'FOUR_B', 27, 'Bärnzeller Straße 12', 'Grundschule Grafenau', '3', 'GS'),
(1, 'FOUR_A', 28, 'Rachelweg 24', 'Mittelschule Grafenau', '3', 'MS'),

-- Deggendorf schools (35km from Passau)
(1, 'FOUR_A', 29, 'Konrad-Adenauer-Straße 11', 'Grundschule Deggendorf-Mitte', '2', 'GS'),
(1, 'FOUR_B', 30, 'Pfleggasse 16', 'Mittelschule Deggendorf', '2', 'MS'),
(1, 'NA', 31, 'Edlmairstraße 39', 'Grundschule Deggendorf-Rettenbach', '2', 'GS'),

-- Hauzenberg schools (25km from Passau)
(1, 'FOUR_A', 32, 'Schulstraße 26', 'Grundschule Hauzenberg', '2', 'GS'),
(1, 'FOUR_B', 33, 'Eckmühlstraße 8', 'Mittelschule Hauzenberg', '2', 'MS'),

-- Ortenburg schools (28km from Passau)
(1, 'NA', 34, 'Schloßberg 7', 'Grundschule Ortenburg', '2', 'GS'),

-- Bad Griesbach schools (40km from Passau)
(1, 'FOUR_A', 35, 'Seilerberg 36', 'Grundschule Bad Griesbach', '2', 'GS'),
(1, 'FOUR_B', 36, 'Thermalbadstraße 23', 'Mittelschule Bad Griesbach', '3', 'MS'),

-- Fürstenzell schools (18km from Passau)
(1, 'FOUR_A', 37, 'Klosterstraße 19', 'Grundschule Fürstenzell', '1', 'GS'),

-- Regen schools (60km from Passau)
(1, 'FOUR_B', 38, 'Lärchenweg 32', 'Grundschule Regen', '3', 'GS'),
(1, 'FOUR_A', 39, 'Amtsgerichtstraße 6', 'Mittelschule Regen', '3', 'MS'),

-- Wegscheid schools (22km from Passau)
(1, 'NA', 40, 'Schulstraße 14', 'Grundschule Wegscheid', '2', 'GS'),
(1, 'FOUR_B', 41, 'Marktplatz 18', 'Mittelschule Wegscheid', '2', 'MS');

-- Teachers from Bavaria/Passau area with realistic German names
INSERT INTO teachers (school_id, teacher_id, email, first_name, last_name, main_subject_id, is_part_time, active) VALUES 
-- Passau schools teachers
(15, 1001, 'hans.mueller@gs-passau.de', 'Hans', 'Müller', 1, 0, 1),
(15, 1002, 'maria.schmidt@gs-passau.de', 'Maria', 'Schmidt', 7, 0, 1),
(16, 1003, 'thomas.weber@ms-passau.de', 'Thomas', 'Weber', 2, 0, 1),
(16, 1004, 'petra.wagner@ms-passau.de', 'Petra', 'Wagner', 4, 0, 1),
(17, 1005, 'michael.bauer@gs-neustift.de', 'Michael', 'Bauer', 1, 0, 1),
(17, 1006, 'sabine.fischer@gs-neustift.de', 'Sabine', 'Fischer', 6, 0, 1),
(18, 1007, 'klaus.schneider@ms-grubweg.de', 'Klaus', 'Schneider', 3, 0, 1),
(18, 1008, 'brigitte.hoffmann@ms-grubweg.de', 'Brigitte', 'Hoffmann', 5, 0, 1),

-- Vilshofen teachers
(19, 1009, 'stefan.schulz@gs-vilshofen.de', 'Stefan', 'Schulz', 1, 0, 1),
(19, 1010, 'andrea.koch@gs-vilshofen.de', 'Andrea', 'Koch', 7, 0, 1),
(20, 1011, 'wolfgang.becker@ms-vilshofen.de', 'Wolfgang', 'Becker', 2, 0, 1),
(20, 1012, 'monika.richter@ms-vilshofen.de', 'Monika', 'Richter', 4, 0, 1),

-- Pocking teachers
(21, 1013, 'jürgen.klein@gs-pocking.de', 'Jürgen', 'Klein', 1, 0, 1),
(21, 1014, 'christine.wolf@gs-pocking.de', 'Christine', 'Wolf', 6, 0, 1),
(22, 1015, 'helmut.schröder@ms-pocking.de', 'Helmut', 'Schröder', 3, 0, 1),
(22, 1016, 'karin.neumann@ms-pocking.de', 'Karin', 'Neumann', 5, 0, 1),

-- Waldkirchen teachers
(23, 1017, 'rainer.schwarz@gs-waldkirchen.de', 'Rainer', 'Schwarz', 7, 0, 1),
(23, 1018, 'susanne.zimmermann@gs-waldkirchen.de', 'Susanne', 'Zimmermann', 1, 0, 1),
(24, 1019, 'martin.braun@ms-waldkirchen.de', 'Martin', 'Braun', 2, 0, 1),
(24, 1020, 'renate.hartmann@ms-waldkirchen.de', 'Renate', 'Hartmann', 4, 0, 1),

-- Freyung teachers
(25, 1021, 'georg.lange@gs-freyung.de', 'Georg', 'Lange', 1, 0, 1),
(25, 1022, 'ute.schmitt@gs-freyung.de', 'Ute', 'Schmitt', 6, 0, 1),
(26, 1023, 'peter.krause@ms-freyung.de', 'Peter', 'Krause', 3, 0, 1),
(26, 1024, 'heike.meier@ms-freyung.de', 'Heike', 'Meier', 5, 0, 1),

-- Grafenau teachers
(27, 1025, 'frank.lehmann@gs-grafenau.de', 'Frank', 'Lehmann', 7, 0, 1),
(27, 1026, 'elisabeth.köhler@gs-grafenau.de', 'Elisabeth', 'Köhler', 1, 0, 1),
(28, 1027, 'bernd.könig@ms-grafenau.de', 'Bernd', 'König', 2, 0, 1),
(28, 1028, 'angelika.fuchs@ms-grafenau.de', 'Angelika', 'Fuchs', 4, 0, 1),

-- Deggendorf teachers
(29, 1029, 'dieter.walter@gs-deggendorf.de', 'Dieter', 'Walter', 1, 0, 1),
(29, 1030, 'gabriele.frank@gs-deggendorf.de', 'Gabriele', 'Frank', 6, 0, 1),
(30, 1031, 'horst.gruber@ms-deggendorf.de', 'Horst', 'Gruber', 3, 0, 1),
(30, 1032, 'margit.huber@ms-deggendorf.de', 'Margit', 'Huber', 5, 0, 1),
(31, 1033, 'werner.steiner@gs-rettenbach.de', 'Werner', 'Steiner', 7, 0, 1),
(31, 1034, 'ingrid.herrmann@gs-rettenbach.de', 'Ingrid', 'Herrmann', 1, 0, 1),

-- Hauzenberg teachers
(32, 1035, 'günter.vogel@gs-hauzenberg.de', 'Günter', 'Vogel', 1, 0, 1),
(32, 1036, 'hildegard.jung@gs-hauzenberg.de', 'Hildegard', 'Jung', 6, 0, 1),
(33, 1037, 'manfred.hofmann@ms-hauzenberg.de', 'Manfred', 'Hofmann', 2, 0, 1),
(33, 1038, 'erika.krüger@ms-hauzenberg.de', 'Erika', 'Krüger', 4, 0, 1),

-- Ortenburg teachers
(34, 1039, 'franz.gross@gs-ortenburg.de', 'Franz', 'Groß', 1, 0, 1),
(34, 1040, 'marianne.förster@gs-ortenburg.de', 'Marianne', 'Förster', 7, 0, 1),

-- Bad Griesbach teachers
(35, 1041, 'ludwig.lechner@gs-badgriesbach.de', 'Ludwig', 'Lechner', 1, 0, 1),
(35, 1042, 'anna.huber@gs-badgriesbach.de', 'Anna', 'Huber', 6, 0, 1),
(36, 1043, 'josef.mayer@ms-badgriesbach.de', 'Josef', 'Mayer', 3, 0, 1),
(36, 1044, 'theresia.bauer@ms-badgriesbach.de', 'Theresia', 'Bauer', 5, 0, 1),

-- Fürstenzell teachers
(37, 1045, 'johann.pfeifer@gs-fuerstenzell.de', 'Johann', 'Pfeifer', 1, 0, 1),
(37, 1046, 'rosa.schuster@gs-fuerstenzell.de', 'Rosa', 'Schuster', 7, 0, 1),

-- Regen teachers
(38, 1047, 'anton.seidel@gs-regen.de', 'Anton', 'Seidel', 1, 0, 1),
(38, 1048, 'gerda.wimmer@gs-regen.de', 'Gerda', 'Wimmer', 6, 0, 1),
(39, 1049, 'alois.graf@ms-regen.de', 'Alois', 'Graf', 2, 0, 1),
(39, 1050, 'christa.berger@ms-regen.de', 'Christa', 'Berger', 4, 0, 1),

-- Wegscheid teachers
(40, 1051, 'max.koller@gs-wegscheid.de', 'Max', 'Koller', 1, 0, 1),
(40, 1052, 'franziska.roth@gs-wegscheid.de', 'Franziska', 'Roth', 7, 0, 1),
(41, 1053, 'sebastian.lang@ms-wegscheid.de', 'Sebastian', 'Lang', 3, 0, 1),
(41, 1054, 'claudia.kaiser@ms-wegscheid.de', 'Claudia', 'Kaiser', 5, 0, 1),

-- Additional versatile teachers who can teach all internship types
(15, 1055, 'markus.lechner@gs-passau.de', 'Markus', 'Lechner', 1, 0, 1),
(16, 1056, 'angelika.weber@ms-passau.de', 'Angelika', 'Weber', 2, 0, 1),
(17, 1057, 'hermann.schmid@gs-neustift.de', 'Hermann', 'Schmid', 6, 0, 1),
(18, 1058, 'eva.braun@ms-grubweg.de', 'Eva', 'Braun', 4, 0, 1),
(19, 1059, 'ludwig.fischer@gs-vilshofen.de', 'Ludwig', 'Fischer', 7, 0, 1),
(20, 1060, 'ursula.meyer@ms-vilshofen.de', 'Ursula', 'Meyer', 3, 0, 1),
(21, 1061, 'richard.huber@gs-pocking.de', 'Richard', 'Huber', 1, 0, 1),
(22, 1062, 'marie.kraus@ms-pocking.de', 'Marie', 'Kraus', 5, 0, 1),
(23, 1063, 'otto.schneider@gs-waldkirchen.de', 'Otto', 'Schneider', 6, 0, 1),
(24, 1064, 'helga.winter@ms-waldkirchen.de', 'Helga', 'Winter', 2, 0, 1),
(25, 1065, 'karl.beck@gs-freyung.de', 'Karl', 'Beck', 7, 0, 1),
(26, 1066, 'gisela.wolf@ms-freyung.de', 'Gisela', 'Wolf', 4, 0, 1),
(27, 1067, 'alfred.koch@gs-grafenau.de', 'Alfred', 'Koch', 1, 0, 1),
(28, 1068, 'rosemarie.schulz@ms-grafenau.de', 'Rosemarie', 'Schulz', 3, 0, 1),
(29, 1069, 'heinz.becker@gs-deggendorf.de', 'Heinz', 'Becker', 6, 0, 1),
(30, 1070, 'ingeborg.richter@ms-deggendorf.de', 'Ingeborg', 'Richter', 5, 0, 1),
(31, 1071, 'walter.klein@gs-rettenbach.de', 'Walter', 'Klein', 7, 0, 1),
(32, 1072, 'gertrud.schwarz@gs-hauzenberg.de', 'Gertrud', 'Schwarz', 1, 0, 1),
(33, 1073, 'bruno.zimmermann@ms-hauzenberg.de', 'Bruno', 'Zimmermann', 2, 0, 1),
(34, 1074, 'elfriede.hartmann@gs-ortenburg.de', 'Elfriede', 'Hartmann', 6, 0, 1),
(35, 1075, 'fritz.lange@gs-badgriesbach.de', 'Fritz', 'Lange', 7, 0, 1),
(36, 1076, 'hilde.schmitt@ms-badgriesbach.de', 'Hilde', 'Schmitt', 4, 0, 1),
(37, 1077, 'erwin.krause@gs-fuerstenzell.de', 'Erwin', 'Krause', 1, 0, 1),
(38, 1078, 'lieselotte.meier@gs-regen.de', 'Lieselotte', 'Meier', 6, 0, 1),
(39, 1079, 'erich.lehmann@ms-regen.de', 'Erich', 'Lehmann', 3, 0, 1),
(40, 1080, 'gertrude.köhler@gs-wegscheid.de', 'Gertrude', 'Köhler', 7, 0, 1),
(41, 1081, 'herbert.fuchs@ms-wegscheid.de', 'Herbert', 'Fuchs', 5, 0, 1);

-- Students from Bavaria/Passau area with realistic German names and addresses
INSERT INTO students (birth_date, matriculation_nbr, oriented, registred, city, country, description, email, first_name, house_nbr, last_name, phone, postal_code, semester_city, semester_country, semester_house_nbr, semester_postal_code, semester_street, street, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type) VALUES
-- Passau students
('2004-03-15', 115, b'1', b'1', 'Passau', 'Deutschland', NULL, 'lukas.weber@student.de', 'Lukas', '23', 'Weber', '+4985143521', '94032', NULL, NULL, NULL, NULL, 'Innstraße', 'Innstraße', 1, 2, 3, 7, 'GS'),
('2003-07-22', 116, b'1', b'1', 'Passau', 'Deutschland', NULL, 'julia.huber@student.de', 'Julia', '15', 'Huber', '+4985143522', '94032', NULL, NULL, NULL, NULL, 'Ludwigstraße', 'Ludwigstraße', 3, 1, 6, 7, 'MS'),
('2005-01-10', 117, b'0', b'1', 'Passau', 'Deutschland', NULL, 'felix.maier@student.de', 'Felix', '8', 'Maier', '+4985143523', '94036', NULL, NULL, NULL, NULL, 'Grubweg', 'Grubweg', 7, 5, 6, 1, 'GS'),
('2004-11-28', 118, b'1', b'1', 'Passau', 'Deutschland', NULL, 'emma.schmidt@student.de', 'Emma', '42', 'Schmidt', '+4985143524', '94034', NULL, NULL, NULL, NULL, 'Neuburger Straße', 'Neuburger Straße', 2, 1, 4, 6, 'MS'),

-- Vilshofen students
('2003-05-18', 119, b'1', b'0', 'Vilshofen', 'Deutschland', NULL, 'max.zimmermann@student.de', 'Max', '17', 'Zimmermann', '+4985416743', '94474', NULL, NULL, NULL, NULL, 'Stadtplatz', 'Stadtplatz', 1, 2, 3, 5, 'GS'),
('2005-09-03', 120, b'0', b'1', 'Vilshofen', 'Deutschland', NULL, 'lena.bauer@student.de', 'Lena', '29', 'Bauer', '+4985416744', '94474', NULL, NULL, NULL, NULL, 'Kapuzinerstraße', 'Kapuzinerstraße', 4, 3, 6, 7, 'MS'),
('2004-02-14', 121, b'1', b'1', 'Vilshofen', 'Deutschland', NULL, 'paul.fischer@student.de', 'Paul', '11', 'Fischer', '+4985416745', '94474', NULL, NULL, NULL, NULL, 'Donaulände', 'Donaulände', 6, 7, 1, 3, 'GS'),

-- Pocking students
('2003-12-07', 122, b'0', b'1', 'Pocking', 'Deutschland', NULL, 'anna.meyer@student.de', 'Anna', '34', 'Meyer', '+4985316891', '94060', NULL, NULL, NULL, NULL, 'Indlinger Straße', 'Indlinger Straße', 3, 2, 5, 6, 'MS'),
('2005-06-25', 123, b'1', b'0', 'Pocking', 'Deutschland', NULL, 'jonas.koch@student.de', 'Jonas', '7', 'Koch', '+4985316892', '94060', NULL, NULL, NULL, NULL, 'Schulstraße', 'Schulstraße', 5, 4, 1, 2, 'GS'),
('2004-08-19', 124, b'1', b'1', 'Pocking', 'Deutschland', NULL, 'sophie.wolf@student.de', 'Sophie', '56', 'Wolf', '+4985316893', '94060', NULL, NULL, NULL, NULL, 'Gartenstraße', 'Gartenstraße', 7, 6, 3, 1, 'MS'),

-- Waldkirchen students
('2003-04-11', 125, b'0', b'1', 'Waldkirchen', 'Deutschland', NULL, 'david.schuster@student.de', 'David', '19', 'Schuster', '+4985835421', '94065', NULL, NULL, NULL, NULL, 'Ringmauerstraße', 'Ringmauerstraße', 1, 3, 7, 4, 'GS'),
('2005-10-30', 126, b'1', b'1', 'Waldkirchen', 'Deutschland', NULL, 'marie.lange@student.de', 'Marie', '22', 'Lange', '+4985835422', '94065', NULL, NULL, NULL, NULL, 'Freyunger Straße', 'Freyunger Straße', 2, 1, 5, 6, 'MS'),
('2004-01-08', 127, b'1', b'0', 'Waldkirchen', 'Deutschland', NULL, 'tim.hofmann@student.de', 'Tim', '3', 'Hofmann', '+4985835423', '94065', NULL, NULL, NULL, NULL, 'Lärchenweg', 'Lärchenweg', 6, 7, 2, 4, 'GS'),

-- Freyung students
('2003-09-16', 128, b'0', b'1', 'Freyung', 'Deutschland', NULL, 'lisa.richter@student.de', 'Lisa', '45', 'Richter', '+4985512345', '94078', NULL, NULL, NULL, NULL, 'Hauptstraße', 'Hauptstraße', 4, 6, 3, 1, 'MS'),
('2005-03-21', 129, b'1', b'1', 'Freyung', 'Deutschland', NULL, 'noah.klein@student.de', 'Noah', '12', 'Klein', '+4985512346', '94078', NULL, NULL, NULL, NULL, 'St.-Gunther-Straße', 'St.-Gunther-Straße', 1, 2, 7, 5, 'GS'),
('2004-07-05', 130, b'1', b'0', 'Freyung', 'Deutschland', NULL, 'mia.krause@student.de', 'Mia', '28', 'Krause', '+4985512347', '94078', NULL, NULL, NULL, NULL, 'Wolfkerstraße', 'Wolfkerstraße', 3, 4, 1, 6, 'MS'),

-- Grafenau students
('2003-11-14', 131, b'0', b'1', 'Grafenau', 'Deutschland', NULL, 'leon.braun@student.de', 'Leon', '9', 'Braun', '+4985523456', '94481', NULL, NULL, NULL, NULL, 'Steinberg', 'Steinberg', 7, 1, 3, 5, 'GS'),
('2005-05-27', 132, b'1', b'1', 'Grafenau', 'Deutschland', NULL, 'laura.schneider@student.de', 'Laura', '37', 'Schneider', '+4985523457', '94481', NULL, NULL, NULL, NULL, 'Rachelweg', 'Rachelweg', 5, 6, 2, 4, 'MS'),
('2004-12-09', 133, b'1', b'1', 'Grafenau', 'Deutschland', NULL, 'ben.schwarz@student.de', 'Ben', '16', 'Schwarz', '+4985523458', '94481', NULL, NULL, NULL, NULL, 'Bärnzeller Straße', 'Bärnzeller Straße', 2, 3, 7, 1, 'GS'),

-- Deggendorf students
('2003-06-02', 134, b'0', b'0', 'Deggendorf', 'Deutschland', NULL, 'hannah.hartmann@student.de', 'Hannah', '51', 'Hartmann', '+49991234561', '94469', NULL, NULL, NULL, NULL, 'Konrad-Adenauer-Straße', 'Konrad-Adenauer-Straße', 4, 5, 6, 2, 'MS'),
('2005-02-18', 135, b'1', b'1', 'Deggendorf', 'Deutschland', NULL, 'elias.neumann@student.de', 'Elias', '24', 'Neumann', '+49991234562', '94469', NULL, NULL, NULL, NULL, 'Pfleggasse', 'Pfleggasse', 1, 3, 5, 7, 'GS'),
('2004-10-11', 136, b'1', b'1', 'Deggendorf', 'Deutschland', NULL, 'clara.jung@student.de', 'Clara', '8', 'Jung', '+49991234563', '94469', NULL, NULL, NULL, NULL, 'Edlmairstraße', 'Edlmairstraße', 6, 1, 4, 3, 'MS'),
('2003-08-24', 137, b'0', b'1', 'Deggendorf', 'Deutschland', NULL, 'moritz.lehmann@student.de', 'Moritz', '33', 'Lehmann', '+49991234564', '94469', NULL, NULL, NULL, NULL, 'Luitpoldplatz', 'Luitpoldplatz', 3, 2, 6, 7, 'GS'),

-- Hauzenberg students
('2005-04-13', 138, b'1', b'0', 'Hauzenberg', 'Deutschland', NULL, 'amelie.vogel@student.de', 'Amelie', '14', 'Vogel', '+4985868734', '94051', NULL, NULL, NULL, NULL, 'Schulstraße', 'Schulstraße', 7, 5, 1, 3, 'MS'),
('2004-09-29', 139, b'1', b'1', 'Hauzenberg', 'Deutschland', NULL, 'finn.gross@student.de', 'Finn', '41', 'Groß', '+4985868735', '94051', NULL, NULL, NULL, NULL, 'Eckmühlstraße', 'Eckmühlstraße', 1, 7, 4, 6, 'GS'),
('2003-01-17', 140, b'0', b'1', 'Hauzenberg', 'Deutschland', NULL, 'lea.förster@student.de', 'Lea', '5', 'Förster', '+4985868736', '94051', NULL, NULL, NULL, NULL, 'Passauer Straße', 'Passauer Straße', 5, 3, 2, 1, 'MS'),

-- Bad Griesbach students
('2005-07-08', 141, b'1', b'1', 'Bad Griesbach', 'Deutschland', NULL, 'jakob.herrmann@student.de', 'Jakob', '18', 'Herrmann', '+4985329876', '94086', NULL, NULL, NULL, NULL, 'Seilerberg', 'Seilerberg', 2, 1, 5, 3, 'GS'),
('2004-03-26', 142, b'0', b'1', 'Bad Griesbach', 'Deutschland', NULL, 'nele.pfeifer@student.de', 'Nele', '27', 'Pfeifer', '+4985329877', '94086', NULL, NULL, NULL, NULL, 'Thermalbadstraße', 'Thermalbadstraße', 4, 6, 7, 2, 'MS'),

-- Ortenburg students
('2003-10-12', 143, b'1', b'0', 'Ortenburg', 'Deutschland', NULL, 'simon.seidel@student.de', 'Simon', '6', 'Seidel', '+4985425543', '94496', NULL, NULL, NULL, NULL, 'Schloßberg', 'Schloßberg', 6, 3, 1, 4, 'GS'),
('2005-12-01', 144, b'1', b'1', 'Ortenburg', 'Deutschland', NULL, 'emily.wimmer@student.de', 'Emily', '21', 'Wimmer', '+4985425544', '94496', NULL, NULL, NULL, NULL, 'Marktstraße', 'Marktstraße', 1, 7, 2, 5, 'GS'),

-- Fürstenzell students
('2004-05-19', 145, b'0', b'1', 'Fürstenzell', 'Deutschland', NULL, 'aaron.berger@student.de', 'Aaron', '13', 'Berger', '+4985028821', '94081', NULL, NULL, NULL, NULL, 'Klosterstraße', 'Klosterstraße', 3, 4, 6, 1, 'GS'),
('2003-02-28', 146, b'1', b'1', 'Fürstenzell', 'Deutschland', NULL, 'charlotte.koller@student.de', 'Charlotte', '39', 'Koller', '+4985028822', '94081', NULL, NULL, NULL, NULL, 'Passauer Straße', 'Passauer Straße', 7, 5, 3, 2, 'GS'),

-- Regen students
('2005-08-07', 147, b'1', b'0', 'Regen', 'Deutschland', NULL, 'louis.roth@student.de', 'Louis', '25', 'Roth', '+4992137654', '94209', NULL, NULL, NULL, NULL, 'Lärchenweg', 'Lärchenweg', 1, 2, 4, 6, 'GS'),
('2004-11-23', 148, b'0', b'1', 'Regen', 'Deutschland', NULL, 'johanna.graf@student.de', 'Johanna', '48', 'Graf', '+4992137655', '94209', NULL, NULL, NULL, NULL, 'Amtsgerichtstraße', 'Amtsgerichtstraße', 5, 3, 7, 1, 'MS'),

-- Wegscheid students
('2003-07-15', 149, b'1', b'1', 'Wegscheid', 'Deutschland', NULL, 'henry.lang@student.de', 'Henry', '10', 'Lang', '+4985929443', '94110', NULL, NULL, NULL, NULL, 'Marktplatz', 'Marktplatz', 2, 6, 4, 3, 'MS'),
('2005-01-04', 150, b'1', b'1', 'Wegscheid', 'Deutschland', NULL, 'valentina.kaiser@student.de', 'Valentina', '32', 'Kaiser', '+4985929444', '94110', NULL, NULL, NULL, NULL, 'Schulstraße', 'Schulstraße', 6, 1, 5, 7, 'GS'),

-- Additional Passau students
('2004-04-20', 151, b'0', b'1', 'Passau', 'Deutschland', NULL, 'maximilian.mayer@student.de', 'Maximilian', '56', 'Mayer', '+4985143525', '94032', NULL, NULL, NULL, NULL, 'Bahnhofstraße', 'Bahnhofstraße', 7, 3, 1, 5, 'GS'),
('2003-09-12', 152, b'1', b'0', 'Passau', 'Deutschland', NULL, 'sophia.lehner@student.de', 'Sophia', '18', 'Lehner', '+4985143526', '94034', NULL, NULL, NULL, NULL, 'Domplatz', 'Domplatz', 5, 2, 4, 6, 'MS'),
('2005-11-08', 153, b'1', b'1', 'Passau', 'Deutschland', NULL, 'tobias.walter@student.de', 'Tobias', '44', 'Walter', '+4985143527', '94036', NULL, NULL, NULL, NULL, 'Höllgasse', 'Höllgasse', 1, 6, 7, 3, 'GS'),
('2004-06-30', 154, b'0', b'1', 'Passau', 'Deutschland', NULL, 'elena.steiner@student.de', 'Elena', '27', 'Steiner', '+4985143528', '94032', NULL, NULL, NULL, NULL, 'Residenzplatz', 'Residenzplatz', 4, 5, 2, 1, 'MS'),

-- Additional Vilshofen students
('2003-03-14', 155, b'1', b'1', 'Vilshofen', 'Deutschland', NULL, 'sebastian.frank@student.de', 'Sebastian', '38', 'Frank', '+4985416746', '94474', NULL, NULL, NULL, NULL, 'Passauer Straße', 'Passauer Straße', 3, 7, 6, 2, 'MS'),
('2005-07-22', 156, b'0', b'0', 'Vilshofen', 'Deutschland', NULL, 'hannah.gruber@student.de', 'Hannah', '9', 'Gruber', '+4985416747', '94474', NULL, NULL, NULL, NULL, 'Vilshofener Straße', 'Vilshofener Straße', 6, 4, 1, 5, 'GS'),
('2004-10-05', 157, b'1', b'1', 'Vilshofen', 'Deutschland', NULL, 'niklas.hauer@student.de', 'Niklas', '51', 'Hauer', '+4985416748', '94474', NULL, NULL, NULL, NULL, 'Donaustraße', 'Donaustraße', 2, 3, 5, 7, 'MS'),

-- Additional Pocking students
('2005-02-18', 158, b'1', b'0', 'Pocking', 'Deutschland', NULL, 'amelie.wimmer@student.de', 'Amelie', '23', 'Wimmer', '+4985316894', '94060', NULL, NULL, NULL, NULL, 'Hauptstraße', 'Hauptstraße', 1, 7, 4, 2, 'GS'),
('2003-08-27', 159, b'0', b'1', 'Pocking', 'Deutschland', NULL, 'robin.bergmann@student.de', 'Robin', '65', 'Bergmann', '+4985316895', '94060', NULL, NULL, NULL, NULL, 'Rathausplatz', 'Rathausplatz', 5, 6, 3, 4, 'MS'),
('2004-12-14', 160, b'1', b'1', 'Pocking', 'Deutschland', NULL, 'leonie.jung@student.de', 'Leonie', '41', 'Jung', '+4085316896', '94060', NULL, NULL, NULL, NULL, 'Kirchenstraße', 'Kirchenstraße', 7, 2, 6, 1, 'GS'),

-- Additional Waldkirchen students
('2003-05-09', 161, b'1', b'0', 'Waldkirchen', 'Deutschland', NULL, 'daniel.köhler@student.de', 'Daniel', '30', 'Köhler', '+4985835424', '94065', NULL, NULL, NULL, NULL, 'Schulberg', 'Schulberg', 2, 4, 3, 6, 'MS'),
('2005-09-23', 162, b'0', b'1', 'Waldkirchen', 'Deutschland', NULL, 'isabella.schmid@student.de', 'Isabella', '12', 'Schmid', '+4985835425', '94065', NULL, NULL, NULL, NULL, 'Stadtplatz', 'Stadtplatz', 6, 1, 7, 5, 'GS'),

-- Additional Freyung students
('2004-01-28', 163, b'1', b'1', 'Freyung', 'Deutschland', NULL, 'julian.müller@student.de', 'Julian', '55', 'Müller', '+4985512348', '94078', NULL, NULL, NULL, NULL, 'Grafenauer Straße', 'Grafenauer Straße', 3, 5, 2, 7, 'MS'),
('2005-06-11', 164, b'0', b'1', 'Freyung', 'Deutschland', NULL, 'melissa.hofer@student.de', 'Melissa', '7', 'Hofer', '+4985512349', '94078', NULL, NULL, NULL, NULL, 'Jahnstraße', 'Jahnstraße', 1, 3, 6, 4, 'GS'),

-- Additional Grafenau students
('2003-10-19', 165, b'1', b'0', 'Grafenau', 'Deutschland', NULL, 'florian.fuchs@student.de', 'Florian', '26', 'Fuchs', '+4985523459', '94481', NULL, NULL, NULL, NULL, 'Rosenau', 'Rosenau', 4, 2, 7, 1, 'MS'),
('2005-03-31', 166, b'1', b'1', 'Grafenau', 'Deutschland', NULL, 'victoria.huber@student.de', 'Victoria', '49', 'Huber', '+4985523460', '94481', NULL, NULL, NULL, NULL, 'Freyunger Straße', 'Freyunger Straße', 6, 5, 3, 2, 'GS'),

-- Additional Deggendorf students
('2004-07-16', 167, b'0', b'0', 'Deggendorf', 'Deutschland', NULL, 'alexander.wagner@student.de', 'Alexander', '62', 'Wagner', '+49991234565', '94469', NULL, NULL, NULL, NULL, 'Stadtplatz', 'Stadtplatz', 2, 1, 5, 3, 'MS'),
('2005-11-29', 168, b'1', b'1', 'Deggendorf', 'Deutschland', NULL, 'sarah.berger@student.de', 'Sarah', '13', 'Berger', '+49991234566', '94469', NULL, NULL, NULL, NULL, 'Oberer Stadtplatz', 'Oberer Stadtplatz', 7, 6, 4, 1, 'GS'),
('2003-04-07', 169, b'1', b'1', 'Deggendorf', 'Deutschland', NULL, 'gabriel.bauer@student.de', 'Gabriel', '35', 'Bauer', '+49991234567', '94469', NULL, NULL, NULL, NULL, 'Deggenauer Straße', 'Deggenauer Straße', 5, 3, 2, 6, 'MS'),

-- Additional Hauzenberg students
('2005-05-12', 170, b'0', b'1', 'Hauzenberg', 'Deutschland', NULL, 'katharina.schmidt@student.de', 'Katharina', '58', 'Schmidt', '+4985868737', '94051', NULL, NULL, NULL, NULL, 'Marktplatz', 'Marktplatz', 1, 2, 7, 5, 'GS'),
('2004-08-25', 171, b'1', b'0', 'Hauzenberg', 'Deutschland', NULL, 'raphael.weber@student.de', 'Raphael', '22', 'Weber', '+4985868738', '94051', NULL, NULL, NULL, NULL, 'Stadtplatz', 'Stadtplatz', 3, 6, 4, 1, 'MS'),

-- Additional Bad Griesbach students
('2003-12-03', 172, b'1', b'1', 'Bad Griesbach', 'Deutschland', NULL, 'michelle.möller@student.de', 'Michelle', '46', 'Möller', '+4985329878', '94086', NULL, NULL, NULL, NULL, 'Kurallee', 'Kurallee', 4, 7, 5, 2, 'MS'),
('2005-04-17', 173, b'0', b'1', 'Bad Griesbach', 'Deutschland', NULL, 'constantin.winkler@student.de', 'Constantin', '11', 'Winkler', '+4985329879', '94086', NULL, NULL, NULL, NULL, 'Kurstraße', 'Kurstraße', 6, 3, 1, 7, 'GS'),

-- Additional Ortenburg students
('2004-09-08', 174, b'1', b'1', 'Ortenburg', 'Deutschland', NULL, 'jasmin.gärtner@student.de', 'Jasmin', '33', 'Gärtner', '+4985425545', '94496', NULL, NULL, NULL, NULL, 'Schlossstraße', 'Schlossstraße', 7, 4, 2, 5, 'GS'),

-- Additional Fürstenzell students
('2003-06-21', 175, b'0', b'0', 'Fürstenzell', 'Deutschland', NULL, 'adrian.schäfer@student.de', 'Adrian', '25', 'Schäfer', '+4985028823', '94081', NULL, NULL, NULL, NULL, 'Marktplatz', 'Marktplatz', 2, 5, 6, 3, 'MS'),
('2005-10-14', 176, b'1', b'1', 'Fürstenzell', 'Deutschland', NULL, 'vanessa.kraus@student.de', 'Vanessa', '50', 'Kraus', '+4985028824', '94081', NULL, NULL, NULL, NULL, 'Vilshofener Straße', 'Vilshofener Straße', 1, 7, 4, 6, 'GS'),

-- Additional Regen students
('2004-02-26', 177, b'1', b'0', 'Regen', 'Deutschland', NULL, 'fabio.herrmann@student.de', 'Fabio', '17', 'Herrmann', '+4992137656', '94209', NULL, NULL, NULL, NULL, 'Stadtplatz', 'Stadtplatz', 5, 2, 3, 1, 'MS'),
('2005-12-19', 178, b'0', b'1', 'Regen', 'Deutschland', NULL, 'alina.pfeifer@student.de', 'Alina', '40', 'Pfeifer', '+4992137657', '94209', NULL, NULL, NULL, NULL, 'Bahnhofstraße', 'Bahnhofstraße', 3, 6, 7, 4, 'GS'),

-- Additional Wegscheid students
('2003-11-11', 179, b'1', b'1', 'Wegscheid', 'Deutschland', NULL, 'vincent.hoffmann@student.de', 'Vincent', '19', 'Hoffmann', '+4985929445', '94110', NULL, NULL, NULL, NULL, 'Passauer Straße', 'Passauer Straße', 4, 3, 5, 2, 'MS'),
('2005-08-02', 180, b'1', b'1', 'Wegscheid', 'Deutschland', NULL, 'chiara.schuster@student.de', 'Chiara', '54', 'Schuster', '+4985929446', '94110', NULL, NULL, NULL, NULL, 'Kirchplatz', 'Kirchplatz', 6, 7, 1, 3, 'GS');

-- Teacher PL configurations for WiSe25-26
INSERT INTO teacher_pl_configs (teacher_id, school_year, max_praktika_per_year, total_hours_credit, active) VALUES
-- All teachers get configs for winter semester
(1001, 'WiSe25-26', 3, 4, 1), (1002, 'WiSe25-26', 3, 4, 1), (1003, 'WiSe25-26', 3, 4, 1),
(1004, 'WiSe25-26', 3, 4, 1), (1005, 'WiSe25-26', 3, 4, 1), (1006, 'WiSe25-26', 3, 4, 1),
(1007, 'WiSe25-26', 3, 4, 1), (1008, 'WiSe25-26', 3, 4, 1), (1009, 'WiSe25-26', 3, 4, 1),
(1010, 'WiSe25-26', 3, 4, 1), (1011, 'WiSe25-26', 3, 4, 1), (1012, 'WiSe25-26', 3, 4, 1),
(1013, 'WiSe25-26', 3, 4, 1), (1014, 'WiSe25-26', 3, 4, 1), (1015, 'WiSe25-26', 3, 4, 1),
(1016, 'WiSe25-26', 3, 4, 1), (1017, 'WiSe25-26', 3, 4, 1), (1018, 'WiSe25-26', 3, 4, 1),
(1019, 'WiSe25-26', 3, 4, 1), (1020, 'WiSe25-26', 3, 4, 1), (1021, 'WiSe25-26', 3, 4, 1),
(1022, 'WiSe25-26', 3, 4, 1), (1023, 'WiSe25-26', 3, 4, 1), (1024, 'WiSe25-26', 3, 4, 1),
(1025, 'WiSe25-26', 3, 4, 1), (1026, 'WiSe25-26', 3, 4, 1), (1027, 'WiSe25-26', 3, 4, 1),
(1028, 'WiSe25-26', 3, 4, 1), (1029, 'WiSe25-26', 3, 4, 1), (1030, 'WiSe25-26', 3, 4, 1),
(1031, 'WiSe25-26', 3, 4, 1), (1032, 'WiSe25-26', 3, 4, 1), (1033, 'WiSe25-26', 3, 4, 1),
(1034, 'WiSe25-26', 3, 4, 1), (1035, 'WiSe25-26', 3, 4, 1), (1036, 'WiSe25-26', 3, 4, 1),
(1037, 'WiSe25-26', 3, 4, 1), (1038, 'WiSe25-26', 3, 4, 1), (1039, 'WiSe25-26', 3, 4, 1),
(1040, 'WiSe25-26', 3, 4, 1), (1041, 'WiSe25-26', 3, 4, 1), (1042, 'WiSe25-26', 3, 4, 1),
(1043, 'WiSe25-26', 3, 4, 1), (1044, 'WiSe25-26', 3, 4, 1), (1045, 'WiSe25-26', 3, 4, 1),
(1046, 'WiSe25-26', 3, 4, 1), (1047, 'WiSe25-26', 3, 4, 1), (1048, 'WiSe25-26', 3, 4, 1),
(1049, 'WiSe25-26', 3, 4, 1), (1050, 'WiSe25-26', 3, 4, 1), (1051, 'WiSe25-26', 3, 4, 1),
(1052, 'WiSe25-26', 3, 4, 1), (1053, 'WiSe25-26', 3, 4, 1), (1054, 'WiSe25-26', 3, 4, 1),
-- Versatile teachers who can teach all internship types
(1055, 'WiSe25-26', 4, 6, 1), (1056, 'WiSe25-26', 4, 6, 1), (1057, 'WiSe25-26', 4, 6, 1),
(1058, 'WiSe25-26', 4, 6, 1), (1059, 'WiSe25-26', 4, 6, 1), (1060, 'WiSe25-26', 4, 6, 1),
(1061, 'WiSe25-26', 4, 6, 1), (1062, 'WiSe25-26', 4, 6, 1), (1063, 'WiSe25-26', 4, 6, 1),
(1064, 'WiSe25-26', 4, 6, 1), (1065, 'WiSe25-26', 4, 6, 1), (1066, 'WiSe25-26', 4, 6, 1),
(1067, 'WiSe25-26', 4, 6, 1), (1068, 'WiSe25-26', 4, 6, 1), (1069, 'WiSe25-26', 4, 6, 1),
(1070, 'WiSe25-26', 4, 6, 1), (1071, 'WiSe25-26', 4, 6, 1), (1072, 'WiSe25-26', 4, 6, 1),
(1073, 'WiSe25-26', 4, 6, 1), (1074, 'WiSe25-26', 4, 6, 1), (1075, 'WiSe25-26', 4, 6, 1),
(1076, 'WiSe25-26', 4, 6, 1), (1077, 'WiSe25-26', 4, 6, 1), (1078, 'WiSe25-26', 4, 6, 1),
(1079, 'WiSe25-26', 4, 6, 1), (1080, 'WiSe25-26', 4, 6, 1), (1081, 'WiSe25-26', 4, 6, 1);

-- Teacher course preferences
-- GS teachers (PDP_I + ZSP)
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id IN (1001, 1005, 1009, 1013, 1017, 1021, 1025, 1029, 1033, 1035, 1039, 1041, 1045, 1047, 1051) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id IN (1002, 1010, 1017, 1022, 1025, 1033, 1040, 1046, 1048, 1052) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id IN (1006, 1014, 1018, 1022, 1026, 1030, 1036, 1042, 1048) AND school_year = 'WiSe25-26';

-- MS teachers (PDP_II + ZSP + SFP)
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id IN (1003, 1011, 1019, 1027, 1037, 1049) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id IN (1007, 1015, 1023, 1031, 1043, 1053) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id IN (1004, 1012, 1020, 1028, 1038, 1050) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id IN (1008, 1016, 1024, 1032, 1044, 1054) AND school_year = 'WiSe25-26';

-- Versatile teachers - can teach all courses (1-7)
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 1 FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 2 FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 3 FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 4 FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 5 FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 6 FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_courses (pl_config_id, course_id)
SELECT pl_config_id, 7 FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';

-- Teacher internship type preferences
-- GS teachers prefer PDP_I and ZSP
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_I' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1005, 1006, 1009, 1010, 1013, 1014, 1017, 1018, 1021, 1022, 1025, 1026, 1029, 1030, 1033, 1034, 1035, 1036, 1039, 1040, 1041, 1042, 1045, 1046, 1047, 1048, 1051, 1052) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1001, 1002, 1005, 1006, 1009, 1010, 1013, 1014, 1017, 1018, 1021, 1022, 1025, 1026, 1029, 1030, 1033, 1034, 1035, 1036, 1039, 1040, 1041, 1042, 1045, 1046, 1047, 1048, 1051, 1052) AND school_year = 'WiSe25-26';

-- MS teachers prefer PDP_II, ZSP, and SFP
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_II' FROM teacher_pl_configs WHERE teacher_id IN (1003, 1004, 1007, 1008, 1011, 1012, 1015, 1016, 1019, 1020, 1023, 1024, 1027, 1028, 1031, 1032, 1037, 1038, 1043, 1044, 1049, 1050, 1053, 1054) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id IN (1003, 1004, 1007, 1008, 1011, 1012, 1015, 1016, 1019, 1020, 1023, 1024, 1027, 1028, 1031, 1032, 1037, 1038, 1043, 1044, 1049, 1050, 1053, 1054) AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'SFP' FROM teacher_pl_configs WHERE teacher_id IN (1003, 1004, 1007, 1008, 1011, 1012, 1015, 1016, 1019, 1020, 1023, 1024, 1027, 1028, 1031, 1032, 1037, 1038, 1043, 1044, 1049, 1050, 1053, 1054) AND school_year = 'WiSe25-26';

-- Versatile teachers can teach ALL 4 internship types (PDP_I, PDP_II, ZSP, SFP)
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_I' FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'PDP_II' FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'ZSP' FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';
INSERT INTO teacher_pl_internship_prefs (pl_config_id, preference)
SELECT pl_config_id, 'SFP' FROM teacher_pl_configs WHERE teacher_id BETWEEN 1055 AND 1081 AND school_year = 'WiSe25-26';

-- Student configurations for WiSe25-26
-- GS students (get PDP_I and/or ZSP)
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(115, 'WiSe25-26', 1, 2, 3, 7, 'GS', 1, 0, 1, 0),
(117, 'WiSe25-26', 7, 5, 6, 1, 'GS', 1, 0, 0, 0),
(119, 'WiSe25-26', 1, 2, 3, 5, 'GS', 0, 0, 1, 0),
(121, 'WiSe25-26', 6, 7, 1, 3, 'GS', 1, 0, 1, 0),
(123, 'WiSe25-26', 5, 4, 1, 2, 'GS', 1, 0, 0, 0),
(125, 'WiSe25-26', 1, 3, 7, 4, 'GS', 0, 0, 1, 0),
(127, 'WiSe25-26', 6, 7, 2, 4, 'GS', 1, 0, 0, 0),
(129, 'WiSe25-26', 1, 2, 7, 5, 'GS', 1, 0, 1, 0),
(131, 'WiSe25-26', 7, 1, 3, 5, 'GS', 0, 0, 1, 0),
(133, 'WiSe25-26', 2, 3, 7, 1, 'GS', 1, 0, 0, 0),
(135, 'WiSe25-26', 1, 3, 5, 7, 'GS', 1, 0, 1, 0),
(137, 'WiSe25-26', 3, 2, 6, 7, 'GS', 0, 0, 1, 0),
(139, 'WiSe25-26', 1, 7, 4, 6, 'GS', 1, 0, 0, 0),
(141, 'WiSe25-26', 2, 1, 5, 3, 'GS', 1, 0, 1, 0),
(143, 'WiSe25-26', 6, 3, 1, 4, 'GS', 0, 0, 1, 0),
(144, 'WiSe25-26', 1, 7, 2, 5, 'GS', 1, 0, 0, 0),
(145, 'WiSe25-26', 3, 4, 6, 1, 'GS', 1, 0, 1, 0),
(146, 'WiSe25-26', 7, 5, 3, 2, 'GS', 0, 0, 1, 0),
(147, 'WiSe25-26', 1, 2, 4, 6, 'GS', 1, 0, 0, 0),
(150, 'WiSe25-26', 6, 1, 5, 7, 'GS', 1, 0, 1, 0),
(151, 'WiSe25-26', 7, 3, 1, 5, 'GS', 0, 0, 1, 0),
(153, 'WiSe25-26', 1, 6, 7, 3, 'GS', 1, 0, 0, 0),
(156, 'WiSe25-26', 6, 4, 1, 5, 'GS', 1, 0, 1, 0),
(158, 'WiSe25-26', 1, 7, 4, 2, 'GS', 0, 0, 1, 0),
(160, 'WiSe25-26', 7, 2, 6, 1, 'GS', 1, 0, 0, 0),
(162, 'WiSe25-26', 6, 1, 7, 5, 'GS', 1, 0, 1, 0),
(164, 'WiSe25-26', 1, 3, 6, 4, 'GS', 0, 0, 1, 0),
(166, 'WiSe25-26', 6, 5, 3, 2, 'GS', 1, 0, 0, 0),
(168, 'WiSe25-26', 7, 6, 4, 1, 'GS', 1, 0, 1, 0),
(170, 'WiSe25-26', 1, 2, 7, 5, 'GS', 0, 0, 1, 0),
(173, 'WiSe25-26', 6, 3, 1, 7, 'GS', 1, 0, 0, 0),
(174, 'WiSe25-26', 7, 4, 2, 5, 'GS', 1, 0, 1, 0),
(176, 'WiSe25-26', 1, 7, 4, 6, 'GS', 0, 0, 1, 0),
(178, 'WiSe25-26', 3, 6, 7, 4, 'GS', 1, 0, 0, 0),
(180, 'WiSe25-26', 6, 7, 1, 3, 'GS', 1, 0, 1, 0);

-- MS students (get PDP_II and/or ZSP and/or SFP)
INSERT INTO student_config (student_matriculation_nbr, year, main_course_id, pref_course1_id, pref_course2_id, pref_course3_id, school_type, pdpi, pdpii, zsp, sfp) VALUES
(116, 'WiSe25-26', 3, 1, 6, 7, 'MS', 0, 1, 1, 0),
(118, 'WiSe25-26', 2, 1, 4, 6, 'MS', 0, 1, 0, 1),
(120, 'WiSe25-26', 4, 3, 6, 7, 'MS', 0, 1, 1, 0),
(122, 'WiSe25-26', 3, 2, 5, 6, 'MS', 0, 1, 0, 1),
(124, 'WiSe25-26', 7, 6, 3, 1, 'MS', 0, 1, 1, 0),
(126, 'WiSe25-26', 2, 1, 5, 6, 'MS', 0, 1, 0, 0),
(128, 'WiSe25-26', 4, 6, 3, 1, 'MS', 0, 1, 1, 1),
(130, 'WiSe25-26', 3, 4, 1, 6, 'MS', 0, 1, 0, 0),
(132, 'WiSe25-26', 5, 6, 2, 4, 'MS', 0, 1, 1, 0),
(134, 'WiSe25-26', 4, 5, 6, 2, 'MS', 0, 1, 0, 1),
(136, 'WiSe25-26', 6, 1, 4, 3, 'MS', 0, 1, 1, 0),
(138, 'WiSe25-26', 7, 5, 1, 3, 'MS', 0, 1, 0, 1),
(140, 'WiSe25-26', 5, 3, 2, 1, 'MS', 0, 1, 1, 0),
(142, 'WiSe25-26', 4, 6, 7, 2, 'MS', 0, 1, 0, 1),
(148, 'WiSe25-26', 5, 3, 7, 1, 'MS', 0, 1, 1, 0),
(149, 'WiSe25-26', 2, 6, 4, 3, 'MS', 0, 1, 0, 1),
(152, 'WiSe25-26', 5, 2, 4, 6, 'MS', 0, 1, 1, 0),
(154, 'WiSe25-26', 4, 5, 2, 1, 'MS', 0, 1, 0, 1),
(155, 'WiSe25-26', 3, 7, 6, 2, 'MS', 0, 1, 1, 0),
(157, 'WiSe25-26', 2, 3, 5, 7, 'MS', 0, 1, 0, 0),
(159, 'WiSe25-26', 5, 6, 3, 4, 'MS', 0, 1, 1, 1),
(161, 'WiSe25-26', 2, 4, 3, 6, 'MS', 0, 1, 0, 1),
(163, 'WiSe25-26', 3, 5, 2, 7, 'MS', 0, 1, 1, 0),
(165, 'WiSe25-26', 4, 2, 7, 1, 'MS', 0, 1, 0, 1),
(167, 'WiSe25-26', 2, 1, 5, 3, 'MS', 0, 1, 1, 0),
(169, 'WiSe25-26', 5, 3, 2, 6, 'MS', 0, 1, 0, 1),
(171, 'WiSe25-26', 3, 6, 4, 1, 'MS', 0, 1, 1, 0),
(172, 'WiSe25-26', 4, 7, 5, 2, 'MS', 0, 1, 0, 1),
(175, 'WiSe25-26', 2, 5, 6, 3, 'MS', 0, 1, 1, 0),
(177, 'WiSe25-26', 5, 2, 3, 1, 'MS', 0, 1, 0, 1),
(179, 'WiSe25-26', 4, 3, 5, 2, 'MS', 0, 1, 1, 0);

-- Completed internships for students
INSERT INTO completed_internships (student_id, id, school_id, teacher_id, description, course_id, type) VALUES
-- GS students with their respective teachers and internship types
(115, 1, 15, 1001, 'Computer Science basics internship', 1, 'PDP_I'),
(117, 2, 15, 1002, 'Sciences laboratory work', 7, 'PDP_I'),
(119, 3, 19, 1009, 'Computer Science project', 1, 'ZSP'),
(121, 4, 17, 1006, 'Arts education internship', 6, 'PDP_I'),
(123, 5, 21, 1013, 'Law studies introduction', 5, 'PDP_I'),
(125, 6, 23, 1017, 'Sciences field work', 7, 'ZSP'),
(127, 7, 25, 1022, 'Arts and culture internship', 6, 'PDP_I'),
(129, 8, 25, 1021, 'Computer Science basics', 1, 'PDP_I'),
(131, 9, 27, 1025, 'Sciences research', 7, 'ZSP'),
(133, 10, 27, 1026, 'Engineering basics', 2, 'PDP_I'),
(135, 11, 29, 1029, 'Computer Science internship', 1, 'PDP_I'),
(137, 12, 31, 1033, 'Business case studies', 3, 'ZSP'),
(139, 13, 32, 1035, 'Computer Science project', 1, 'PDP_I'),
(141, 14, 35, 1041, 'Engineering introduction', 2, 'PDP_I'),
(143, 15, 34, 1039, 'Arts education', 6, 'ZSP'),
(144, 16, 34, 1040, 'Computer Science basics', 1, 'PDP_I'),
(145, 17, 37, 1045, 'Business studies', 3, 'PDP_I'),
(146, 18, 37, 1046, 'Sciences lab work', 7, 'ZSP'),
(147, 19, 38, 1047, 'Computer Science fundamentals', 1, 'PDP_I'),
(150, 20, 40, 1052, 'Arts portfolio development', 6, 'PDP_I'),

-- MS students with their respective teachers and internship types
(116, 21, 16, 1003, 'Business project work', 3, 'PDP_II'),
(118, 22, 16, 1004, 'Medical observation program', 4, 'SFP'),
(120, 23, 20, 1012, 'Medicine basics internship', 4, 'PDP_II'),
(122, 24, 22, 1015, 'Business consulting', 3, 'PDP_II'),
(124, 25, 22, 1016, 'Law case studies', 5, 'ZSP'),
(126, 26, 24, 1019, 'Engineering project', 2, 'PDP_II'),
(128, 27, 26, 1024, 'Law research assistant', 5, 'SFP'),
(130, 28, 26, 1023, 'Business analysis', 3, 'PDP_II'),
(132, 29, 28, 1028, 'Medical research assistant', 4, 'PDP_II'),
(134, 30, 28, 1027, 'Medicine clinical observation', 4, 'SFP'),
(136, 31, 30, 1032, 'Law studies internship', 5, 'ZSP'),
(138, 32, 33, 1038, 'Medical lab rotation', 4, 'SFP'),
(140, 33, 33, 1037, 'Law case research', 5, 'PDP_II'),
(142, 34, 36, 1044, 'Law firm internship', 5, 'SFP'),
(148, 35, 39, 1050, 'Medicine observation', 4, 'PDP_II'),
(149, 36, 41, 1053, 'Engineering development', 2, 'PDP_II'),

-- Additional GS students internships
(151, 37, 15, 1002, 'Sciences exploration', 7, 'ZSP'),
(153, 38, 17, 1005, 'Computer Science introduction', 1, 'PDP_I'),
(156, 39, 19, 1010, 'Arts and culture project', 6, 'PDP_I'),
(158, 40, 21, 1013, 'Computer Science basics', 1, 'ZSP'),
(160, 41, 21, 1014, 'Sciences laboratory', 7, 'PDP_I'),
(162, 42, 23, 1018, 'Arts education internship', 6, 'PDP_I'),
(164, 43, 25, 1021, 'Computer Science project', 1, 'ZSP'),
(166, 44, 27, 1026, 'Arts portfolio work', 6, 'PDP_I'),
(168, 45, 29, 1029, 'Sciences research project', 7, 'PDP_I'),
(170, 46, 32, 1035, 'Computer Science fundamentals', 1, 'ZSP'),
(173, 47, 35, 1042, 'Arts education program', 6, 'PDP_I'),
(174, 48, 34, 1040, 'Sciences field work', 7, 'PDP_I'),
(176, 49, 37, 1045, 'Computer Science basics', 1, 'ZSP'),
(178, 50, 38, 1048, 'Business case studies', 3, 'PDP_I'),
(180, 51, 40, 1051, 'Arts and culture internship', 6, 'PDP_I'),

-- Additional MS students internships
(152, 52, 16, 1004, 'Law research program', 5, 'PDP_II'),
(154, 53, 18, 1008, 'Medicine clinical work', 4, 'SFP'),
(155, 54, 20, 1011, 'Business project development', 3, 'PDP_II'),
(157, 55, 20, 1012, 'Engineering internship', 2, 'PDP_II'),
(159, 56, 22, 1016, 'Law case analysis', 5, 'ZSP'),
(161, 57, 24, 1020, 'Medicine observation', 4, 'SFP'),
(163, 58, 26, 1023, 'Business consulting project', 3, 'PDP_II'),
(165, 59, 28, 1028, 'Medical research assistant', 4, 'SFP'),
(167, 60, 30, 1031, 'Engineering development', 2, 'PDP_II'),
(169, 61, 30, 1032, 'Law studies internship', 5, 'SFP'),
(171, 62, 33, 1037, 'Business analysis work', 3, 'PDP_II'),
(172, 63, 36, 1043, 'Medicine clinical observation', 4, 'SFP'),
(175, 64, 37, 1045, 'Engineering project work', 2, 'PDP_II'),
(177, 65, 39, 1049, 'Law research assistant', 5, 'SFP'),
(179, 66, 41, 1054, 'Medicine observation program', 4, 'PDP_II');

