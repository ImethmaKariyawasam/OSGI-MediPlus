package com.vitalServicePublisher;

public class Vitals {
    private int patientId;
    private double temperature;
    private int heartRate;
    private int bloodPressureSystolic;
    private int bloodPressureDiastolic;
    private int respiratoryRate;
    
    public Vitals() {
		super();
	}
    
    public Vitals(double temperature, int heartRate, int bloodPressureSystolic, 
                 int bloodPressureDiastolic, int respiratoryRate) {
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.bloodPressureSystolic = bloodPressureSystolic;
        this.bloodPressureDiastolic = bloodPressureDiastolic;
        this.respiratoryRate = respiratoryRate;
    }

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public int getHeartRate() {
		return heartRate;
	}

	public void setHeartRate(int heartRate) {
		this.heartRate = heartRate;
	}

	public int getBloodPressureSystolic() {
		return bloodPressureSystolic;
	}

	public void setBloodPressureSystolic(int bloodPressureSystolic) {
		this.bloodPressureSystolic = bloodPressureSystolic;
	}

	public int getBloodPressureDiastolic() {
		return bloodPressureDiastolic;
	}

	public void setBloodPressureDiastolic(int bloodPressureDiastolic) {
		this.bloodPressureDiastolic = bloodPressureDiastolic;
	}

	public int getRespiratoryRate() {
		return respiratoryRate;
	}

	public void setRespiratoryRate(int respiratoryRate) {
		this.respiratoryRate = respiratoryRate;
	}
    
}