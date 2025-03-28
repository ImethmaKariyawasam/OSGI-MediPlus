package com.prescriptionservicepublisher;

import java.sql.Timestamp;

public class Prescription {
	private int id;
	private int patientId;
    private int doctorId;
    private Integer itemId; // Nullable in DB
    private String medicine;
    private double dosage;
    private String dosageType; // enum('mg','ml','tablet','capsule','puff')
    private int frequency;
    private int duration;
    private String foodRelation; // enum('Before Food','After Food','With Food')
    private String instructions;
    private String route; // enum('Oral','Injection','Topical','Inhalation')
    private String status; // enum('Pending','Completed','Rejected')
    private Timestamp prescriptionDate;
    
    public Prescription() {
        this.status = "Pending"; // Default status
    }
    
    public Prescription(int patientId, int doctorId, Integer itemId, String medicine, 
                      double dosage, String dosageType, int frequency, int duration,
                      String foodRelation, String instructions, String route, 
                      String status, Timestamp prescriptionDate) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.itemId = itemId;
        this.medicine = medicine;
        this.dosage = dosage;
        this.dosageType = dosageType;
        this.frequency = frequency;
        this.duration = duration;
        this.foodRelation = foodRelation;
        this.instructions = instructions;
        this.route = route;
        this.status = status != null ? status : "Pending";
        this.prescriptionDate = prescriptionDate;
    }
    
    // Getters and setters
    public int getPatientId() {
        return patientId;
    }
    
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    
    public int getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
    
    public Integer getItemId() {
        return itemId;
    }
    
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
    
    public String getMedicine() {
        return medicine;
    }
    
    public void setMedicine(String medicine) {
        this.medicine = medicine;
    }
    
    public double getDosage() {
        return dosage;
    }
    
    public void setDosage(double dosage) {
        this.dosage = dosage;
    }
    
    public String getDosageType() {
        return dosageType;
    }
    
    public void setDosageType(String dosageType) {
        this.dosageType = dosageType;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public String getFoodRelation() {
        return foodRelation;
    }
    
    public void setFoodRelation(String foodRelation) {
        this.foodRelation = foodRelation;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public String getRoute() {
        return route;
    }
    
    public void setRoute(String route) {
        this.route = route;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Timestamp getPrescriptionDate() {
        return prescriptionDate;
    }
    
    public void setPrescriptionDate(Timestamp prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }
    
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}