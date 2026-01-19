package com.aspd.backend.dto;

import com.aspd.backend.model.SchoolType;
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
public class SchoolResponse {
    private Long id;
    private String name;
    private String address;
    private String zone;
    private Boolean oepnv;
    private SchoolType type;
    private Boolean active;
    private Double longitude;
    private Double latitude;
}
