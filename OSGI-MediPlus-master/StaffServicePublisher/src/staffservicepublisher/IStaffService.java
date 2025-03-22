package staffservicepublisher;

import java.util.List;

public interface IStaffService {
    boolean addStaff(Staff staff);
    boolean updateStaff(Staff staff);
    boolean deleteStaff(int staffId);
    List<Staff> getAllStaff();
    List<Staff> getStaffByRole(String role);
    List<Staff> searchStaff(String term);
    Staff getStaffById(int staffId);
}