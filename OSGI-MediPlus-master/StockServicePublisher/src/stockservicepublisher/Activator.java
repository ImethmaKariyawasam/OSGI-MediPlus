package stockservicepublisher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    private ServiceRegistration<StockService> serviceRegistration;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        // Create an instance of the StockService implementation
        StockService stockService = new StockServiceImplementation(bundleContext);

        // Register the StockService as an OSGi service
        serviceRegistration = bundleContext.registerService(StockService.class, stockService, null);
        System.out.println("StockService registered successfully.");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // Unregister the service when the bundle stops
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            System.out.println("StockService unregistered.");
        }
    }
}