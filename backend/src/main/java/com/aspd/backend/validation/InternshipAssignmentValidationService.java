package com.aspd.backend.validation;

import com.aspd.backend.model.*;
import com.aspd.backend.repository.InternshipAssignmentRepository;
import com.aspd.backend.repository.SchoolRepository;
import com.aspd.backend.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InternshipAssignmentValidationService {

    private final InternshipAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;

    public ValidationResult validateUpdate(Long assignmentId, Long teacherId, Long schoolId) {

        InternshipAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("InternshipAssignment not found: " + assignmentId));

        List<ValidationViolation> hard = new ArrayList<>();
        List<ValidationViolation> warn = new ArrayList<>();

        PraktikumType type = assignment.getPraktikumType();
        Course course = assignment.getCourse();
        String schoolYear = assignment.getSchoolYear();

        // ---- teacher checks (same idea as Phase 1) ----
        if (teacherId != null) {
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));

            // Mirror InternshipEasyScoreCalculator: do not allow assigning inactive teachers
            if (!teacher.isActive()) {
                hard.add(v("TEACHER_INACTIVE", ViolationSeverity.HARD,
                        "Lehrkraft ist inaktiv: Eine inaktive Lehrkraft darf nicht zugewiesen werden.",
                        List.of("teacherId")));
            }

            TeacherPlConfig cfg = teacher.getPlConfigs().stream()
                    .filter(c -> schoolYear != null && schoolYear.equals(c.getSchoolYear()))
                    .findFirst()
                    .orElse(null);

            if (cfg == null) {
                hard.add(v("TEACHER_NO_CONFIG_FOR_YEAR", ViolationSeverity.HARD,
                        "Der ausgewählte PL hat keine Konfiguration für dieses Schuljahr.",
                        List.of("teacherId")));
            } else {
                if (cfg.getInternshipPreferences() == null || !cfg.getInternshipPreferences().contains(type)) {
                    hard.add(v("TEACHER_TYPE_NOT_SUPPORTED", ViolationSeverity.HARD,
                            "Der ausgewählte PL unterstützt diesen Praktikumstyp laut Präferenzen nicht.",
                            List.of("teacherId")));
                }
            }

            // Course rule for ZSP/SFP (if course exists)
            if ((type == PraktikumType.ZSP || type == PraktikumType.SFP) && course != null) {
                boolean mainMatch = course.equals(teacher.getMainSubject());
                boolean specMatch = cfg != null && cfg.getSubjectSpecializations() != null
                        && cfg.getSubjectSpecializations().contains(course);

                if (!mainMatch && !specMatch) {
                    hard.add(v("TEACHER_SUBJECT_MISMATCH", ViolationSeverity.HARD,
                            "Fach passt nicht: Der ausgewählte PL unterrichtet das benötigte Fach für dieses ZSP/SFP nicht.",
                            List.of("teacherId")));
                }
            }
        }

        // ---- school zone checks ----
        if (schoolId != null) {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new IllegalArgumentException("School not found: " + schoolId));

            String zone = school.getZone();
            OepnvStatus oepnv = school.getOepnv();

            boolean violates;
            if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                // ZSP & SFP: Zone 1 or Zone 2 acceptable (ÖPNV doesn't matter)
                violates = !("1".equals(zone) || "2".equals(zone));
            } else { // PDP_I or PDP_II
                // PDP: Zone 2 or Zone 3 acceptable, but ÖPNV FOUR_B is NOT allowed
                violates = !("2".equals(zone) || "3".equals(zone)) || oepnv == OepnvStatus.FOUR_B;
            }

            if (violates) {
                hard.add(v("ZONE_VIOLATION", ViolationSeverity.HARD,
                        "Zonen-Verstoß: Diese Schule ist für den ausgewählten Praktikumstyp nicht zulässig.",
                        List.of("schoolId")));
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
