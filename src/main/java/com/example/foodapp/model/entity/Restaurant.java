package com.example.foodapp.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Restaurant {
    private int id;
    private String name;
    private String address;
    private String phone;
    @JsonProperty("logo_base64")
    private String logoBase64;
    private int taxFee;
    private int additionalFee;
    private int ownerId;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private String workingHours;

    // Empty constructor
    public Restaurant() { }

    // Constructor for creating a new restaurant (before we know id, timestamps)
    public Restaurant(String name, String address, String phone, int ownerId) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.ownerId = ownerId;
        this.enabled = false;
        this.description = "";
        this.workingHours = "";
    }

    // Full constructor (including id & timestamps)
    public Restaurant(int id, String name, String address, String phone,
                      int ownerId, boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.ownerId = ownerId;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.description = "";
        this.workingHours = "";
    }

    // Getters & setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLogoBase64() {
        return logoBase64;
    }
    public void setLogoBase64(String logoBase64) {
        this.logoBase64 = logoBase64;
    }

    public int getTaxFee() {
        return taxFee;
    }
    public void setTaxFee(int taxFee) {
        this.taxFee = taxFee;
    }

    public int getAdditionalFee() {
        return additionalFee;
    }
    public void setAdditionalFee(int additionalFee) {
        this.additionalFee = additionalFee;
    }

    public int getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    @Override
    public String toString() {
        return "Restaurant{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", enabled=" + enabled +
               ", description='" + description + '\'' +
               ", workingHours='" + workingHours + '\'' +
               '}';
    }
} 