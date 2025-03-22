package departmentservicepublisher;

import com.hospital.core.database.IDatabaseService;
import staffservicepublisher.Staff;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class DepartmentServiceImpl implements IDepartmentService {

    private IDatabaseService dbService;
    private BundleContext context;
    private Connection connection;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;

    public DepartmentServiceImpl(BundleContext context) {
        this.context = context;
        initializeConnection();
    }

    private void initializeConnection() {
        ServiceReference<?> serviceReference = context.getServiceReference(IDatabaseService.class.getName());
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
            } catch (SQLException e) {
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
    public boolean addDepartment(Department department) {
        return executeWithRetry(conn -> {
            String sql = "INSERT INTO departments (name) VALUES (?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, department.getName());
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean updateDepartment(Department department) {
        return executeWithRetry(conn -> {
            String sql = "UPDATE departments SET name=? WHERE id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, department.getName());
                pstmt.setInt(2, department.getId());
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public boolean deleteDepartment(int departmentId) {
        return executeWithRetry(conn -> {
            String sql = "DELETE FROM departments WHERE id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, departmentId);
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public Department getDepartment(int id) {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM departments WHERE id=?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return new Department(
                        rs.getInt("id"),
                        rs.getString("name")
                    );
                }
                return null;
            }
        });
    }

    @Override
    public List<Department> searchDepartments(String term) {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM departments WHERE name LIKE ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String searchTerm = "%" + term + "%";
                pstmt.setString(1, searchTerm);

                ResultSet rs = pstmt.executeQuery();
                List<Department> departmentList = new ArrayList<>();

                while (rs.next()) {
                    departmentList.add(new Department(
                        rs.getInt("id"),
                        rs.getString("name")
                    ));
                }
                return departmentList;
            }
        });
    }

    @Override
    public String getDepartmentDetails(int id) {
        return executeWithRetry(conn -> {
            String sql = "SELECT id, name FROM departments WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return "ID: " + rs.getInt("id") +
                           "\nName: " + rs.getString("name");
                }
            }
            return "Department not found.";
        });
    }

    @Override
    public boolean assignStaffToDepartment(int staffId, int departmentId) {
        return executeWithRetry(conn -> {
            String sql = "INSERT INTO staff_department (staff_id, department_id) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, staffId);
                pstmt.setInt(2, departmentId);
                return pstmt.executeUpdate() > 0;
            }
        });
    }

    @Override
    public List<Staff> getStaffByDepartment(int departmentId) {
        return executeWithRetry(conn -> {
            String sql = "SELECT s.id, s.name, s.role, s.contact, s.departmentId " +
                         "FROM staff s " +
                         "JOIN staff_department sd ON s.id = sd.staff_id " +
                         "WHERE sd.department_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, departmentId);
                ResultSet rs = pstmt.executeQuery();
                List<Staff> staffList = new ArrayList<>();

                while (rs.next()) {
                    staffList.add(new Staff(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("contact"),
                        rs.getInt("departmentId")
                    ));
                }
                return staffList;
            }
        });
    }

    @Override
    public List<Department> getAllDepartments() {
        return executeWithRetry(conn -> {
            String sql = "SELECT * FROM departments";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                List<Department> departmentList = new ArrayList<>();

                while (rs.next()) {
                    departmentList.add(new Department(
                        rs.getInt("id"),
                        rs.getString("name")
                    ));
                }
                return departmentList;
            }
        });
    }
}