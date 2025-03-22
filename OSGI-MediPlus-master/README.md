# Hospital Management System (OSGi-Based)

An OSGi-based modular hospital management system that provides services for patient management and vital records management.

## 🚀 Features

- Patient management (CRUD operations)
- Vital signs recording and monitoring
- Modular architecture using OSGi
- Command-line interface for easy interaction

## 📋 Prerequisites

- Java 21
- XAMPP (Apache & MySQL)
- Eclipse IDE with Plugin Development Environment
- MySQL Database

## 💾 Database Setup

### Step 1: Create the Database

```sql
CREATE DATABASE hospital;
USE hospital;
```

### Step 2: Create the Patients Table

```sql
CREATE TABLE patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender ENUM('Male', 'Female', 'Other') NOT NULL,
    contact VARCHAR(15) NOT NULL UNIQUE,
    medical_history TEXT
);
```

### Step 3: Create the Vitals Table

```sql
CREATE TABLE vitals (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    temperature DECIMAL(5,2) NOT NULL,
    heart_rate INT NOT NULL,
    blood_pressure_systolic INT NOT NULL,
    blood_pressure_diastolic INT NOT NULL,
    respiratory_rate INT NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);
```

## 📥 Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-repo/hospital-management.git
   ```
   Or download the ZIP and extract the files.

2. **Set Up Eclipse Workspace**
   - Move all service folders and META-INF to your workspace directory
   - Open Eclipse in Plugin Development Perspective
   - Import the workspace folder

3. **Install MySQL Connector**
   - Locate `mysql-connector-j-8.0.31.jar` in HospitalCoreDatabase/lib
   - Copy to Eclipse workspace
   - Install in OSGi:
     ```bash
     install file:mysql-connector-j-8.0.31.jar
     ```

4. **Start OSGi Services**
   Start the following services in order:
   - HospitalCoreDatabase
   - PatientServicePublisher
   - VitalServicePublisher
   - PatientServiceConsumer

## 🎮 Usage

The system provides a Command-Line Interface through the PatientServiceConsumer bundle. You can:

- Manage patients (Add/Update/Delete/Search)
- Record vital signs
- View patient history
- Monitor vital statistics

## 🔍 Service Verification

To verify installed services:
```bash
ss
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request
