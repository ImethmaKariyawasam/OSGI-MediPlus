package supplierservicepublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<?> serviceRegistration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("Starting Supplier Service...");

        // Pass the BundleContext to the SupplierServiceImplementation
        SupplierService supplierService = new SupplierServiceImplementation(bundleContext);

        // Register the service
        serviceRegistration = bundleContext.registerService(SupplierService.class.getName(), supplierService, null);

        System.out.println("SupplierService registered successfully.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("Stopping Supplier Service...");

        // Unregister the service
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }

        System.out.println("SupplierService stopped.");
    }
}
