package com.aspd.backend.dto;

import com.aspd.backend.model.Address;
import com.aspd.backend.model.SchoolType;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {
    private int matriculationNbr;
    private String firstName;
    private String lastName;
    private String email;

    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;

    private Long mainCourseId;
    private Long prefCourse1Id;
    private Long prefCourse2Id;
    private Long prefCourse3Id;

    private boolean registred;
    private boolean oriented;

    private Address address;
    private Address addressSemester;

    private String phone;
    private LocalDate birthDate;
    private String description;
}
