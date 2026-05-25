package com.renthouse.houseservice.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RentalApplication {
    private Integer id;
    private Integer userId;
    private Integer houseId;
    private Integer landlordId;
    private String applicantName;
    private String applicantPhone;
    private LocalDate moveInDate;
    private Integer leaseMonths;
    private String message;
    private String status;
    private String reply;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private House house;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getHouseId() { return houseId; }
    public void setHouseId(Integer houseId) { this.houseId = houseId; }
    public Integer getLandlordId() { return landlordId; }
    public void setLandlordId(Integer landlordId) { this.landlordId = landlordId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getApplicantPhone() { return applicantPhone; }
    public void setApplicantPhone(String applicantPhone) { this.applicantPhone = applicantPhone; }
    public LocalDate getMoveInDate() { return moveInDate; }
    public void setMoveInDate(LocalDate moveInDate) { this.moveInDate = moveInDate; }
    public Integer getLeaseMonths() { return leaseMonths; }
    public void setLeaseMonths(Integer leaseMonths) { this.leaseMonths = leaseMonths; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public House getHouse() { return house; }
    public void setHouse(House house) { this.house = house; }
}
