package com.aspd.backend.validation;

import com.aspd.backend.dto.StudentConfigDto;
import com.aspd.backend.model.StudentConfig;
import com.aspd.backend.repository.StudentConfigRepository;
import com.aspd.backend.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentConfigValidationService {

    private final StudentRepository studentRepository;
    private final StudentConfigRepository studentConfigRepository;

    public ValidationResult validate(StudentConfigDto dto) {
        List<ValidationViolation> hard = new ArrayList<>();
        List<ValidationViolation> warn = new ArrayList<>();

        // -------------------------
        // Student exists
        // -------------------------
        if (dto.getStudentId() <= 0) {
            hard.add(v("STUDENT_ID_REQUIRED", ViolationSeverity.HARD,
                    "Student-ID fehlt oder ist ungültig.", List.of("studentId")));
        } else if (studentRepository.findById(dto.getStudentId()).isEmpty()) {
            hard.add(v("STUDENT_NOT_FOUND", ViolationSeverity.HARD,
                    "Student wurde nicht gefunden.", List.of("studentId")));
        }

        // -------------------------
        // Year required
        // -------------------------
        if (dto.getYear() == null || dto.getYear().trim().isEmpty()) {
            hard.add(v("YEAR_REQUIRED", ViolationSeverity.HARD,
                    "Jahr darf nicht leer sein.", List.of("year")));
        }

        // -------------------------
        // School type required (Phase 2 hard constraint depends on it)
        // -------------------------
        if (dto.getSchoolType() == null) {
            hard.add(v("SCHOOL_TYPE_REQUIRED", ViolationSeverity.HARD,
                    "Schultyp fehlt (GS/MS).", List.of("schoolType")));
        }

        // -------------------------
        // Internship type selection (at least one)
        // -------------------------
        boolean anyType = dto.isPdpI() || dto.isPdpII() || dto.isZsp() || dto.isSfp();
        if (!anyType) {
            hard.add(v("NO_INTERNSHIP_SELECTED", ViolationSeverity.HARD,
                    "Mindestens ein Praktikumstyp muss ausgewählt werden.", List.of("pdpI", "pdpII", "zsp", "sfp")));
        }

        // -------------------------
        // Courses required (you already enforce this in frontend alerts)
        // -------------------------
        if (dto.getMainCourse() == null) {
            hard.add(v("MAIN_COURSE_REQUIRED", ViolationSeverity.HARD,
                    "Hauptfach fehlt.", List.of("mainCourse")));
        }
        if (dto.getPrefCourse1() == null) {
            hard.add(v("PREF1_REQUIRED", ViolationSeverity.HARD,
                    "Wunschfach 1 fehlt.", List.of("prefCourse1")));
        }
        if (dto.getPrefCourse2() == null) {
            hard.add(v("PREF2_REQUIRED", ViolationSeverity.HARD,
                    "Wunschfach 2 fehlt.", List.of("prefCourse2")));
        }
        if (dto.getPrefCourse3() == null) {
            hard.add(v("PREF3_REQUIRED", ViolationSeverity.HARD,
                    "Wunschfach 3 fehlt.", List.of("prefCourse3")));
        }

        // -------------------------
        // No duplicate courses
        // -------------------------
        List<Long> ids = new ArrayList<>();
        if (dto.getMainCourse() != null && dto.getMainCourse().getId() != null) ids.add(dto.getMainCourse().getId());
        if (dto.getPrefCourse1() != null && dto.getPrefCourse1().getId() != null) ids.add(dto.getPrefCourse1().getId());
        if (dto.getPrefCourse2() != null && dto.getPrefCourse2().getId() != null) ids.add(dto.getPrefCourse2().getId());
        if (dto.getPrefCourse3() != null && dto.getPrefCourse3().getId() != null) ids.add(dto.getPrefCourse3().getId());

        if (!ids.isEmpty()) {
            Set<Long> unique = new HashSet<>(ids);
            if (unique.size() != ids.size()) {
                hard.add(v("DUPLICATE_COURSES", ViolationSeverity.HARD,
                        "Ein Fach darf nicht mehrfach ausgewählt werden.", List.of("mainCourse", "prefCourse1", "prefCourse2", "prefCourse3")));
            }
        }

        // -------------------------
        // Uniqueness: only one config per (studentId, year)
        // (allow same record on update)
        // -------------------------
        if (dto.getStudentId() > 0 && dto.getYear() != null && !dto.getYear().trim().isEmpty()) {
            Optional<StudentConfig> existing = studentConfigRepository
                    .findByStudent_MatriculationNbrAndYear(dto.getStudentId(), dto.getYear());

            if (existing.isPresent()) {
                Long existingId = existing.get().getId();
                Long incomingId = dto.getId();

                boolean sameRecord = incomingId != null && Objects.equals(existingId, incomingId);
                if (!sameRecord) {
                    hard.add(v("DUPLICATE_STUDENT_YEAR_CONFIG", ViolationSeverity.HARD,
                            "Für diesen Studenten existiert bereits eine Konfiguration in diesem Jahr.", List.of("studentId", "year")));
                }
            }
        }

        return ValidationResult.builder()
                .hardValid(hard.isEmpty())
                .hardViolations(hard)
                .warnings(warn)
                .build();
    }

    private ValidationViolation v(String code, ViolationSeverity severity, String message, List<String> fields) {
        return ValidationViolation.builder()
                .code(code)
                .severity(severity)
                .message(message)
                .fields(fields)
                .build();
    }
}
