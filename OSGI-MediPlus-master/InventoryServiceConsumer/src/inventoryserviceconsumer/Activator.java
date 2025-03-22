package inventoryserviceconsumer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.ServiceReference;
import stockservicepublisher.Stock;
import stockservicepublisher.StockService;
import supplierservicepublisher.Supplier;
import supplierservicepublisher.SupplierService;
import supplierservicepublisher.SupplierServiceImplementation;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

public class Activator implements BundleActivator {

    private ServiceReference<?> stockServiceRef;
    private Display display;
    private Shell shell;
    private StockService stockService;
    private Table stockTable;
    private Font titleFont;
    private Font buttonFont;
    private Color headerColor;
    private Color buttonColor;
    private ServiceReference<?> supplierServiceRef;
    private SupplierService supplierservice;

    public void start(BundleContext context) throws Exception {
        stockServiceRef = context.getServiceReference(StockService.class.getName());
        stockService = (StockService) context.getService(stockServiceRef);
        
        supplierServiceRef = context.getServiceReference(SupplierService.class.getName());
        supplierservice = (SupplierService) context.getService(supplierServiceRef);

        
        // Check if the stock service is null
        if (stockService == null) {
            System.err.println("StockService is not available. Please ensure the service is running.");
            return;
        }

        // Create UI in the main thread
        display = new Display();
        shell = new Shell(display);
        shell.setText("Inventory Management System");
        shell.setSize(900, 600);
        shell.setLayout(new GridLayout(1, false));

        // Create fonts and colors
        titleFont = new Font(display, new FontData("Arial", 14, SWT.BOLD));
        buttonFont = new Font(display, new FontData("Arial", 10, SWT.NORMAL));
        headerColor = new Color(display, 135, 206, 235); // Light blue
        buttonColor = new Color(display, 100, 149, 237); // Cornflower blue

        // Create tab folder
        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setSimple(false);
        tabFolder.setUnselectedImageVisible(false);
        tabFolder.setUnselectedCloseVisible(false);

        // Create Inventory Management Tab
        CTabItem inventoryTab = new CTabItem(tabFolder, SWT.NONE);
        inventoryTab.setText("Inventory Management");
        Composite inventoryComposite = createInventoryTabContent(tabFolder);
        inventoryTab.setControl(inventoryComposite);

        tabFolder.setSelection(0);

        // Center the shell
        shell.open();

        // Event loop
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        // Dispose resources
        titleFont.dispose();
        buttonFont.dispose();
        headerColor.dispose();
        buttonColor.dispose();
        display.dispose();
    }

