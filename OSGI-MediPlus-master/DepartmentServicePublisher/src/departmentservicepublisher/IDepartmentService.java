package departmentservicepublisher;

import com.hospital.core.database.IDatabaseService;
import staffservicepublisher.Staff;

import java.util.List;

public interface IDepartmentService {
    
    boolean addDepartment(Department department);

    boolean updateDepartment(Department department);
    
    boolean deleteDepartment(int departmentId);
    
    Department getDepartment(int id);

    List<Department> searchDepartments(String term);

    String getDepartmentDetails(int id);

    boolean assignStaffToDepartment(int staffId, int departmentId);

    List<Staff> getStaffByDepartment(int departmentId);

    List<Department> getAllDepartments();
}