package com.aspd.backend.service;


import com.aspd.backend.dto.TeacherDto;
import com.aspd.backend.dto.TeacherPlConfigDto;
import com.aspd.backend.dto.TeacherRequest;

import com.aspd.backend.model.Teacher;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.TeacherPlConfigRepository;
import com.aspd.backend.repository.TeacherRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



import com.aspd.backend.common.exception.InvalidDataException;
import com.aspd.backend.dto.TeacherImportResult;
import com.aspd.backend.model.Course;
import com.aspd.backend.model.School;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



@Service
@Transactional
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherPlConfigRepository plConfigRepository;
    private final SchoolRepository schoolRepository;
    public TeacherService(TeacherRepository teacherRepository,
                          TeacherPlConfigRepository plConfigRepository,
                          SchoolRepository schoolRepository) {
        this.teacherRepository = teacherRepository;
        this.plConfigRepository = plConfigRepository;
        this.schoolRepository = schoolRepository;
    }


    // List all PLs (teachers)
    public List<TeacherDto> getAll() {
        return teacherRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    // Get one by id
    public TeacherDto getById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher (PL) not found with id " + id));
        return toDto(teacher);
    }

    // Create
    public TeacherDto create(TeacherRequest request) {
        validateRequest(request, null);

        Teacher teacher = new Teacher();
        applyRequest(teacher, request);

        teacher = teacherRepository.save(teacher);
        return toDto(teacher);
    }

    // Update
    public TeacherDto update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Teacher (PL) not found with id " + id));

        validateRequest(request, id);
        applyRequest(teacher, request);

        teacher = teacherRepository.save(teacher);
        return toDto(teacher);
    }

    // Delete
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new IllegalArgumentException("Teacher (PL) not found with id " + id);
        }
        teacherRepository.deleteById(id);
    }

    // ----- Helpers -----

    private void validateRequest(TeacherRequest request, Long currentId) {
        if (request.firstName() == null || request.firstName().isBlank()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.lastName() == null || request.lastName().isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.mainSubject() == null ) {
            throw new IllegalArgumentException("Main subject is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        // Uniqueness by email
        teacherRepository.findByEmail(request.email()).ifPresent(existing -> {
            if (currentId == null || !existing.getTeacherId().equals(currentId)) {
                throw new IllegalArgumentException("A teacher/PL with this email already exists");
            }
        });
    }

    private void applyRequest(Teacher teacher, TeacherRequest request) {
        teacher.setFirstName(request.firstName().trim());
        teacher.setLastName(request.lastName().trim());
        teacher.setMainSubject(request.mainSubject());
        teacher.setSchool(request.schoolId());
        teacher.setEmail(request.email().trim());

    }

    private TeacherDto toDto(Teacher teacher) {
        var configDtos = teacher.getPlConfigs().stream()
                .map(cfg -> new TeacherPlConfigDto(
                        cfg.getId(),
                        cfg.getSchoolYear(),
                        cfg.getMaxPraktikaPerYear(),
                        cfg.getTotalHoursCredit(),
                        cfg.getAvailabilityStatus(),
                        cfg.getSubjectSpecializations(),
                        cfg.getInternshipPreferences()
                ))
                .toList();

        return new TeacherDto(
                teacher.getTeacherId(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getMainSubject(),
                teacher.getSchool(),
                teacher.getEmail(),
                configDtos
        );
    }

    // =====================================================================
    // Excel IMPORT / EXPORT for Teachers (PLs)
    // =====================================================================

    @Transactional
    public TeacherImportResult importFromExcel(InputStream inputStream) {
        TeacherImportResult result = new TeacherImportResult();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = getFirstSheet(workbook);
            if (isSheetEmpty(sheet)) {
                result.setTotalRows(0);
                return result;
            }

            Map<String, Integer> headerIndex = buildHeaderIndex(sheet.getRow(0));
            validateRequiredHeaders(headerIndex, result);
            if (result.getErrorCount() > 0) {
                return result;
            }

            List<Teacher> toSave = processDataRows(sheet, headerIndex, result);
            saveImportedTeachers(toSave, result);
            return result;

        } catch (InvalidDataException e) {
            result.addError(e.getMessage());
            result.setErrorCount(result.getErrors().size());
            return result;
        } catch (IOException e) {
            result.addError("I/O error while reading Excel file: " + e.getMessage());
            result.setErrorCount(result.getErrors().size());
            return result;
        }
    }

    public byte[] exportToExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Teachers");

            int rowIdx = 0;

            // Header row
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("firstName");
            header.createCell(1).setCellValue("lastName");
            header.createCell(2).setCellValue("email");
            header.createCell(3).setCellValue("mainSubject");
            header.createCell(4).setCellValue("schoolId");

            for (Teacher t : teacherRepository.findAll()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getFirstName());
                row.createCell(1).setCellValue(t.getLastName());
                row.createCell(2).setCellValue(t.getEmail());
                row.createCell(3).setCellValue(
                        t.getMainSubject() != null ? t.getMainSubject().name() : ""
                );
                row.createCell(4).setCellValue(
                        t.getSchool() != null && t.getSchool().getId() != null
                                ? t.getSchool().getId()
                                : 0
                );
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export teachers to Excel", e);
        }
    }

    // -----------------------------------------------------------------
    // Helpers – same pattern as SchoolService
    // -----------------------------------------------------------------

    private Sheet getFirstSheet(Workbook workbook) {
        Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            throw new InvalidDataException("No sheets found in the Excel file");
        }
        return sheet;
    }

    private boolean isSheetEmpty(Sheet sheet) {
        return sheet.getPhysicalNumberOfRows() <= 1;
    }

    private List<Teacher> processDataRows(Sheet sheet,
                                          Map<String, Integer> headerIndex,
                                          TeacherImportResult result) {
        List<Teacher> toSave = new ArrayList<>();
        int total = 0;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            total++;
            try {
                Teacher teacher = parseRow(row, headerIndex);
                toSave.add(teacher);
            } catch (InvalidDataException e) {
                result.addError("Row " + (i + 1) + ": " + e.getMessage());
            }
        }

        result.setTotalRows(total);
        return toSave;
    }

    private void saveImportedTeachers(List<Teacher> toSave, TeacherImportResult result) {
        if (!toSave.isEmpty()) {
            teacherRepository.saveAll(toSave);
            result.setImportedCount(toSave.size());
        }
        result.setErrorCount(result.getErrors().size());
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        Map<String, Integer> idx = new HashMap<>();
        if (headerRow == null) {
            return idx;
        }

        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell == null) {
                continue;
            }
            String key = normalize(headerString(cell));
            if (!key.isEmpty()) {
                idx.put(key, c);
            }
        }
        return idx;
    }

    private void validateRequiredHeaders(Map<String, Integer> headerIndex,
                                         TeacherImportResult result) {
        // Normalized header keys: "firstname", "lastname", "email", "mainsubject"
        List<String> required = List.of("firstname", "lastname", "email", "mainsubject");
        for (String r : required) {
            if (!headerIndex.containsKey(r)) {
                result.addError("Missing required column: " + r);
            }
        }
        result.setErrorCount(result.getErrors().size());
    }

    private Teacher parseRow(Row row, Map<String, Integer> headerIndex) {
        String firstName = readString(row, headerIndex.get("firstname"));
        String lastName = readString(row, headerIndex.get("lastname"));
        String email = readString(row, headerIndex.get("email"));
        String mainSubjectRaw = readString(row, headerIndex.get("mainsubject"));
        String schoolIdRaw = readString(row, headerIndex.get("schoolid"));

        if (isBlank(firstName) || isBlank(lastName) || isBlank(email)) {
            throw new InvalidDataException("Required fields missing or invalid (firstName, lastName, email)");
        }
        if (isBlank(mainSubjectRaw)) {
            throw new InvalidDataException("mainSubject is required");
        }

        Course mainSubject;
        try {
            // Expect values like COMPUTER_SCIENCE, BUSINESS, etc.
            mainSubject = Course.valueOf(mainSubjectRaw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidDataException("mainSubject", mainSubjectRaw,
                    "must be one of: " + List.of(Course.values()));
        }

        Teacher teacher = new Teacher();
        teacher.setFirstName(firstName.trim());
        teacher.setLastName(lastName.trim());
        teacher.setEmail(email.trim());
        teacher.setMainSubject(mainSubject);

        // Optional: link to a School by id if column schoolId exists and is not empty
        if (!isBlank(schoolIdRaw)) {
            try {
                Long schoolId = Long.valueOf(schoolIdRaw.trim());
                School school = schoolRepository.findById(schoolId)
                        .orElseThrow(() -> new InvalidDataException("schoolId", schoolId,
                                "School not found"));
                teacher.setSchool(school);
            } catch (NumberFormatException ex) {
                throw new InvalidDataException("schoolId", schoolIdRaw, "must be a valid number");
            }
        }

        return teacher;
    }

    // ---- small helper methods (copied style from SchoolService) ----

    private String headerString(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        String t = s.trim().toLowerCase(Locale.ROOT);
        t = t.replace("ä", "ae").replace("ö", "oe")
                .replace("ü", "ue").replace("ß", "ss");
        t = t.replaceAll("[^a-z0-9]", "");
        return t;
    }

    private String readString(Row row, Integer colIdx) {
        if (colIdx == null) {
            return null;
        }
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            String v = cell.getStringCellValue();
            return isBlank(v) ? null : v.trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            double d = cell.getNumericCellValue();
            if (Double.isNaN(d)) {
                return null;
            }
            long l = (long) d;
            if (Math.abs(d - l) < 1e-9) {
                return String.valueOf(l);
            }
            return String.valueOf(d);
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


}
