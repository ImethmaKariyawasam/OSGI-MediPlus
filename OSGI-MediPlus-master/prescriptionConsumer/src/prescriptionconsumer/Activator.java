package prescriptionconsumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.patientPublisherService.IPatientService;
import com.prescriptionservicepublisher.IPrescriptionService;

import staffservicepublisher.IStaffService;
import stockservicepublisher.StockService;

public class Activator implements BundleActivator {

    private static BundleContext context;
    
    // Service references
    private ServiceReference<?> prescriptionServiceReference;
    private ServiceReference<?> patientServiceReference;
    private ServiceReference<?> staffServiceReference;
    private ServiceReference<?> stockServiceReference;
    // Service instances
    private IPrescriptionService prescriptionService;
    private IPatientService patientService;
    private IStaffService staffService;
    private StockService stockService;
    
    private PrescriptionConsumerUI consumerUI;
    
    static BundleContext getContext() {
        return context;
    }

    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        
        System.out.println("Prescription Service Consumer Started...");
        
        // Get all required service references
        prescriptionServiceReference = context.getServiceReference(IPrescriptionService.class.getName());
        patientServiceReference = context.getServiceReference(IPatientService.class.getName());
        staffServiceReference = context.getServiceReference(IStaffService.class.getName());
        stockServiceReference=context.getServiceReference(StockService.class.getName());
        boolean servicesAvailable = true;
        
        // Get prescription service (required)
        if (prescriptionServiceReference != null) {
            prescriptionService = (IPrescriptionService) context.getService(prescriptionServiceReference);
            if (prescriptionService != null) {
                System.out.println("Prescription Service Successfully Located");
            } else {
                System.err.println("Failed to get Prescription Service instance");
                servicesAvailable = false;
            }
        } else {
            System.err.println("Prescription Service is not available");
            servicesAvailable = false;
        }
        
        // Get patient service (required)
        if (patientServiceReference != null) {
            patientService = (IPatientService) context.getService(patientServiceReference);
            if (patientService != null) {
                System.out.println("Patient Service Successfully Located");
            } else {
                System.err.println("Failed to get Patient Service instance");
                servicesAvailable = false;
            }
        } else {
            System.err.println("Patient Service is not available");
            servicesAvailable = false;
        }
        
        // Get staff service (required)
        if (staffServiceReference != null) {
            staffService = (IStaffService) context.getService(staffServiceReference);
            if (staffService != null) {
                System.out.println("Staff Service Successfully Located");
            } else {
                System.err.println("Failed to get Staff Service instance");
                servicesAvailable = false;
            }
        } else {
            System.err.println("Staff Service is not available");
            servicesAvailable = false;
        }
        
        
     // Get stock service (required)
        if (stockServiceReference != null) {
            stockService = (StockService) context.getService(stockServiceReference);
            if (stockService != null) {
                System.out.println("Stock Service Successfully Located");
            } else {
                System.err.println("Failed to get Stock Service instance");
                servicesAvailable = false;
            }
        } else {
            System.err.println("Stock Service is not available");
            servicesAvailable = false;
        }
        
        
        // Start the UI if required services are available
        if (servicesAvailable) {
            // Start the UI in a separate thread to avoid blocking the OSGi framework
            new Thread(() -> {
                try {
                    // Pass all services to the UI
                    consumerUI = new PrescriptionConsumerUI(
                        prescriptionService,
                        patientService,
                        staffService,
                        stockService
                    );
                    consumerUI.open();
                } catch (Exception e) {
                    System.err.println("Error starting Prescription UI: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.err.println("Not all required services are available. UI will not start.");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        System.out.println("Prescription Service Consumer Stopped");
        
        // Close the UI if it's open
        if (consumerUI != null) {
            try {
                consumerUI.close();
                consumerUI = null;
            } catch (Exception e) {
                System.err.println("Error closing UI: " + e.getMessage());
            }
        }
        
        // Unget all services
        if (prescriptionServiceReference != null) {
            context.ungetService(prescriptionServiceReference);
            prescriptionServiceReference = null;
            prescriptionService = null;
        }
        
        if (patientServiceReference != null) {
            context.ungetService(patientServiceReference);
            patientServiceReference = null;
            patientService = null;
        }
        
        if (staffServiceReference != null) {
            context.ungetService(staffServiceReference);
            staffServiceReference = null;
            staffService = null;
        }
        
        if (stockServiceReference != null) {
            context.ungetService(stockServiceReference);
            stockServiceReference = null;
            stockService = null;
        }
        
        Activator.context = null;
    }
}