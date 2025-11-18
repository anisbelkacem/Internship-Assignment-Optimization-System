package com.aspd.backend.dto;

import com.aspd.backend.model.SchoolType;
import jakarta.validation.constraints.*;

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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public Boolean getOepnv() { return oepnv; }
    public void setOepnv(Boolean oepnv) { this.oepnv = oepnv; }
    public SchoolType getType() { return type; }
    public void setType(SchoolType type) { this.type = type; }
}
