package com.aspd.backend.dto;

import com.aspd.backend.model.SchoolType;

public class SchoolResponse {
    private Long id;
    private String name;
    private String address;
    private String zone;
    private Boolean oepnv;
    private SchoolType type;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
