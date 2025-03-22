package stockshareconsumer;

import departmentservicepublisher.Department;
import departmentservicepublisher.DepartmentServiceImpl;
import departmentservicepublisher.IDepartmentService;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import stockservicepublisher.Stock;
import stockservicepublisher.StockService;
import stockshareservicepublisher.StockShare;
import stockshareservicepublisher.StockShareService;

public class Activator implements BundleActivator {

    private ServiceReference<?> stockShareServiceRef;
    private ServiceReference<?> stockServiceRef; 
    private ServiceReference<?> departmentServiceRef;
    private Display display;
    private Shell shell;
    private StockShareService stockShareService;
    private Table stockTable;
    private Font titleFont;
    private Font buttonFont;
    private Color headerColor;
    private Color buttonColor;
    private StockService stockservice;
    private IDepartmentService deparetmentservice;


    public void start(BundleContext context) throws Exception {
        stockShareServiceRef = context.getServiceReference(StockShareService.class.getName());
        stockShareService = (StockShareService) context.getService(stockShareServiceRef);
        if (stockShareService == null) {
            System.err.println("StockService is not available. Please ensure the service is running.");
            return;
        }
        
        stockServiceRef = context.getServiceReference(StockService.class.getName());
        stockservice = (StockService) context.getService(stockServiceRef);

        departmentServiceRef = context.getServiceReference(IDepartmentService.class);
        deparetmentservice = (IDepartmentService) context.getService(departmentServiceRef);

        
        display = new Display();
        shell = new Shell(display);
        shell.setText("Inventory Management System");
        shell.setSize(900, 600);
        shell.setLayout(new GridLayout(1, false));

        titleFont = new Font(display, new FontData("Arial", 14, SWT.BOLD));
        buttonFont = new Font(display, new FontData("Arial", 10, SWT.NORMAL));
        headerColor = new Color(display, 135, 206, 235); // Light blue
        buttonColor = new Color(display, 100, 149, 237); // Cornflower blue

        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setSimple(false);
        tabFolder.setUnselectedImageVisible(false);
        tabFolder.setUnselectedCloseVisible(false);

        CTabItem inventoryTab = new CTabItem(tabFolder, SWT.NONE);
        inventoryTab.setText("Inventory Management");
        Composite inventoryComposite = createInventoryTabContent(tabFolder);
        inventoryTab.setControl(inventoryComposite);

        tabFolder.setSelection(0);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        titleFont.dispose();
        buttonFont.dispose();
        headerColor.dispose();
        buttonColor.dispose();
        display.dispose();
    }

