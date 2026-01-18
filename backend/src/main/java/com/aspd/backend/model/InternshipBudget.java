package com.aspd.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Problem fact representing the total budget (maximum number) of active internships allowed.
 * This is used by the constraint provider to enforce the activation limit.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InternshipBudget {
    private Integer maxActiveInternships;
}
