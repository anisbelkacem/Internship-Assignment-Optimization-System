package com.aspd.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    private int matriculationNbr;
    private String firstName;
    private String lastName;
    private String email;

    @Enumerated(EnumType.STRING)
    private SchoolType schoolType;

    @Enumerated(EnumType.STRING)
    private Course mainCourse;

    @Enumerated(EnumType.STRING)
    private Course prefCourse1;

    @Enumerated(EnumType.STRING)
    private Course prefCourse2;

    @Enumerated(EnumType.STRING)
    private Course prefCourse3;

    private boolean registred;
    private boolean oriented;

    @Embedded
    private Address address;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "street", column = @Column(name = "semester_street")),
            @AttributeOverride(name = "city", column = @Column(name = "semester_city")),
            @AttributeOverride(name = "houseNbr", column = @Column(name = "semester_house_nbr")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "semester_postal_code")),
            @AttributeOverride(name = "country", column = @Column(name = "semester_country"))
    })
    private Address addressSemester;

    private String phone;
    private LocalDate birthDate;
    private String description;

}
