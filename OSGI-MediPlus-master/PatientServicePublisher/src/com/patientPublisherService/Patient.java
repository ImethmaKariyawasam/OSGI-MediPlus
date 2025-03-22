package com.patientPublisherService;

import java.io.Serializable;

public class Patient implements Serializable{
    private int id;
    private String name;
    private int age;
    private String gender;
    private String contact;
    private String medicalHistory;

    // Constructors
    public Patient() {}

    public Patient(int id, String name, int age, String gender, String contact, String medicalHistory) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
        this.medicalHistory = medicalHistory;
    }

    public Patient(String name, int age, String gender, String contact, String medicalHistory) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
        this.medicalHistory = medicalHistory;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    @Override
    public String toString() {
        return String.format(
            "Patient ID: %d\nName: %s\nAge: %d\nGender: %s\nContact: %s\nMedical History: %s",
            id, name, age, gender, contact, medicalHistory
        );
    }
}
