package com.aspd.backend.mapper;

import com.aspd.backend.dto.AssignmentDto;
import com.aspd.backend.model.InternshipAssignment;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting InternshipAssignment entities to DTOs.
 */
@Component
public class AssignmentMapper {

    public AssignmentDto toDto(InternshipAssignment assignment) {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setStudentName(assignment.getStudentConfig().getStudent().getFirstName() + " " +
                assignment.getStudentConfig().getStudent().getLastName());
        dto.setPraktikumType(assignment.getPraktikumType().toString());
        
        if (assignment.getCourse() != null) {
            dto.setCourse(assignment.getCourse().getName());
        }
        
        if (assignment.getTeacher() != null) {
            dto.setTeacherName(assignment.getTeacher().getFirstName() + " " +
                    assignment.getTeacher().getLastName());
        }
        
        if (assignment.getSchool() != null) {
            dto.setSchoolName(assignment.getSchool().getName());
        }
        
        dto.setStatus(assignment.getStatus().toString());
        
        return dto;
    }
}
