package com.aspd.backend.dto;

import lombok.Data;

@Data
public class AssignmentDto {
    private Long id;
    private String studentName;
    private String praktikumType;
    private String course;
    private String teacherName;
    private String schoolName;
    private String status;
}
