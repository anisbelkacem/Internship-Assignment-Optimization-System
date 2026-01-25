package com.aspd.backend.validation;

import com.aspd.backend.model.*;
import com.aspd.backend.repository.PlannedInternshipRepository;
import com.aspd.backend.repository.SchoolRepository;
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
    private final SchoolRepository schoolRepository;
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

        // If teacher and school are both provided, they should belong together.
        // NOTE: This is a business rule (not an OptaPlanner hard constraint).
        // Keep it as HARD to avoid inconsistent data entry.
        List<ValidationViolation> hard = new ArrayList<>();
        List<ValidationViolation> warn = new ArrayList<>();

        if (teacherId != null) {
            newSchool = newTeacher != null ? newTeacher.getSchool() : null;

            if (schoolId != null && newSchool != null && !newSchool.getId().equals(schoolId)) {
                hard.add(v("SCHOOL_MISMATCH", ViolationSeverity.HARD,
                        "Schule passt nicht: Die ausgewählte Schule gehört nicht zur ausgewählten Lehrkraft. " +
                                "Bitte wählen Sie die Schule der Lehrkraft oder ändern Sie die Lehrkraft.",
                        List.of("teacherId", "schoolId")));

                return ValidationResult.builder()
                        .hardValid(false)
                        .hardViolations(hard)
                        .warnings(warn)
                        .build();
            }
        }

        // Resolve explicit school selection (can be null)
        School explicitSchool = null;
        if (schoolId != null) {
            explicitSchool = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new IllegalArgumentException("School not found: " + schoolId));
        }

        // Build a simulated copy with the new selections
        PlannedInternship simulated = PlannedInternship.builder()
                .id(internship.getId())
                .praktikumType(internship.getPraktikumType())
                .schoolType(internship.getSchoolType())
                .course(internship.getCourse())
                .originalCourse(internship.getOriginalCourse())
                .schoolYear(internship.getSchoolYear())
                .maxCapacity(internship.getMaxCapacity())
                .currentAssignments(internship.getCurrentAssignments())
                .active(internship.getActive())
                .assignedTeacher(newTeacher)
                .assignedSchool(explicitSchool)
                .build();

        // --------------------------------------------------------------------
        // (Extra safety) Capacity consistency check
        // --------------------------------------------------------------------
        if (simulated.getCurrentAssignments() > simulated.getMaxCapacity()) {
            hard.add(v("CAPACITY_EXCEEDED", ViolationSeverity.HARD,
                    "Kapazität überschritten: Zu viele Studierende sind diesem Praktikum zugewiesen.",
                    List.of("maxCapacity")));
        }

        // --------------------------------------------------------------------
        // HARD (same as calculateTeacherAssignmentConstraint)
        // Active internship must have a teacher; inactive can be unassigned.
        // --------------------------------------------------------------------
        if (simulated.isActive() && simulated.getAssignedTeacher() == null) {
            hard.add(v("TEACHER_REQUIRED", ViolationSeverity.HARD,
                    "Lehrkraft fehlt: Ein aktives Praktikum muss einer Lehrkraft zugewiesen sein.",
                    List.of("teacherId")));
        }

        // --------------------------------------------------------------------
        // HARD (same as calculateTeacherAssignmentConstraint) - teacher must be active
        // + HARD (same as calculateTeacherPraktikumTypeConstraint / calculateTeacherCourseMatchConstraint)
        // --------------------------------------------------------------------
        TeacherPlConfig cfg = null;
        if (simulated.isActive() && newTeacher != null) {

            if (!newTeacher.isActive()) {
                hard.add(v("TEACHER_INACTIVE", ViolationSeverity.HARD,
                        "Lehrkraft ist inaktiv: Eine inaktive Lehrkraft darf nicht zugewiesen werden.",
                        List.of("teacherId")));
            }

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

            // ZSP/SFP require course match (main subject or specialization)
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
        // HARD (same as calculateCoursePinningConstraint + calculateZspCourseAssignmentConstraint)
        // --------------------------------------------------------------------
        if (simulated.isActive()) {
            PraktikumType type = simulated.getPraktikumType();

            if ((type == PraktikumType.PDP_I || type == PraktikumType.PDP_II) && simulated.getCourse() != null) {
                hard.add(v("PDP_COURSE_NOT_ALLOWED", ViolationSeverity.HARD,
                        "Ungültiger Kurs: PDP-Praktika dürfen keinen Kurs haben.",
                        List.of("course")));
            }

            if (type == PraktikumType.SFP && simulated.getOriginalCourse() != null) {
                if (simulated.getCourse() == null || !simulated.getCourse().equals(simulated.getOriginalCourse())) {
                    hard.add(v("SFP_COURSE_PINNED", ViolationSeverity.HARD,
                            "Ungültiger Kurs: Der SFP-Kurs ist fest vorgegeben und darf nicht geändert werden.",
                            List.of("course")));
                }
            }

            if (type == PraktikumType.ZSP && simulated.getCourse() == null) {
                hard.add(v("ZSP_COURSE_REQUIRED", ViolationSeverity.HARD,
                        "Kurs fehlt: Ein aktives ZSP-Praktikum muss einen Kurs haben.",
                        List.of("course")));
            }
        }

        // --------------------------------------------------------------------
        // HARD (same as calculateSchoolTypeMatchConstraint)
        // --------------------------------------------------------------------
        if (simulated.isActive()) {
            School effSchool = simulated.getSchool();
            if (effSchool != null) {
                if (!Boolean.TRUE.equals(effSchool.getActive())) {
                    hard.add(v("SCHOOL_INACTIVE", ViolationSeverity.HARD,
                            "Schule ist inaktiv: Eine inaktive Schule darf nicht zugewiesen werden.",
                            List.of("schoolId")));
                } else if (effSchool.getType() != simulated.getSchoolType()) {
                    hard.add(v("SCHOOL_TYPE_MISMATCH", ViolationSeverity.HARD,
                            "Schultyp passt nicht: Die ausgewählte Schule passt nicht zum benötigten Schultyp (GS/MS).",
                            List.of("schoolId")));
                }
            }
        }

        // --------------------------------------------------------------------
        // HARD (same as calculateZoneConstraint)
        // --------------------------------------------------------------------
        if (simulated.isActive()) {
            School effSchool = simulated.getSchool();
            if (effSchool != null) {
                String zone = effSchool.getZone();
                OepnvStatus oepnv = effSchool.getOepnv();
                PraktikumType type = simulated.getPraktikumType();

                boolean violates;
                if (type == PraktikumType.ZSP || type == PraktikumType.SFP) {
                    // ZSP & SFP: Zone 1 or 2 (ÖPNV doesn't matter)
                    violates = !("1".equals(zone) || "2".equals(zone));
                } else {
                    // PDP: Zone 2 or 3, and ÖPNV FOUR_B is forbidden
                    violates = !("2".equals(zone) || "3".equals(zone)) || oepnv == OepnvStatus.FOUR_B;
                }

                if (violates) {
                    hard.add(v("ZONE_VIOLATION", ViolationSeverity.HARD,
                            "Zonen-Verstoß: Diese Schule ist für den ausgewählten Praktikumstyp nicht zulässig.",
                            List.of("schoolId")));
                }
            }
        }

        // --------------------------------------------------------------------
        // HARD (same as calculateTeacherWorkloadConstraint/calculateTeacherDiversityConstraint/calculateTeacherMaxPraktikaConstraint)
        // Count only ACTIVE internships (solver uses isActive()).
        // --------------------------------------------------------------------
        if (newTeacher != null) {
            List<PlannedInternship> allYear = plannedInternshipRepository.findBySchoolYear(internship.getSchoolYear());

            List<PlannedInternship> view = new ArrayList<>(allYear.size());
            for (PlannedInternship pi : allYear) {
                if (pi.getId().equals(simulated.getId())) view.add(simulated);
                else view.add(pi);
            }

            List<PlannedInternship> teacherInternships = view.stream()
                    .filter(PlannedInternship::isActive)
                    .filter(pi -> pi.getAssignedTeacher() != null)
                    .filter(pi -> pi.getAssignedTeacher().getTeacherId().equals(newTeacher.getTeacherId()))
                    .toList();

            int count = teacherInternships.size();

            // Max praktika per year (ACTIVE only)
            if (cfg != null && cfg.getMaxPraktikaPerYear() != null) {
                int maxAllowed = cfg.getMaxPraktikaPerYear();
                if (count > maxAllowed) {
                    hard.add(v("TEACHER_MAX_PRAKTIKA_EXCEEDED", ViolationSeverity.HARD,
                            "Ungültige PL-Auslastung: Der ausgewählte PL würde " + count +
                                    " aktive Praktika betreuen (maximal erlaubt: " + maxAllowed + ").",
                            List.of("teacherId")));
                }
            }

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
