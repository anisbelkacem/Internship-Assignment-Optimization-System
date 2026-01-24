package com.aspd.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "schools")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false)
    @NotBlank
    private String address;

    @Column(nullable = false)
    @NotBlank
    private String zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "oepnv", nullable = false)
    @NotNull
    private OepnvStatus oepnv = OepnvStatus.NONE; // ÖPNV accessibility category

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private SchoolType type; // GS or MS

    @Column(nullable = false)
    @NotNull
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    public OepnvStatus getOepnv() { return oepnv; }
    public void setOepnv(OepnvStatus oepnv) { this.oepnv = oepnv; }
    public SchoolType getType() { return type; }
    public void setType(SchoolType type) { this.type = type; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
