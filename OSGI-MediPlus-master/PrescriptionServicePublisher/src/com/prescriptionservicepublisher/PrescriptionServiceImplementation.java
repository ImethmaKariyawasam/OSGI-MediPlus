package com.prescriptionservicepublisher;

import com.hospital.core.database.IDatabaseService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrescriptionServiceImplementation implements IPrescriptionService {
    private IDatabaseService dbService;
    private BundleContext context;
    private Connection connection;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
	private static final int LOW_STOCK_THRESHOLD = 10;

    public PrescriptionServiceImplementation(BundleContext context) {
        this.context = context;
        initializeConnection();
    }

    private void initializeConnection() {
        ServiceReference serviceReference = context.getServiceReference(IDatabaseService.class.getName());
        if (serviceReference != null) {
            dbService = (IDatabaseService) context.getService(serviceReference);
            try {
                connection = dbService.getConnection();
            } catch (SQLException e) {
                System.err.println("Failed to initialize database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean ensureConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return false;
        }
    }

    private <T> T executeWithRetry(DatabaseOperation<T> operation) {
        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                if (!ensureConnection()) {
                    throw new SQLException("Failed to establish database connection");
                }
                return operation.execute(connection);
            } catch (SQLNonTransientConnectionException | SQLRecoverableException e) {
                attempts++;
                System.err.println("Connection error on attempt " + attempts + ": " + e.getMessage());
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                        initializeConnection();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (SQLException e) {
                System.err.println("SQL error: " + e.getMessage());
                return operation.handleError(e);
            }
        }
        return operation.handleError(new SQLException("Max retry attempts reached"));
    }

    @FunctionalInterface
    private interface DatabaseOperation<T> {
        T execute(Connection conn) throws SQLException;
        
        @SuppressWarnings("unchecked")
        default T handleError(SQLException e) {
            e.printStackTrace();
            if (Boolean.class.equals(((Class<?>) ((ParameterizedType) getClass()
                .getGenericInterfaces()[0]).getActualTypeArguments()[0]))) {
                return (T) Boolean.FALSE;
            }
            return null;
        }
    }

    @Override
    public int createPrescription(Prescription prescription) throws Exception {
        return executeWithRetry(conn -> {
            String sql = "INSERT INTO prescription (patient_id, doctor_id, item_id, medicine, " +
                         "dosage, dosage_type, frequency, duration, food_relation, instructions, route, status) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, prescription.getPatientId());
                pstmt.setInt(2, prescription.getDoctorId());
                if (prescription.getItemId() != null) {
                    pstmt.setInt(3, prescription.getItemId());
                } else {
                    pstmt.setNull(3, Types.INTEGER);
                }
                pstmt.setString(4, prescription.getMedicine());
                pstmt.setDouble(5, prescription.getDosage());
                pstmt.setString(6, prescription.getDosageType());
                pstmt.setInt(7, prescription.getFrequency());
                pstmt.setInt(8, prescription.getDuration());
                pstmt.setString(9, prescription.getFoodRelation());
                pstmt.setString(10, prescription.getInstructions());
                pstmt.setString(11, prescription.getRoute());
                pstmt.setString(12, prescription.getStatus());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating prescription failed, no rows affected.");
                }
                
                // Since there's no auto-generated ID, we return the patient ID as a reference
                return prescription.getPatientId();
            }
        });
    }

    @Override
    public Prescription getPrescriptionById(int id) throws Exception {
        // Since there's no id column, let's assume we're looking for prescriptions by patient ID
        List<Prescription> prescriptions = executeWithRetry(conn -> {
            String sql = "SELECT * FROM prescription WHERE patient_id = ? ORDER BY prescription_date DESC LIMIT 1";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                
                List<Prescription> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapResultSetToPrescription(rs));
                }
                return results;
            }
        });
        
        // Return the most recent prescription for this patient, if any
        return prescriptions != null && !prescriptions.isEmpty() ? prescriptions.get(0) : null;
    }

    @Override
    public List<Prescription> getAllPrescriptions() throws Exception {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM prescription ORDER BY prescription_date DESC";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                List<Prescription> prescriptions = new ArrayList<>();
                
                while (rs.next()) {
                    prescriptions.add(mapResultSetToPrescription(rs));
                }
                
                return prescriptions;
            }
        });
    }

    @Override
    public boolean updatePrescription(Prescription prescription) throws Exception {
        return executeWithRetry(conn -> {
            String sql = "UPDATE prescription SET medicine = ?, dosage = ?, dosage_type = ?, " +
                         "frequency = ?, duration = ?, food_relation = ?, instructions = ?, route = ?, status = ? " +
                         "WHERE patient_id = ? AND doctor_id = ? AND prescription_date = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, prescription.getMedicine());
                pstmt.setDouble(2, prescription.getDosage());
                pstmt.setString(3, prescription.getDosageType());
                pstmt.setInt(4, prescription.getFrequency());
                pstmt.setInt(5, prescription.getDuration());
                pstmt.setString(6, prescription.getFoodRelation());
                pstmt.setString(7, prescription.getInstructions());
                pstmt.setString(8, prescription.getRoute());
                pstmt.setString(9, prescription.getStatus());
                pstmt.setInt(10, prescription.getPatientId());
                pstmt.setInt(11, prescription.getDoctorId());
                pstmt.setTimestamp(12, prescription.getPrescriptionDate());
                
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean deletePrescription(int id) throws Exception {
        return executeWithRetry(conn -> {
            String sql = "DELETE FROM prescription WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean isValidPatient(int patientId) throws Exception {
        return executeWithRetry(conn -> {
            String sql = "SELECT 1 FROM patients WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                ResultSet rs = pstmt.executeQuery();
                return rs.next();
            }
        });
    }

    @Override
    public boolean isValidDoctor(int doctorId) throws Exception {
        return executeWithRetry(conn -> {
            String sql = "SELECT 1 FROM staff WHERE id = ? AND role LIKE '%doctor%'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, doctorId);
                ResultSet rs = pstmt.executeQuery();
                return rs.next();
            }
        });
    }
    @Override
    public boolean isValidMedication(int medicationId) throws Exception {
        return executeWithRetry(conn -> {
            String sql = "SELECT item_id, quantity, expiry_date FROM stocks WHERE item_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, medicationId);
                ResultSet rs = pstmt.executeQuery();
                
                if (!rs.next()) {
                    return false; // Medication does not exist
                }
                
                // Check if expired
                Date expiryDate = rs.getDate("expiry_date");
                if (expiryDate != null && expiryDate.before(new Date(System.currentTimeMillis()))) {
                    // The medication is expired
                    System.out.println("Medication (ID: " + medicationId + ") is expired. Expiry date: " + expiryDate);
                    return false;
                }
                
                // Check quantity
                int quantity = rs.getInt("quantity");
                if (quantity <= 0) {
                    // Out of stock
                    System.out.println("Medication (ID: " + medicationId + ") is out of stock.");
                    return false;
                }
                
                // Check if low stock (this doesn't invalidate but we'll log it)
                if (quantity <= LOW_STOCK_THRESHOLD) {
                    System.out.println("WARNING: Medication (ID: " + medicationId + ") is running low. Current quantity: " + quantity);
                }
                
                return true; // Valid medication
            }
        });
    }
    
    /**
     * Updates the medication stock quantity based on prescription status.
     * 
     * @param prescription The prescription with medication details
     * @param newStatus The new status of the prescription
     * @return true if the update was successful, false otherwise
     * @throws Exception if a database error occurs
     */
    public boolean updateMedicationStock(Prescription prescription, String newStatus) throws Exception {
        // If there's no item ID, no stock update is needed
        if (prescription.getItemId() == null) {
            return true;
        }
        
        return executeWithRetry(conn -> {
            int stockDelta = 0;
            String oldStatus = prescription.getStatus();
            
            // Calculate quantity change based on status transition
            if ("Completed".equals(newStatus) && !"Completed".equals(oldStatus)) {
                // Decrease stock when prescription is completed
                stockDelta = -1 * prescription.getDuration(); // Assuming duration affects quantity
            } else if (("Rejected".equals(newStatus) || "Pending".equals(newStatus)) 
                    && "Completed".equals(oldStatus)) {
                // If previously completed prescription is now rejected/reset, restore stock
                stockDelta = prescription.getDuration();
            }
            
            // If no stock change required
            if (stockDelta == 0) {
                return true;
            }
            
            String sql = "UPDATE stocks SET quantity = quantity + ? WHERE item_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, stockDelta);
                pstmt.setInt(2, prescription.getItemId());
                
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        });
    }
    
    /**
     * Gets stock information for a specific medication.
     * 
     * @param medicationId The ID of the medication
     * @return Map containing medication stock details or null if not found
     * @throws Exception if a database error occurs
     */
    public Map<String, Object> getMedicationStockInfo(int medicationId) throws Exception {
        return executeWithRetry(conn -> {
            String sql = "SELECT item_name, quantity, unit_price, expiry_date FROM stocks WHERE item_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, medicationId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    Map<String, Object> stockInfo = new HashMap<>();
                    stockInfo.put("name", rs.getString("item_name"));
                    stockInfo.put("quantity", rs.getInt("quantity"));
                    stockInfo.put("price", rs.getBigDecimal("unit_price"));
                    stockInfo.put("expiryDate", rs.getDate("expiry_date"));
                    return stockInfo;
                }
                return null;
            }
        });
    }
    
    /**
     * Update prescription status with proper stock management
     */
    public boolean updatePrescriptionStatus(int patientId, int doctorId, Timestamp prescriptionDate, String newStatus) throws Exception {
        // First get the current prescription
        Prescription prescription = null;
        List<Prescription> prescriptions = executeWithRetry(conn -> {
            String sql = "SELECT * FROM prescription WHERE patient_id = ? AND doctor_id = ? AND prescription_date = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                pstmt.setInt(2, doctorId);
                pstmt.setTimestamp(3, prescriptionDate);
                ResultSet rs = pstmt.executeQuery();
                
                List<Prescription> results = new ArrayList<>();
                if (rs.next()) {
                    results.add(mapResultSetToPrescription(rs));
                }
                return results;
            }
        });
        
        if (prescriptions.isEmpty()) {
            return false;
        }
        
        prescription = prescriptions.get(0);
        String oldStatus = prescription.getStatus();
        
        // Update the status in the database
        boolean statusUpdated = executeWithRetry(conn -> {
            String sql = "UPDATE prescription SET status = ? " +
                         "WHERE patient_id = ? AND doctor_id = ? AND prescription_date = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, patientId);
                pstmt.setInt(3, doctorId);
                pstmt.setTimestamp(4, prescriptionDate);
                
                return pstmt.executeUpdate() > 0;
            }
        });
        
        if (statusUpdated && prescription.getItemId() != null) {
            // Now update the stock based on the status change
            prescription.setStatus(newStatus);  // Set the new status
            return updateMedicationStock(prescription, newStatus);
        }
        
        return statusUpdated;
    }

    private Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        Prescription prescription = new Prescription();
        prescription.setPatientId(rs.getInt("patient_id"));
        prescription.setDoctorId(rs.getInt("doctor_id"));
        
        int itemId = rs.getInt("item_id");
        if (!rs.wasNull()) {
            prescription.setItemId(itemId);
        }
        prescription.setId(rs.getInt("id"));
        prescription.setMedicine(rs.getString("medicine"));
        prescription.setDosage(rs.getDouble("dosage"));
        prescription.setDosageType(rs.getString("dosage_type"));
        prescription.setFrequency(rs.getInt("frequency"));
        prescription.setDuration(rs.getInt("duration"));
        prescription.setFoodRelation(rs.getString("food_relation"));
        prescription.setInstructions(rs.getString("instructions"));
        prescription.setRoute(rs.getString("route"));
        prescription.setStatus(rs.getString("status"));
        prescription.setPrescriptionDate(rs.getTimestamp("prescription_date"));
        
        return prescription;
    }
}