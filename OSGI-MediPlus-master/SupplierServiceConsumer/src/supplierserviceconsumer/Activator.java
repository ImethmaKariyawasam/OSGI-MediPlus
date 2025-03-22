package supplierserviceconsumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import supplierservicepublisher.Supplier;
import supplierservicepublisher.SupplierService;

import java.util.List;

public class Activator implements BundleActivator {
    private ServiceReference<?> supplierServiceRef;
    private Display display;
    private Shell shell;
    private SupplierService supplierService;
    private Table supplierTable;

    public void start(BundleContext context) throws Exception {
        supplierServiceRef = context.getServiceReference(SupplierService.class.getName());
        supplierService = (SupplierService) context.getService(supplierServiceRef);

        if (supplierService == null) {
            System.err.println("SupplierService is not available. Please ensure the service is running.");
            return;
        }

        display = new Display();
        shell = new Shell(display);
        shell.setText("Supplier Management System");
        shell.setSize(900, 600);
        shell.setLayout(new GridLayout(1, false));

        // Set a gradient background for the main shell
        shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
        shell.addListener(SWT.Paint, e -> {
            Rectangle rect = shell.getClientArea();
            Color color1 = new Color(display, 30, 144, 255); // Dodger Blue
            Color color2 = new Color(display, 135, 206, 250); // Light Sky Blue
            e.gc.setForeground(color1);
            e.gc.setBackground(color2);
            e.gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true);
        });

        // Create a banner at the top
        Label banner = new Label(shell, SWT.CENTER);
        banner.setText("Supplier Management System");
        banner.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        banner.setFont(new Font(display, "Arial", 18, SWT.BOLD));
        banner.setForeground(display.getSystemColor(SWT.COLOR_WHITE));

        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tabFolder.setSimple(false); // Enable gradient rendering
        tabFolder.setSelectionBackground(new Color[] {
            new Color(display, 173, 216, 230), // Light Blue
            new Color(display, 135, 206, 250) // Light Sky Blue
        }, new int[] {100}, true);

        CTabItem supplierTab = new CTabItem(tabFolder, SWT.NONE);
        supplierTab.setText("Supplier Management");
        Composite supplierComposite = createSupplierTabContent(tabFolder);
        supplierTab.setControl(supplierComposite);
        tabFolder.setSelection(0);

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private Composite createSupplierTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        supplierTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        supplierTable.setHeaderVisible(true);
        supplierTable.setLinesVisible(true);
        supplierTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // Customize table header
        supplierTable.setHeaderBackground(new Color(display, 30, 144, 255)); // Dodger Blue
        supplierTable.setHeaderForeground(display.getSystemColor(SWT.COLOR_WHITE));

        String[] titles = {"Supplier ID", "Supplier Code", "Supplier Name", "Phone", "Address", "Company ID"};
        for (String title : titles) {
            TableColumn column = new TableColumn(supplierTable, SWT.NONE);
            column.setText(title);
            column.setWidth(130);
        }

        // Add alternating row colors
        supplierTable.addListener(SWT.EraseItem, event -> {
            if ((event.index & 1) == 0) {
                event.gc.setBackground(new Color(display, 240, 248, 255)); // Alice Blue
            } else {
                event.gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            }
            event.detail &= ~SWT.HOT;
        });

