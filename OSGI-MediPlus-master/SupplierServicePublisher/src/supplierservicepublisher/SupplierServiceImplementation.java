package supplierservicepublisher;


import java.util.List;
import com.hospital.core.database.IDatabaseService;
import java.sql.*;
import java.util.ArrayList;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class SupplierServiceImplementation implements SupplierService {
    
    private IDatabaseService dbService;
    private BundleContext context;
    private Connection connection;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public SupplierServiceImplementation(BundleContext context) {
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
                initializeConnection();  // Reinitialize the connection if it's closed or null
            }
            if (connection != null && !connection.isClosed()) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeQuery("SELECT 1");
                    return true;  // Connection is valid
                } catch (SQLException e) {
                    System.err.println("Error executing test query: " + e.getMessage());
                    return false;  // Connection is not valid
                }
            } else {
                System.err.println("Connection is null or closed.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection status: " + e.getMessage());
            return false;  // Connection is not valid or couldn't be established
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
        default T handleError(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean addsupplier(Supplier supplier) {
        return executeWithRetry(conn -> {
            String sql = "INSERT INTO suppliers(supplier_code,supplier_name, phone, address, companyID) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, supplier.getSupplierCode());
                pstmt.setString(2, supplier.getSupplierName());
                pstmt.setString(3, supplier.getPhone());
                pstmt.setString(4, supplier.getAddress());
                pstmt.setString(5, supplier.getCompanyID());

                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0; // Returns true if the insert was successful
            }
        });
    }

    @Override
    public boolean updatesupplier(Supplier supplier) {
        return executeWithRetry(conn -> {
            String sql = "UPDATE suppliers SET supplier_name=?, supplier_code=?, phone=?, address=?, companyID=? WHERE supplier_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, supplier.getSupplierName());
                pstmt.setString(2, supplier.getSupplierCode());
                pstmt.setString(3, supplier.getPhone());
                pstmt.setString(4, supplier.getAddress());
                pstmt.setString(5, supplier.getCompanyID());
                pstmt.setInt(6, supplier.getSupplierId());
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean deletesupplier(int id) {
        return executeWithRetry(conn -> {
            String sql = "DELETE FROM suppliers WHERE supplier_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public List<Supplier> getAllSuppliers() {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM suppliers";
            List<Supplier> supplierList = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    supplierList.add(new Supplier(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("supplier_code"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("companyID")
                    ));
                }
            }
            return supplierList;
        });
    }

    @Override
    public Supplier getSupplierById(int id) {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM suppliers WHERE supplier_id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return new Supplier(
                            rs.getInt("supplier_id"),
                            rs.getString("supplier_name"),
                            rs.getString("supplier_code"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("companyID")
                        );
                    }
                }
            }
            return null;
        });
    }

	@Override
	public boolean getsupplier(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getsupplierdetials(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Supplier> search(Supplier supplier) {
		// TODO Auto-generated method stub
		return null;
	}

    
}
