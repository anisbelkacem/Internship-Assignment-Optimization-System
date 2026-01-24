package com.aspd.backend.validation;

import com.aspd.backend.model.*;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PlannedInternshipValidationService {

    private final PlannedInternshipRepository plannedInternshipRepository;
    private final TeacherRepository teacherRepository;

    public ValidationResult validatePlannedInternshipUpdate(Long internshipId, Long teacherId, Long schoolId) {

        PlannedInternship internship = plannedInternshipRepository.findById(internshipId)
                .orElseThrow(() -> new IllegalArgumentException("PlannedInternship not found: " + internshipId));

        Teacher newTeacher;
        School newSchool = null;

        if (teacherId != null) {
            newTeacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        } else {
            newTeacher = null;
        }
        // School is derived from teacher. If a schoolId is provided, ensure it matches the teacher's school when teacherId is present.
        if (teacherId != null) {
            newSchool = newTeacher != null ? newTeacher.getSchool() : null;
            if (schoolId != null && newSchool != null && !newSchool.getId().equals(schoolId)) {
                throw new IllegalArgumentException("Provided schoolId does not match teacher's school");
            }
        }

        // Build a simulated copy with the new selections
        PlannedInternship simulated = PlannedInternship.builder()
                .id(internship.getId())
                .praktikumType(internship.getPraktikumType())
                .schoolType(internship.getSchoolType())
                .course(internship.getCourse())
                .schoolYear(internship.getSchoolYear())
                .maxCapacity(internship.getMaxCapacity())
                .currentAssignments(internship.getCurrentAssignments())
                .assignedTeacher(newTeacher)
                .build();

        List<ValidationViolation> hard = new ArrayList<>();
        List<ValidationViolation> warn = new ArrayList<>();

        // --------------------------------------------------------------------
        // (Extra safety) Capacity consistency check
        // This does NOT change solver logic. It just prevents an obviously invalid state.
        // --------------------------------------------------------------------
        if (simulated.getCurrentAssignments() > simulated.getMaxCapacity()) {
            hard.add(v("CAPACITY_EXCEEDED", ViolationSeverity.HARD,
                    "Kapazität überschritten: Zu viele Studierende sind diesem Praktikum zugewiesen.",
                    List.of("maxCapacity")));
        }

        // --------------------------------------------------------------------
        // HARD (same as teacherMustSupportPraktikumType)
        // --------------------------------------------------------------------
        TeacherPlConfig cfg = null;
        if (newTeacher != null) {
            cfg = newTeacher.getPlConfigs().stream()
                    .filter(c -> Objects.equals(c.getSchoolYear(), internship.getSchoolYear()))
                    .findFirst()
                    .orElse(null);

            if (cfg == null) {
                hard.add(v("TEACHER_NO_CONFIG_FOR_YEAR", ViolationSeverity.HARD,
                        "Der ausgewählte PL hat keine Konfiguration für dieses Schuljahr.",
                        List.of("teacherId")));
            } else {
                boolean supports = cfg.getInternshipPreferences() != null
                        && cfg.getInternshipPreferences().contains(internship.getPraktikumType());

                if (!supports) {
                    hard.add(v("TEACHER_TYPE_NOT_SUPPORTED", ViolationSeverity.HARD,
                            "Der ausgewählte PL unterstützt diesen Praktikumstyp laut Präferenzen nicht.",
                            List.of("teacherId", "praktikumType")));
                }
            }

            // ----------------------------------------------------------------
            // HARD (same as teacherMustMatchCourseForZspAndSfp)
            // Only applies to ZSP/SFP and course != null
            // ----------------------------------------------------------------
            if ((internship.getPraktikumType() == PraktikumType.ZSP || internship.getPraktikumType() == PraktikumType.SFP)
                    && internship.getCourse() != null) {

                boolean mainMatch = internship.getCourse().equals(newTeacher.getMainSubject());
                boolean specMatch = (cfg != null
                        && cfg.getSubjectSpecializations() != null
                        && cfg.getSubjectSpecializations().contains(internship.getCourse()));

                if (!mainMatch && !specMatch) {
                    hard.add(v("TEACHER_SUBJECT_MISMATCH", ViolationSeverity.HARD,
                            "Fach passt nicht: Der ausgewählte PL unterrichtet das benötigte Fach für dieses ZSP/SFP nicht.",
                            List.of("teacherId", "course")));
                }
            }
        }

        // --------------------------------------------------------------------
        // HARD (same as internshipsMustBeInAcceptableZones)
        // --------------------------------------------------------------------
        if (newTeacher != null && newTeacher.getSchool() != null) {
            String zone = newTeacher.getSchool().getZone();
            boolean oepnv = hasOepnv(newTeacher.getSchool().getOepnv());
            PraktikumType type = internship.getPraktikumType();

            boolean violates;
            if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                // OK: zone 1 OR zone 2 + OEPNV
                violates = !(("1".equals(zone)) || ("2".equals(zone) && oepnv));
            } else {
                // PDP_I or PDP_II: OK zone 2 or 3 (zone 1 not allowed)
                violates = !("2".equals(zone) || "3".equals(zone));
            }

            if (violates) {
                hard.add(v("ZONE_VIOLATION", ViolationSeverity.HARD,
                        "Zonen-Verstoß: Diese Schule ist für den ausgewählten Praktikumstyp nicht zulässig.",
                        List.of("teacherId")));
            }
        }

        // --------------------------------------------------------------------
        // HARD (same as teacherCanOnlyTake_0_1_2_or_4 and teacherWith_2_or_4_InternshipsMustHaveAllDifferentTypes)
        // Apply across all planned internships in same schoolYear (simulate the change)
        // --------------------------------------------------------------------
        if (newTeacher != null) {
            List<PlannedInternship> allYear = plannedInternshipRepository.findBySchoolYear(internship.getSchoolYear());

            List<PlannedInternship> view = new ArrayList<>(allYear.size());
            for (PlannedInternship pi : allYear) {
                if (pi.getId().equals(simulated.getId())) view.add(simulated);
                else view.add(pi);
            }

            List<PlannedInternship> teacherInternships = view.stream()
                    .filter(pi -> pi.getAssignedTeacher() != null)
                    .filter(pi -> pi.getAssignedTeacher().getTeacherId().equals(newTeacher.getTeacherId()))
                    .toList();

            int count = teacherInternships.size();

            // 0/1/2/4 only
            if (count == 3 || count > 4) {
                hard.add(v("TEACHER_WORKLOAD_INVALID", ViolationSeverity.HARD,
                        "Ungültige PL-Auslastung: Ein PL darf nur 0, 1, 2 oder 4 Praktika betreuen (nicht 3 und nicht 5+).",
                        List.of("teacherId")));
            }

            // if 2 or 4 => all types must be different
            if (count == 2 || count == 4) {
                long uniqueTypes = teacherInternships.stream()
                        .map(PlannedInternship::getPraktikumType)
                        .distinct()
                        .count();
                if (uniqueTypes != count) {
                    hard.add(v("TEACHER_TYPES_NOT_DIVERSE", ViolationSeverity.HARD,
                            "Ungültige Kombination: Wenn ein PL 2 oder 4 Praktika betreut, müssen alle Praktikumstypen unterschiedlich sein.",
                            List.of("teacherId")));
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

    private boolean hasOepnv(OepnvStatus status) {
        return status != null && status.isAvailable();
    }
}
