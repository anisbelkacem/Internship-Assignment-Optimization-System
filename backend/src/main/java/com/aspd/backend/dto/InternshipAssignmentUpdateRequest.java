package com.aspd.backend.dto;

import com.aspd.backend.model.AssignmentStatus;
import lombok.Data;

@Data
public class InternshipAssignmentUpdateRequest {
    private Long teacherId;
    private Long schoolId;
    private AssignmentStatus status;
}