    private Composite createInventoryTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Stock Share Management System");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        GridData headerData = new GridData(SWT.FILL, SWT.TOP, true, false);
        headerData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerData);

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
        addStockButton.setText("Add Share Stock");
        addStockButton.setFont(buttonFont);
        addStockButton.setBackground(buttonColor);

        stockTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        stockTable.setHeaderVisible(true);
        stockTable.setLinesVisible(true);

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 800;
        gridData.heightHint = 400;
        stockTable.setLayoutData(gridData);

        String[] titles = {"StockShare ID", "Division", "Ward ID", "Item Name", "Category", "Quantity", "Provided Date"};
        for (String title : titles) {
            TableColumn column = new TableColumn(stockTable, SWT.NONE);
            column.setText(title);
            column.setWidth(130);
        }

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

        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshStockList();
            }
        });

        addStockButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openAddStockDialog();
                refreshStockList();

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
            System.out.println("Error: No item selected for deletion.");
            return;
        }

        TableItem selectedItem = stockTable.getItem(selectedIndex);
        int stockId = Integer.parseInt(selectedItem.getText(0));

        boolean isDeleted = stockShareService.deleteStockShare(stockId);
        if (isDeleted) {
            System.out.println("Stock shared deleted successfully.");
            refreshStockList();
        } else {
            System.out.println("Failed to delete stock shared.");
        }
    }

    private void updateSelectedStock() {
        int selectedIndex = stockTable.getSelectionIndex();
        if (selectedIndex == -1) {
            System.out.println("Please select a stock item to update.");
            return;
        }

        TableItem selectedItem = stockTable.getItem(selectedIndex);
        int stockShareId = Integer.parseInt(selectedItem.getText(0));
        String division = selectedItem.getText(1);
        String wardID = selectedItem.getText(2);  
        String itemName = selectedItem.getText(3);  
        String category = selectedItem.getText(4);  
        int quantity = Integer.parseInt(selectedItem.getText(5));
        String provideDate = selectedItem.getText(6);  

        try {
            Date provideDateParsed = convertStringToDate(provideDate);
            openUpdateStockDialog(stockShareId, division, wardID, itemName, category, quantity, provideDateParsed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void openUpdateStockDialog(int stockShareId, String division, String wardID, String itemName, String category, int quantity, Date provideDate) {
//        UpdateStockDialog dialog = new UpdateStockDialog(shell, stockShareId, division, wardID, itemName, category, quantity, provideDate);
//        dialog.open();
//        refreshStockList();
//    }
    
    private void openUpdateStockDialog(int stockShareId, String division, String wardID, String itemName, String category, int quantity, Date provideDate) {
        // Create a new shell (dialog window)
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Update Stock Share");

        // Modern layout with margins and spacing
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 15;
        gridLayout.marginHeight = 15;
        gridLayout.horizontalSpacing = 10;
        gridLayout.verticalSpacing = 10;
        dialogShell.setLayout(gridLayout);

        // Set a light gray background
        dialogShell.setBackground(new Color(display, 240, 240, 240));

        // Input field GridData for width adjustment
        GridData textFieldGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFieldGridData.widthHint = 300;

        // Division
        createLabel(dialogShell, "Division:");
        Text divisionText = new Text(dialogShell, SWT.BORDER);
        divisionText.setText(division);
        divisionText.setLayoutData(textFieldGridData);

        // Ward ID
        createLabel(dialogShell, "Ward ID:");
        Text wardIDText = new Text(dialogShell, SWT.BORDER);
        wardIDText.setText(wardID);
        wardIDText.setLayoutData(textFieldGridData);

        // Item Name (ComboBox)
        createLabel(dialogShell, "Item Name:");
        Combo itemNameCombo = new Combo(dialogShell, SWT.DROP_DOWN);
        itemNameCombo.setLayoutData(textFieldGridData);

        // Category (ComboBox)
        createLabel(dialogShell, "Category:");
        Combo categoryCombo = new Combo(dialogShell, SWT.DROP_DOWN);
        categoryCombo.setLayoutData(textFieldGridData);

        // Fetch item names and categories from the database
        List<Stock> stockList = stockservice.getAllStocks();
        Set<String> itemNames = new HashSet<>();
        Map<String, Set<String>> itemCategories = new HashMap<>();

        for (Stock stock : stockList) {
            itemNames.add(stock.getItemName());
            itemCategories.computeIfAbsent(stock.getItemName(), k -> new HashSet<>()).add(stock.getCategory());
        }

        // Populate Item Name ComboBox
        for (String name : itemNames) {
            itemNameCombo.add(name);
        }

        // Pre-select the existing item name
        itemNameCombo.setText(itemName);

        // Add listener to Item Name ComboBox to update Category ComboBox
        itemNameCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String selectedItem = itemNameCombo.getText();
                categoryCombo.removeAll(); // Clear previous categories

                if (selectedItem != null && !selectedItem.isEmpty()) {
                    Set<String> categories = itemCategories.get(selectedItem);
                    if (categories != null) {
                        for (String cat : categories) {
                            categoryCombo.add(cat);
                        }
                    }
                }
            }
        });

        // Pre-select the existing category
        categoryCombo.setText(category);

        // Quantity
        createLabel(dialogShell, "Quantity:");
        Text quantityText = new Text(dialogShell, SWT.BORDER);
        quantityText.setText(String.valueOf(quantity));
        quantityText.setLayoutData(textFieldGridData);

        // Provide Date (Date Picker)
        createLabel(dialogShell, "Provide Date:");
        DateTime datePicker = new DateTime(dialogShell, SWT.DATE | SWT.DROP_DOWN);
        datePicker.setDate(provideDate.getYear() + 1900, provideDate.getMonth(), provideDate.getDate());
        datePicker.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Buttons
        Composite buttonComposite = new Composite(dialogShell, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, true));
        buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));

        GridData buttonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        buttonGridData.widthHint = 120;

        // Save Button
        Button saveButton = new Button(buttonComposite, SWT.PUSH);
        saveButton.setText("Save Changes");
        saveButton.setLayoutData(buttonGridData);
        saveButton.setBackground(new Color(display, 40, 167, 69)); // Green
        saveButton.setForeground(new Color(display, 255, 255, 255)); // White

        // Cancel Button
        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(buttonGridData);
        cancelButton.setBackground(new Color(display, 220, 20, 60)); // Red
        cancelButton.setForeground(new Color(display, 255, 255, 255)); // White

        // Cancel button listener
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialogShell.close();
            }
        });

        // Save button listener
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    // Get input values
                    String updatedDivision = divisionText.getText().trim();
                    String updatedWardID = wardIDText.getText().trim();
                    String updatedItemName = itemNameCombo.getText().trim();
                    String updatedCategory = categoryCombo.getText().trim();
                    String quantityStr = quantityText.getText().trim();

                    // Validate inputs
                    if (updatedDivision.isEmpty() || updatedWardID.isEmpty() || updatedItemName.isEmpty() || updatedCategory.isEmpty() || quantityStr.isEmpty()) {
                        MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                        errorMessage.setText("Validation Error");
                        errorMessage.setMessage("All fields are required.");
                        errorMessage.open();
                        return;
                    }

                    // Ensure quantity is a valid integer
                    int updatedQuantity = Integer.parseInt(quantityStr);

                    // Get date from date picker
                    String year = String.valueOf(datePicker.getYear());
                    String month = String.format("%02d", datePicker.getMonth() + 1); // Month is 0-based
                    String day = String.format("%02d", datePicker.getDay());
                    String provideDateStr = String.format("%s-%s-%s", year, month, day);
                    Date updatedProvideDate = Date.valueOf(provideDateStr);

                    // Create a new StockShare object with updated values
                    StockShare updatedStock = new StockShare(stockShareId, updatedDivision, updatedWardID, updatedItemName, updatedCategory, updatedQuantity, updatedProvideDate);

                    // Update the stock share using the stockShareService
                    boolean isUpdated = stockShareService.updateStockShare(updatedStock);

                    if (isUpdated) {
                        // Show a success message
                        MessageBox successMessage = new MessageBox(dialogShell, SWT.ICON_INFORMATION | SWT.OK);
                        successMessage.setText("Success");
                        successMessage.setMessage("Stock updated successfully!");
                        successMessage.open();

                        refreshStockList(); // Refresh the table to show the updated stock
                        dialogShell.close(); // Close the dialog
                    } else {
                        // Show an error message if update fails
                        MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                        errorMessage.setText("Error");
                        errorMessage.setMessage("Failed to update stock.");
                        errorMessage.open();
                    }
                } catch (NumberFormatException ex) {
                    // Show an error message for invalid quantity
                    MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                    errorMessage.setText("Invalid Input");
                    errorMessage.setMessage("Quantity must be a number.");
                    errorMessage.open();
                } catch (IllegalArgumentException ex) {
                    // Show an error message for invalid date format
                    MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                    errorMessage.setText("Invalid Input");
                    errorMessage.setMessage("Invalid date format. Please use YYYY-MM-DD.");
                    errorMessage.open();
                } catch (Exception ex) {
                    // Show a generic error message
                    MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                    errorMessage.setText("Error");
                    errorMessage.setMessage("Error updating stock: " + ex.getMessage());
                    errorMessage.open();
                }
            }
        });

        // Center the dialog on the screen
        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = dialogShell.getBounds();
        dialogShell.setLocation((bounds.width - rect.width) / 2, (bounds.height - rect.height) / 2);

        dialogShell.pack();
        dialogShell.open();
    }



    private Date convertStringToDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Ensure the format matches the input
        try {
            java.util.Date utilDate = sdf.parse(dateString);
            return new Date(utilDate.getTime());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString);
        }
    }

    private void refreshStockList() {
        List<StockShare> stockList = stockShareService.getAllStocksShare();
        stockTable.removeAll();

        for (StockShare stock : stockList) {
            TableItem item = new TableItem(stockTable, SWT.NONE);
            item.setText(new String[]{
                String.valueOf(stock.getStockshareId()),
                stock.getDivision(),
                stock.getWardID(),
                stock.getItemName(),
                stock.getCategory(),
                String.valueOf(stock.getQuantity()),
                stock.getProvideDate().toString()});
        }
    }

 
    
    private void openAddStockDialog() {
        // Create a new shell (dialog window)
        Shell dialogShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialogShell.setText("Add New Stock Share");

        // Modern layout with margins and spacing
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 15;
        gridLayout.marginHeight = 15;
        gridLayout.horizontalSpacing = 10;
        gridLayout.verticalSpacing = 10;
        dialogShell.setLayout(gridLayout);

        // Set a light gray background
        dialogShell.setBackground(new Color(display, 240, 240, 240));

        // Input field GridData for width adjustment
        GridData textFieldGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textFieldGridData.widthHint = 300;

     // Division (ComboBox)
        createLabel(dialogShell, "Division:");
        Combo divisionCombo = new Combo(dialogShell, SWT.DROP_DOWN);
        divisionCombo.setLayoutData(textFieldGridData);

        // Fetch departments and populate the Combo
        List<Department> departments = deparetmentservice.getAllDepartments();
        for (Department department : departments) {
            divisionCombo.add(department.getName());
        }
        divisionCombo.select(0); // Select the first department by default
        // Ward ID
        createLabel(dialogShell, "Ward ID:");
        Text wardIDText = new Text(dialogShell, SWT.BORDER);
        wardIDText.setLayoutData(textFieldGridData);

        // Item Name (ComboBox)
        createLabel(dialogShell, "Item Name:");
        Combo itemNameCombo = new Combo(dialogShell, SWT.DROP_DOWN);
        itemNameCombo.setLayoutData(textFieldGridData);

        // Category (ComboBox)
        createLabel(dialogShell, "Category:");
        Combo categoryCombo = new Combo(dialogShell, SWT.DROP_DOWN);
        categoryCombo.setLayoutData(textFieldGridData);

        // Fetch item names and categories from the database
        List<Stock> stockList = stockservice.getAllStocks();
        Set<String> itemNames = new HashSet<>();
        Map<String, Set<String>> itemCategories = new HashMap<>();

        for (Stock stock : stockList) {
            itemNames.add(stock.getItemName());
            itemCategories.computeIfAbsent(stock.getItemName(), k -> new HashSet<>()).add(stock.getCategory());
        }

        // Populate Item Name ComboBox
        for (String itemName : itemNames) {
            itemNameCombo.add(itemName);
        }

        // Add listener to Item Name ComboBox to update Category ComboBox
        itemNameCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String selectedItem = itemNameCombo.getText();
                categoryCombo.removeAll(); // Clear previous categories

                if (selectedItem != null && !selectedItem.isEmpty()) {
                    Set<String> categories = itemCategories.get(selectedItem);
                    if (categories != null) {
                        for (String category : categories) {
                            categoryCombo.add(category);
                        }
                    }
                }
            }
        });
        categoryCombo.select(0); // Select the first supplier by default
        itemNameCombo.select(0); // Select the first supplier by default

        // Quantity
        createLabel(dialogShell, "Quantity:");
        Text quantityText = new Text(dialogShell, SWT.BORDER);
        quantityText.setLayoutData(textFieldGridData);

        // Provide Date (Date Picker)
        createLabel(dialogShell, "Provide Date:");
        DateTime datePicker = new DateTime(dialogShell, SWT.DATE | SWT.DROP_DOWN);
        datePicker.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Buttons
        Composite buttonComposite = new Composite(dialogShell, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(2, true));
        buttonComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1));

        GridData buttonGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        buttonGridData.widthHint = 120;

        // Save Button
        Button saveButton = new Button(buttonComposite, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setLayoutData(buttonGridData);
        saveButton.setBackground(new Color(display, 40, 167, 69)); // Green
        saveButton.setForeground(new Color(display, 255, 255, 255)); // White

        // Cancel Button
        Button cancelButton = new Button(buttonComposite, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(buttonGridData);
        cancelButton.setBackground(new Color(display, 220, 20, 60)); // Red
        cancelButton.setForeground(new Color(display, 255, 255, 255)); // White

        // Cancel button listener
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialogShell.close();
            }
        });

        // Save button listener
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    // Get input values
                    String division = divisionCombo.getText().trim();
                    String wardID = wardIDText.getText().trim();
                    String itemName = itemNameCombo.getText().trim();
                    String category = categoryCombo.getText().trim();
                    String quantityStr = quantityText.getText().trim();

                    // Validate inputs
                    if (division.isEmpty() || wardID.isEmpty() || itemName.isEmpty() || category.isEmpty() || quantityStr.isEmpty()) {
                        MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                        errorMessage.setText("Validation Error");
                        errorMessage.setMessage("All fields are required.");
                        errorMessage.open();
                        return;
                    }

                    // Ensure quantity is a valid integer
                    int quantity = Integer.parseInt(quantityStr);

                    // Get date from date picker
                    String year = String.valueOf(datePicker.getYear());
                    String month = String.format("%02d", datePicker.getMonth() + 1); // Month is 0-based
                    String day = String.format("%02d", datePicker.getDay());
                    String provideDateStr = String.format("%s-%s-%s", year, month, day);
                    Date provideDate = Date.valueOf(provideDateStr);

                    // Check if the item already exists in the database
                    List<Stock> existingStocks = stockservice.getStockWithShare(itemName, category);
                    if (existingStocks.isEmpty()) {
                        // If the item does not exist, add it with a quantity of 0
                        Stock newStock = new Stock(itemName, category, 0);
                        stockservice.addStocks(newStock);
                    }

                    // Create a new StockShare object
                    StockShare newStockShare = new StockShare(division, wardID, itemName, category, quantity, provideDate);

                    // Log newStockShare data for debugging
                    System.out.println("New Stock Share to be added: " + itemName + " - " + category);

                    // Add the new stock share using the stockShareService
                    boolean isAdded = stockShareService.addStockShare(newStockShare);

                    if (isAdded) {
                        // Show a success message
                        MessageBox successMessage = new MessageBox(dialogShell, SWT.ICON_INFORMATION | SWT.OK);
                        successMessage.setText("Success");
                        successMessage.setMessage("Stock added successfully!");
                        successMessage.open();

                        refreshStockList(); // Refresh the table to show the new stock
                        dialogShell.close(); // Close the dialog
                    } else {
                        // Show an error message if addition fails
                        MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                        errorMessage.setText("Error");
                        errorMessage.setMessage("Failed to add stock.");
                        errorMessage.open();
                    }
                } catch (NumberFormatException ex) {
                    // Show an error message for invalid quantity
                    MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                    errorMessage.setText("Invalid Input");
                    errorMessage.setMessage("Quantity must be a number.");
                    errorMessage.open();
                } catch (IllegalArgumentException ex) {
                    // Show an error message for invalid date format
                    MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                    errorMessage.setText("Invalid Input");
                    errorMessage.setMessage("Invalid date format. Please use YYYY-MM-DD.");
                    errorMessage.open();
                } catch (Exception ex) {
                    // Show a generic error message
                    MessageBox errorMessage = new MessageBox(dialogShell, SWT.ICON_ERROR | SWT.OK);
                    errorMessage.setText("Error");
                    errorMessage.setMessage("Error saving stock: " + ex.getMessage());
                    errorMessage.open();
                }
            }
        });

        // Center the dialog on the screen
        Monitor primary = display.getPrimaryMonitor();
        Rectangle bounds = primary.getBounds();
        Rectangle rect = dialogShell.getBounds();
        dialogShell.setLocation((bounds.width - rect.width) / 2, (bounds.height - rect.height) / 2);

        dialogShell.pack();
        dialogShell.open();
    }

    // Helper method to create labels with consistent styling
    private Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);

        // Set a readable font
        FontData fontData = new FontData("Segoe UI", 12, SWT.NORMAL);
        Font font = new Font(parent.getDisplay(), fontData);
        label.setFont(font);

        // Set a contrasting foreground color (e.g., black or dark gray)
        label.setForeground(new Color(parent.getDisplay(), 0, 0, 0)); // Black

        return label;
    }
    

    public void stop(BundleContext context) throws Exception {
        context.ungetService(stockShareServiceRef);
        display.dispose();
    }
}
