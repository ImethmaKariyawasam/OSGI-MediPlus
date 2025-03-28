package com.diagnosisServicePublisher;

import java.util.Date;

public class Diagnosis {
    private int id;
    private int patientId;
    private String doctorName;  // Only storing the doctor's name, not the ID
    private String diagnosis;
    private String type; // "Provincial" or "Principal"
    private String icd10Code;
    private String symptoms;
    private String severityLevel;
    private Date diagnosisDate;
    
	public Diagnosis() {
		super();
	}
	
	
	public Diagnosis(int patientId, String doctorName, String diagnosis, String type, String icd10Code,
			String symptoms, String severityLevel, Date diagnosisDate) {
		super();
		this.patientId = patientId;
		this.doctorName = doctorName;
		this.diagnosis = diagnosis;
		this.type = type;
		this.icd10Code = icd10Code;
		this.symptoms = symptoms;
		this.severityLevel = severityLevel;
		this.diagnosisDate = diagnosisDate;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPatientId() {
		return patientId;
	}
	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
	public String getDoctorName() {
		return doctorName;
	}
	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}
	public String getDiagnosis() {
		return diagnosis;
	}
	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getIcd10Code() {
		return icd10Code;
	}
	public void setIcd10Code(String icd10Code) {
		this.icd10Code = icd10Code;
	}
	public String getSymptoms() {
		return symptoms;
	}
	public void setSymptoms(String symptoms) {
		this.symptoms = symptoms;
	}
	public String getSeverityLevel() {
		return severityLevel;
	}
	public void setSeverityLevel(String severityLevel) {
		this.severityLevel = severityLevel;
	}
	public Date getDiagnosisDate() {
		return diagnosisDate;
	}
	public void setDiagnosisDate(Date diagnosisDate) {
		this.diagnosisDate = diagnosisDate;
	} 
    
    
    
}