        Button refreshButton = new Button(composite, SWT.PUSH);
        refreshButton.setText("Refresh List");
        refreshButton.setBackground(new Color(display, 30, 144, 255)); // Dodger Blue
        refreshButton.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshSupplierList();
            }
        });

        Button addSupplierButton = new Button(composite, SWT.PUSH);
        addSupplierButton.setText("Add New Supplier");
        addSupplierButton.setBackground(new Color(display, 50, 205, 50)); // Lime Green
        addSupplierButton.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        addSupplierButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openAddSupplierDialog();
            }
        });

        return composite;
    }

    private void openAddSupplierDialog() {
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Add New Supplier");
        dialogShell.setLayout(new GridLayout(2, false));

        // Set a fixed size for the dialog
        dialogShell.setSize(400, 300);

        // Center the dialog on the screen
        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = dialogShell.getBounds();
        dialogShell.setLocation((bounds.width - rect.width) / 2, (bounds.height - rect.height) / 2);

        // Input field GridData for width adjustment
        GridData textFieldGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFieldGridData.widthHint = 200; // Increase input field width

        // Add input fields for supplier details
        createLabel(dialogShell, "Supplier Code:");
        Text codeText = new Text(dialogShell, SWT.BORDER);
        codeText.setLayoutData(textFieldGridData);

        createLabel(dialogShell, "Supplier Name:");
        Text nameText = new Text(dialogShell, SWT.BORDER);
        nameText.setLayoutData(textFieldGridData);

        createLabel(dialogShell, "Phone:");
        Text phoneText = new Text(dialogShell, SWT.BORDER);
        phoneText.setLayoutData(textFieldGridData);

        createLabel(dialogShell, "Address:");
        Text addressText = new Text(dialogShell, SWT.BORDER);
        addressText.setLayoutData(textFieldGridData);

        createLabel(dialogShell, "Company ID:");
        Text companyIdText = new Text(dialogShell, SWT.BORDER);
        companyIdText.setLayoutData(textFieldGridData);

        // Create a composite for buttons (to center them)
        Composite buttonComposite = new Composite(dialogShell, SWT.NONE);
        buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));
        buttonComposite.setLayout(new GridLayout(2, true));

        GridData buttonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        buttonGridData.widthHint = 120; // Increase button width

        // Add Save and Cancel buttons
        Button saveButton = new Button(buttonComposite, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setLayoutData(buttonGridData);
        saveButton.setBackground(display.getSystemColor(SWT.COLOR_GREEN)); // Green background
        saveButton.setForeground(display.getSystemColor(SWT.COLOR_WHITE)); // White text

        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(buttonGridData);
        cancelButton.setBackground(display.getSystemColor(SWT.COLOR_GRAY)); // Gray background
        cancelButton.setForeground(display.getSystemColor(SWT.COLOR_WHITE)); // White text

        // Save button listener
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    // Get input values
                    String supplierCode = codeText.getText();
                    String supplierName = nameText.getText();
                    String phone = phoneText.getText();
                    String address = addressText.getText();
                    String companyId = companyIdText.getText();

                    // Create a new Supplier object
                    Supplier newSupplier = new Supplier(supplierCode, supplierName, phone, address, companyId);

                    // Add the new supplier using the SupplierService
                    boolean isAdded = supplierService.addsupplier(newSupplier);

                    if (isAdded) {
                        // Show a success message
                        MessageBox successMessage = new MessageBox(dialogShell, SWT.ICON_INFORMATION | SWT.OK);
                        successMessage.setText("Success");
                        successMessage.setMessage("Supplier added successfully!");
                        successMessage.open();

                        refreshSupplierList(); // Refresh the table
                        dialogShell.close(); // Close the dialog
                    } else {
                        // Show an error message if addition fails
                        MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                        errorMessage.setText("Error");
                        errorMessage.setMessage("Failed to add supplier.");
                        errorMessage.open();
                    }
                } catch (Exception ex) {
                    // Show a generic error message
                    MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                    errorMessage.setText("Error");
                    errorMessage.setMessage("Error adding supplier: " + ex.getMessage());
                    errorMessage.open();
                }
            }
        });

        // Cancel button listener
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialogShell.close(); // Close the dialog without saving
            }
        });

        // Open the dialog
        dialogShell.pack();
        dialogShell.open();

        // Event loop for the dialog
        while (!dialogShell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    private void refreshSupplierList() {
        if (supplierService == null) return;
        supplierTable.removeAll();
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            TableItem item = new TableItem(supplierTable, SWT.NONE);
            item.setText(new String[]{
                String.valueOf(supplier.getSupplierId()),
                supplier.getSupplierCode(),
                supplier.getSupplierName(),
                supplier.getPhone(),
                supplier.getAddress(),
                String.valueOf(supplier.getCompanyID())
            });
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        bundleContext.ungetService(supplierServiceRef);
    }
}