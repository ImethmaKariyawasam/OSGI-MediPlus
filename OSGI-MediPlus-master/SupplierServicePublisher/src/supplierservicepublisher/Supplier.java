package supplierservicepublisher;

public class Supplier {
    
    private int supplierId;
    private String supplierCode;
    private String supplierName;
    private String phone;
    private String address;
    private String companyID;

    // 🔹 Constructor
    public Supplier(int supplierId, String supplierCode, String supplierName, String phone, String address, String companyID) {
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.phone = phone; 
        this.address = address;
        this.companyID = companyID;
    }
    public Supplier( String supplierCode, String supplierName, String phone, String address, String companyID) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.phone = phone; 
        this.address = address;
        this.companyID = companyID;
    }


    // 🔹 Getters
    public int getSupplierId() { return supplierId; }
    public String getSupplierCode() { return supplierCode; }
    public String getSupplierName() { return supplierName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCompanyID() { return companyID; }

    // 🔹 Setters
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setCompanyID(String companyID) { this.companyID = companyID; }

    // 🔹 toString() - For Displaying Supplier Details
    @Override
    public String toString() {
        return "Supplier {" +
                "ID=" + supplierId +
                ", Code='" + supplierCode + '\'' +
                ", Name='" + supplierName + '\'' +
                ", Phone='" + phone + '\'' +
                ", Address='" + address + '\'' +
                ", CompanyID='" + companyID + '\'' +
                '}';
    }
}
