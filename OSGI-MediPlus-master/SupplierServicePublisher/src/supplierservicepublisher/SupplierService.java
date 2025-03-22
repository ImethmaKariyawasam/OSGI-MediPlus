package supplierservicepublisher;

import java.util.List;

public interface SupplierService {

	boolean addsupplier(Supplier supplier);
	boolean updatesupplier(Supplier supplier);
	boolean deletesupplier(int id);
	boolean getsupplier(int id);
	boolean getsupplierdetials(String id);
	List<Supplier> search(Supplier supplier);
	List<Supplier> getAllSuppliers();
    Supplier getSupplierById(int id);

}
