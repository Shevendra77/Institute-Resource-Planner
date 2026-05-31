package com.example.irp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resources")
public class Resource
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int resource_id;

    private String resource_name;
    private String type;
    private int quantity;
    private String location;
    private String status;

    // This is not make a column in database
    @Transient
    private int availableQuantity;


    public int getResource_id() { return resource_id; }
    public void setResource_id(int resource_id) { this.resource_id = resource_id; }
    public String getResource_name() { return resource_name; }
    public void setResource_name(String resource_name) { this.resource_name = resource_name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
}