    private Composite createInventoryTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        // Header label
        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Inventory Management System");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        GridData headerData = new GridData(SWT.FILL, SWT.TOP, true, false);
        headerData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerData);

        // Create top controls
        Composite topControls = new Composite(composite, SWT.NONE);
        topControls.setLayout(new GridLayout(5, false));
        topControls.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Label searchLabel = new Label(topControls, SWT.NONE);
        searchLabel.setText("Search:");

        Text searchText = new Text(topControls, SWT.BORDER);
        searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button searchButton = new Button(topControls, SWT.PUSH);
        searchButton.setText("Search");
        searchButton.setFont(buttonFont);
        searchButton.setBackground(buttonColor);

        Button refreshButton = new Button(topControls, SWT.PUSH);
        refreshButton.setText("Refresh List");
        refreshButton.setFont(buttonFont);
        refreshButton.setBackground(buttonColor);

        Button addStockButton = new Button(topControls, SWT.PUSH);
        addStockButton.setText("Add New Stock");
        addStockButton.setFont(buttonFont);
        addStockButton.setBackground(buttonColor);

        // Create table to display stock items
        stockTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        stockTable.setHeaderVisible(true);
        stockTable.setLinesVisible(true);

        // Set layout data to fill the parent composite properly
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 800;  // Set preferred width
        gridData.heightHint = 400; // Set preferred height
        stockTable.setLayoutData(gridData);


        String[] titles = {"Item ID", "Item Name","Category", "Quantity", "Price", "Supplier","expiry_date","last_updated"};
        for (String title : titles) {
            TableColumn column = new TableColumn(stockTable, SWT.NONE);
            column.setText(title);
            column.setWidth(130);
        }

        // Create bottom controls for actions
        Composite bottomControls = new Composite(composite, SWT.NONE);
        bottomControls.setLayout(new GridLayout(3, true));
        bottomControls.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

        Button viewButton = new Button(bottomControls, SWT.PUSH);
        viewButton.setText("View Selected");
        viewButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        viewButton.setFont(buttonFont);
        viewButton.setBackground(buttonColor);

        Button updateButton = new Button(bottomControls, SWT.PUSH);
        updateButton.setText("Update Selected");
        updateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        updateButton.setFont(buttonFont);
        updateButton.setBackground(buttonColor);

        Button deleteButton = new Button(bottomControls, SWT.PUSH);
        deleteButton.setText("Delete Selected");
        deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        deleteButton.setFont(buttonFont);
        deleteButton.setBackground(buttonColor);

        // Button listeners
        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshStockList();
            }
        });

        addStockButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openAddStockDialog(); // Open the "Add New Stock" dialog
            }
        });
        
        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteSelectedStock();
                refreshStockList();

            }
        });

        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectedStock();
                refreshStockList();

            }
        });

        return composite;
    }
    
    private void deleteSelectedStock() {
        int selectedIndex = stockTable.getSelectionIndex();
        if (selectedIndex == -1) {
        	System.out.println("Error");
            return;
        }

        TableItem selectedItem = stockTable.getItem(selectedIndex);
        int stockId = Integer.parseInt(selectedItem.getText(0));

        boolean isDeleted = stockService.deleteStock(stockId);
        if (isDeleted) {
        	System.out.println("Stock deleted successfully.");
            refreshStockList();
        } else {
        	System.out.println("Failed to delete stock.");
        }
    }

    private void updateSelectedStock() {
        int selectedIndex = stockTable.getSelectionIndex();
        if (selectedIndex == -1) {
            System.out.println("Please select a stock item to update.");
            return;
        }

        TableItem selectedItem = stockTable.getItem(selectedIndex);
        int stockId = Integer.parseInt(selectedItem.getText(0));
        String itemName = selectedItem.getText(1);
        String category = selectedItem.getText(2);  // Assuming category is in column 2
        int quantity = Integer.parseInt(selectedItem.getText(3));
        double price = Double.parseDouble(selectedItem.getText(4));
        String supplier = selectedItem.getText(5);  // Assuming supplier is in column 5
        String expiryDateString = selectedItem.getText(6);  // Assuming expiry date is in column 6
        String lastUpdatedString = selectedItem.getText(7);  // Assuming last updated is in column 7

        // Debugging output
        System.out.println("Expiry Date String: " + expiryDateString);
        System.out.println("Last Updated String: " + lastUpdatedString);

        // Ensure the date strings are in the correct format
        try {
            Date expiryDate = convertStringToDate(expiryDateString);  // Adjust if needed
            Date lastUpdated = convertStringToDate(lastUpdatedString);  // Adjust if needed

            // Pass the updated parameters to the dialog
            openUpdateStockDialog(stockId, itemName, quantity, price, supplier, category, expiryDate, lastUpdated);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Please check the input.");
        }
    }

    // Helper method to convert String to Date
    private Date convertStringToDate(String dateString) {
        try {
            // Try to convert using the default format (YYYY-MM-DD)
            return Date.valueOf(dateString);
        } catch (IllegalArgumentException e) {
            // If invalid format, handle the exception and parse the date manually
            // For example, handle MM/DD/YYYY format
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                java.util.Date utilDate = sdf.parse(dateString);
                return new Date(utilDate.getTime());
            } catch (ParseException ex) {
                throw new IllegalArgumentException("Invalid date format.");
            }
        }
    }

    
    private void openUpdateStockDialog(int stockId, String itemName, int quantity, double price, String supplier, String category, Date expiryDate, Date lastUpdated) {
        Shell dialogShell = new Shell(stockTable.getShell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Update Stock");
        dialogShell.setLayout(new GridLayout(2, false));

        // Set a fixed size for better width
        dialogShell.setSize(400, 400); // Adjust width and height as needed
        dialogShell.pack(); // Pack before setting location to center it properly
        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = dialogShell.getBounds();
        dialogShell.setLocation((bounds.width - rect.width) / 2, (bounds.height - rect.height) / 2);

        // Input field GridData for width adjustment
        GridData textFieldGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFieldGridData.widthHint = 200; // Increase input field width

        // Add input fields for stock details
        new Label(dialogShell, SWT.NONE).setText("Item Name:");
        Text nameText = new Text(dialogShell, SWT.BORDER);
        nameText.setText(itemName);
        nameText.setLayoutData(textFieldGridData);

        new Label(dialogShell, SWT.NONE).setText("Category:");
        Text categoryText = new Text(dialogShell, SWT.BORDER);
        categoryText.setText(category);
        categoryText.setLayoutData(textFieldGridData);

        new Label(dialogShell, SWT.NONE).setText("Quantity:");
        Text quantityText = new Text(dialogShell, SWT.BORDER);
        quantityText.setText(String.valueOf(quantity));
        quantityText.setLayoutData(textFieldGridData);

        new Label(dialogShell, SWT.NONE).setText("Price:");
        Text priceText = new Text(dialogShell, SWT.BORDER);
        priceText.setText(String.valueOf(price));
        priceText.setLayoutData(textFieldGridData);

        // Replace the Text input field with a ComboBox for Supplier
        new Label(dialogShell, SWT.NONE).setText("Supplier:");
        Combo supplierCombo = new Combo(dialogShell, SWT.DROP_DOWN | SWT.READ_ONLY);
        supplierCombo.setLayoutData(textFieldGridData);

        // Fetch suppliers from SupplierService and populate the ComboBox
        List<Supplier> suppliers = supplierservice.getAllSuppliers();
        if (suppliers != null) {
            for (Supplier sup : suppliers) {
                supplierCombo.add(sup.getSupplierName()); // Add supplier names to the dropdown
            }
            supplierCombo.setText(supplier); // Set the current supplier
        }

        // Replace expiry date text field with DateTime picker
        new Label(dialogShell, SWT.NONE).setText("Expiry Date:");
        DateTime expiryDatePicker = new DateTime(dialogShell, SWT.DATE | SWT.DROP_DOWN);
        expiryDatePicker.setDate(expiryDate.getYear() + 1900, expiryDate.getMonth(), expiryDate.getDate());
        expiryDatePicker.setLayoutData(textFieldGridData);

        // Replace last updated text field with DateTime picker
        new Label(dialogShell, SWT.NONE).setText("Last Updated:");
        DateTime lastUpdatedPicker = new DateTime(dialogShell, SWT.DATE | SWT.DROP_DOWN);
        lastUpdatedPicker.setDate(lastUpdated.getYear() + 1900, lastUpdated.getMonth(), lastUpdated.getDate());
        lastUpdatedPicker.setLayoutData(textFieldGridData);

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
        saveButton.setBackground(display.getSystemColor(SWT.COLOR_GREEN));

        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(buttonGridData);
        cancelButton.setBackground(display.getSystemColor(SWT.COLOR_GRAY));

        // Save button listener
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    // Get input values
                    String newItemName = nameText.getText();
                    String newCategory = categoryText.getText();
                    int newQuantity = Integer.parseInt(quantityText.getText());
                    double newPrice = Double.parseDouble(priceText.getText());
                    String newSupplier = supplierCombo.getText();  // Get selected supplier from the Combo box

                    // Get date values from DateTime pickers
                    LocalDate newExpiryDate = LocalDate.of(expiryDatePicker.getYear(), expiryDatePicker.getMonth() + 1, expiryDatePicker.getDay());
                    LocalDate newLastUpdated = LocalDate.of(lastUpdatedPicker.getYear(), lastUpdatedPicker.getMonth() + 1, lastUpdatedPicker.getDay());

                    // Convert LocalDate to SQL Date
                    Date sqlExpiryDate = Date.valueOf(newExpiryDate);
                    Date sqlLastUpdated = Date.valueOf(newLastUpdated);

                    // Create an updated Stock object
                    Stock updatedStock = new Stock(stockId, newItemName, newCategory, newQuantity, newPrice, newSupplier, sqlExpiryDate, sqlLastUpdated);

                    // Log updatedStock data for debugging
                    System.out.println("Updated Stock: " + updatedStock);

                    // Update the stock using the StockService
                    boolean isUpdated = stockService.updateStock(updatedStock);

                    if (isUpdated) {
                        System.out.println("Stock updated successfully.");
                        refreshStockList(); // Refresh the table to show the updated stock
                        dialogShell.close(); // Close the dialog
                    } else {
                        System.err.println("Failed to update stock.");
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid input: Quantity and Price must be numbers.");
                } catch (Exception ex) {
                    System.err.println("Error updating stock: " + ex.getMessage());
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


    private void openAddStockDialog() {
        // Create a new shell (dialog window)
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Add New Stock");
        dialogShell.setLayout(new GridLayout(2, false));

        // Set a fixed size for better width
        dialogShell.setSize(400, 400); // Adjust width and height as needed
        dialogShell.pack(); // Pack before setting location to center it properly
        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = dialogShell.getBounds();
        dialogShell.setLocation((bounds.width - rect.width) / 2, (bounds.height - rect.height) / 2);

        // Input field GridData for width adjustment
        GridData textFieldGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFieldGridData.widthHint = 200; // Increase input field width

        // Add input fields for stock details
        new Label(dialogShell, SWT.NONE).setText("Item Name:");
        Text nameText = new Text(dialogShell, SWT.BORDER);
        nameText.setLayoutData(textFieldGridData);

        new Label(dialogShell, SWT.NONE).setText("Category:");
        Text categoryText = new Text(dialogShell, SWT.BORDER);
        categoryText.setLayoutData(textFieldGridData);

        new Label(dialogShell, SWT.NONE).setText("Quantity:");
        Text quantityText = new Text(dialogShell, SWT.BORDER);
        quantityText.setLayoutData(textFieldGridData);

        new Label(dialogShell, SWT.NONE).setText("Price:");
        Text priceText = new Text(dialogShell, SWT.BORDER);
        priceText.setLayoutData(textFieldGridData);

        // Replace the Text input field with a ComboBox for Supplier
        new Label(dialogShell, SWT.NONE).setText("Supplier:");
        Combo supplierCombo = new Combo(dialogShell, SWT.DROP_DOWN | SWT.READ_ONLY);
        supplierCombo.setLayoutData(textFieldGridData);

        // Fetch suppliers from SupplierService and populate the ComboBox
        List<Supplier> suppliers = supplierservice.getAllSuppliers();
        if (suppliers != null) {
            for (Supplier supplier : suppliers) {
                supplierCombo.add(supplier.getSupplierName()); // Add supplier names to the dropdown
            }
            if (!suppliers.isEmpty()) {
                supplierCombo.select(0); // Select the first supplier by default
            }
        }

        // Replace expiry date text field with DateTime picker
        new Label(dialogShell, SWT.NONE).setText("Expiry Date:");
        DateTime expiryDatePicker = new DateTime(dialogShell, SWT.DATE | SWT.DROP_DOWN);
        expiryDatePicker.setLayoutData(textFieldGridData);

        // Replace last updated text field with DateTime picker
        new Label(dialogShell, SWT.NONE).setText("Last Updated:");
        DateTime lastUpdatedPicker = new DateTime(dialogShell, SWT.DATE | SWT.DROP_DOWN);
        lastUpdatedPicker.setLayoutData(textFieldGridData);

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
        saveButton.setBackground(display.getSystemColor(SWT.COLOR_GREEN));

        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(buttonGridData);
        cancelButton.setBackground(display.getSystemColor(SWT.COLOR_GRAY));

        // Save button listener
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    // Get input values
                    String itemName = nameText.getText();
                    String category = categoryText.getText();
                    int quantity = Integer.parseInt(quantityText.getText());
                    double price = Double.parseDouble(priceText.getText());
                    String supplier = supplierCombo.getText();  // Get selected supplier from the Combo box

                    // Get date values from DateTime pickers
                    LocalDate expiryDate = LocalDate.of(expiryDatePicker.getYear(), expiryDatePicker.getMonth() + 1, expiryDatePicker.getDay());
                    LocalDate lastUpdated = LocalDate.of(lastUpdatedPicker.getYear(), lastUpdatedPicker.getMonth() + 1, lastUpdatedPicker.getDay());

                    // Convert LocalDate to SQL Date
                    Date sqlExpiryDate = Date.valueOf(expiryDate);
                    Date sqlLastUpdated = Date.valueOf(lastUpdated);

                    // Create a new Stock object
                    Stock newStock = new Stock(itemName, category, quantity, price, supplier, sqlExpiryDate, sqlLastUpdated);

                    // Log newStock data for debugging
                    System.out.println("New Stock to be added: " + newStock);

                    // Add the new stock using the StockService
                    boolean isAdded = stockService.addStocks(newStock);

                    if (isAdded) {
                        System.out.println("Stock added successfully.");
                        refreshStockList(); // Refresh the table to show the new stock
                        dialogShell.close(); // Close the dialog
                    } else {
                        System.err.println("Failed to add stock.");
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("Invalid input: Quantity and Price must be numbers.");
                } catch (Exception ex) {
                    System.err.println("Error saving stock: " + ex.getMessage());
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


  
     
    
    
    
    private void refreshStockList() {
        if (stockService == null) {
            System.err.println("StockService is not available.");
            return;
        }

        // Fetch stock data from the service
        List<Stock> stockList = stockService.getAllStocks();
        
        // Clear existing table data
        stockTable.removeAll();

        // Populate table with stock data
        for (Stock stock : stockList) {
            TableItem item = new TableItem(stockTable, SWT.NONE);
            item.setText(new String[]{
                String.valueOf(stock.getItemId()), 
                stock.getItemName(), 
                String.valueOf(stock.getCategory()),
                String.valueOf(stock.getQuantity()), 
                String.valueOf(stock.getUnitPrice()),
                String.valueOf(stock.getSupplier()),
                String.valueOf(stock.getExpiryDate()),
                String.valueOf(stock.getLastUpdated())
            });
        }
    }


    public void stop(BundleContext context) throws Exception {
        // Clean up when the bundle is stopped
    }
}