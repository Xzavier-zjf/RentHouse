package com.renthouse.houseservice.model;

import java.math.BigDecimal;

public class House {
    private Integer id;
    private String title;
    private BigDecimal price;
    private String location;
    private Integer userId;
    private String status;
    private String reason;

    // Constructors
    public House() {}

    public House(String title, BigDecimal price, String location, Integer userId) {
        this.title = title;
        this.price = price;
        this.location = location;
        this.userId = userId;
        this.status = "PENDING";
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}