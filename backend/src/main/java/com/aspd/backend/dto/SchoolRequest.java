package com.aspd.backend.dto;

import com.aspd.backend.model.SchoolType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotBlank
    private String zone;

    @NotNull
    private Boolean oepnv;

    @NotNull
    private SchoolType type; // GS or MS
}
