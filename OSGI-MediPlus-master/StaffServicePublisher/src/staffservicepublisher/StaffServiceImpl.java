package staffservicepublisher;

import com.hospital.core.database.IDatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class StaffServiceImpl implements IStaffService {

    private IDatabaseService dbService;
    private BundleContext context;
    private Connection connection;

    public StaffServiceImpl(BundleContext context) {
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

    @Override
    public boolean addStaff(Staff staff) {
        String sql = "INSERT INTO staff (name, role, contact, department_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getRole());
            pstmt.setString(3, staff.getContact());
            pstmt.setInt(4, staff.getDepartmentId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStaff(Staff staff) {
        String sql = "UPDATE staff SET name = ?, role = ?, contact = ?, department_id = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getRole());
            pstmt.setString(3, staff.getContact());
            pstmt.setInt(4, staff.getDepartmentId());
            pstmt.setInt(5, staff.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteStaff(int staffId) {
        String sql = "DELETE FROM staff WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        
        String sql = "SELECT s.id, s.name, s.role, s.contact, s.department_id, d.name AS department_name " +
                     "FROM staff s " +
                     "LEFT JOIN departments d ON s.department_id = d.id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("contact"),
                    rs.getInt("department_id")
                );
               
                staff.setDepartmentName(rs.getString("department_name"));
                staffList.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }
   
    @Override
    public List<Staff> getStaffByRole(String role) {
        List<Staff> staffList = new ArrayList<>();

        // Modified SQL query to filter by role
        String sql = "SELECT s.id, s.name, s.role, s.contact, s.department_id, d.name AS department_name " +
                     "FROM staff s " +
                     "LEFT JOIN departments d ON s.department_id = d.id " +
                     "WHERE s.role = ?"; // Adding WHERE clause to filter by role

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role); // Setting the role parameter in the query
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("contact"),
                    rs.getInt("department_id")
                );

                staff.setDepartmentName(rs.getString("department_name"));
                staffList.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    
    @Override
    public List<Staff> searchStaff(String term) {
        List<Staff> staffList = new ArrayList<>();
        
        String sql = "SELECT s.id, s.name, s.role, s.contact, s.department_id, d.name AS department_name " +
                     "FROM staff s " +
                     "LEFT JOIN departments d ON s.department_id = d.id " +
                     "WHERE s.name LIKE ? OR s.role LIKE ? OR s.contact LIKE ? OR d.name LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + term + "%");
            pstmt.setString(2, "%" + term + "%");
            pstmt.setString(3, "%" + term + "%");
            pstmt.setString(4, "%" + term + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("contact"),
                    rs.getInt("department_id")
                );
                
                staff.setDepartmentName(rs.getString("department_name"));
                staffList.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    @Override
    public Staff getStaffById(int staffId) {
       
        String sql = "SELECT s.id, s.name, s.role, s.contact, s.department_id, d.name AS department_name " +
                     "FROM staff s " +
                     "LEFT JOIN departments d ON s.department_id = d.id " +
                     "WHERE s.id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getString("contact"),
                    rs.getInt("department_id")
                );
                
                staff.setDepartmentName(rs.getString("department_name"));
                return staff;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}