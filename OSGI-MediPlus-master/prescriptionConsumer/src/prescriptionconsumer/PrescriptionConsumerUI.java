package prescriptionconsumer;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import staffservicepublisher.IStaffService;
import staffservicepublisher.Staff;
import stockservicepublisher.Stock;
import stockservicepublisher.StockService;

import com.patientPublisherService.IPatientService;
import com.patientPublisherService.Patient;
import com.prescriptionservicepublisher.IPrescriptionService;
import com.prescriptionservicepublisher.Prescription;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class PrescriptionConsumerUI {
    private Display display;
    private Shell shell;
    private IPrescriptionService prescriptionService;
    private IPatientService patientService;
    private IStaffService staffService;
    private StockService stockService;
    
    // UI styling elements
    private Font titleFont;
    private Font buttonFont;
    private Color headerColor;
    private Color buttonColor;
    
    // UI Components
    private Table tblPrescriptions;
    private Text searchText;
    
    public PrescriptionConsumerUI(IPrescriptionService prescriptionService, IPatientService patientService, IStaffService staffService,StockService stockService) {
        this.prescriptionService = prescriptionService;
        this.patientService= patientService;
        this.staffService=staffService;
        this.stockService=stockService;
        
        display = new Display();
        shell = new Shell(display);
        shell.setText("Hospital Prescription Management System");
        shell.setSize(1000, 700);
        
        // Center the shell
        Monitor primary = display.getPrimaryMonitor();
        shell.setLocation(
            (primary.getBounds().width - shell.getSize().x) / 2,
            (primary.getBounds().height - shell.getSize().y) / 2
        );
        
        // Initialize fonts and colors
        titleFont = new Font(display, new FontData("Arial", 14, SWT.BOLD));
        buttonFont = new Font(display, new FontData("Arial", 10, SWT.NORMAL));
        headerColor = new Color(display, 135, 206, 235); // Light sky blue
        buttonColor = new Color(display, 100, 149, 237); // Cornflower blue
        
        createContents();
    }
    
    public void open() {
        shell.open();
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
    
    public void close() {
        if (display != null && !display.isDisposed()) {
            display.syncExec(() -> {
                if (shell != null && !shell.isDisposed()) {
                    shell.close();
                }
            });
        }
    }
    
    private void createContents() {
        shell.setLayout(new FillLayout());
        
        // Create tab folder
        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setSimple(false);
        tabFolder.setUnselectedImageVisible(false);
        tabFolder.setUnselectedCloseVisible(false);
        
        // Create prescription tab
        CTabItem prescriptionTab = new CTabItem(tabFolder, SWT.NONE);
        prescriptionTab.setText("Prescription Management");
        Composite prescriptionComposite = createPrescriptionTabContent(tabFolder);
        prescriptionTab.setControl(prescriptionComposite);
        
        tabFolder.setSelection(0);
    }
    
    private Composite createPrescriptionTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        
        // Header with title
        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Prescription Management System");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        GridData headerData = new GridData(SWT.FILL, SWT.TOP, true, false);
        headerData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerData);
        
        // Search controls
        Composite searchControls = new Composite(composite, SWT.NONE);
        searchControls.setLayout(new GridLayout(4, false));
        searchControls.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label searchLabel = new Label(searchControls, SWT.NONE);
        searchLabel.setText("Search Patient:");
        
        searchText = new Text(searchControls, SWT.BORDER);
        searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button searchButton = new Button(searchControls, SWT.PUSH);
        searchButton.setText("Search");
        searchButton.setFont(buttonFont);
        searchButton.setBackground(buttonColor);
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                searchPrescriptions();
            }
        });
        
        Button refreshButton = new Button(searchControls, SWT.PUSH);
        refreshButton.setText("Refresh List");
        refreshButton.setFont(buttonFont);
        refreshButton.setBackground(buttonColor);
        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loadPrescriptions();
                clearForm();
            }
        });
        
        // Split view - Main content area divided into table and form
        Composite mainContent = new Composite(composite, SWT.NONE);
        mainContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mainContent.setLayout(new GridLayout(2, false));
        
        // Left side - Prescription list
        createPrescriptionTable(mainContent);
        
        // Right side - Prescription form
        createPrescriptionForm(mainContent);
        
        // Action buttons at the bottom
        createButtonBar(composite);
        
        // Load data
        loadPrescriptions();
        
        return composite;
    }
    
    private void createPrescriptionTable(Composite parent) {
        Group tableGroup = new Group(parent, SWT.NONE);
        tableGroup.setText("Prescription List");
        tableGroup.setLayout(new GridLayout(1, false));
        tableGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        tblPrescriptions = new Table(tableGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        tblPrescriptions.setHeaderVisible(true);
        tblPrescriptions.setLinesVisible(true);
        GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableData.heightHint = 400;
        tblPrescriptions.setLayoutData(tableData);
        
        String[] titles = {"Patient", "Doctor", "Medicine", "Dosage", "Status", "Date"};
        for (String title : titles) {
            TableColumn column = new TableColumn(tblPrescriptions, SWT.NONE);
            column.setText(title);
            column.setWidth(120);
        }
        
        tblPrescriptions.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = tblPrescriptions.getSelection();
                if (items.length > 0) {
                    showPrescriptionDetails((Prescription)items[0].getData());
                }
            }
        });
    }
    
    private void createPrescriptionForm(Composite parent) {
        Group formGroup = new Group(parent, SWT.NONE);
        formGroup.setText("Prescription Details");
        formGroup.setLayout(new GridLayout(2, false));
        formGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Patient ID
        new Label(formGroup, SWT.NONE).setText("Patient ID:");
        Text txtPatientId = new Text(formGroup, SWT.BORDER);
        txtPatientId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtPatientId.setData("field", "patientId");
        
        // Doctor ID
        new Label(formGroup, SWT.NONE).setText("Doctor ID:");
        Text txtDoctorId = new Text(formGroup, SWT.BORDER);
        txtDoctorId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtDoctorId.setData("field", "doctorId");
        
        // Item ID
        new Label(formGroup, SWT.NONE).setText("Item ID (Optional):");
        Text txtItemId = new Text(formGroup, SWT.BORDER);
        txtItemId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtItemId.setData("field", "itemId");
        
        // Medicine
        new Label(formGroup, SWT.NONE).setText("Medicine:");
        Text txtMedicine = new Text(formGroup, SWT.BORDER);
        txtMedicine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtMedicine.setData("field", "medicine");
        
        // Dosage
        new Label(formGroup, SWT.NONE).setText("Dosage:");
        Text txtDosage = new Text(formGroup, SWT.BORDER);
        txtDosage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtDosage.setData("field", "dosage");
        
        // Dosage Type
        new Label(formGroup, SWT.NONE).setText("Dosage Type:");
        Combo cbDosageType = new Combo(formGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbDosageType.setItems(new String[] {"mg", "ml", "tablet", "capsule", "puff"});
        cbDosageType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbDosageType.select(0);
        cbDosageType.setData("field", "dosageType");
        
        // Frequency
        new Label(formGroup, SWT.NONE).setText("Frequency (per day):");
        Text txtFrequency = new Text(formGroup, SWT.BORDER);
        txtFrequency.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtFrequency.setData("field", "frequency");
        
        // Duration
        new Label(formGroup, SWT.NONE).setText("Duration (days):");
        Text txtDuration = new Text(formGroup, SWT.BORDER);
        txtDuration.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        txtDuration.setData("field", "duration");
        
        // Food Relation
        new Label(formGroup, SWT.NONE).setText("Food Relation:");
        Combo cbFoodRelation = new Combo(formGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbFoodRelation.setItems(new String[] {"Before Food", "After Food", "With Food"});
        cbFoodRelation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbFoodRelation.select(0);
        cbFoodRelation.setData("field", "foodRelation");
        
        // Instructions
        new Label(formGroup, SWT.NONE).setText("Instructions:");
        Text txtInstructions = new Text(formGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData gdInstructions = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdInstructions.heightHint = 60;
        txtInstructions.setLayoutData(gdInstructions);
        txtInstructions.setData("field", "instructions");
        
        // Route
        new Label(formGroup, SWT.NONE).setText("Route:");
        Combo cbRoute = new Combo(formGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbRoute.setItems(new String[] {"Oral", "Injection", "Topical", "Inhalation"});
        cbRoute.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbRoute.select(0);
        cbRoute.setData("field", "route");
        
        // Status
        new Label(formGroup, SWT.NONE).setText("Status:");
        Combo cbStatus = new Combo(formGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbStatus.setItems(new String[] {"Pending", "Completed", "Rejected"});
        cbStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbStatus.select(0);
        cbStatus.setData("field", "status");
        
        // Store form controls for later reference
        formGroup.setData("patientId", txtPatientId);
        formGroup.setData("doctorId", txtDoctorId);
        formGroup.setData("itemId", txtItemId);
        formGroup.setData("medicine", txtMedicine);
        formGroup.setData("dosage", txtDosage);
        formGroup.setData("dosageType", cbDosageType);
        formGroup.setData("frequency", txtFrequency);
        formGroup.setData("duration", txtDuration);
        formGroup.setData("foodRelation", cbFoodRelation);
        formGroup.setData("instructions", txtInstructions);
        formGroup.setData("route", cbRoute);
        formGroup.setData("status", cbStatus);
        
        shell.setData("formGroup", formGroup);
    }
    
    private void createButtonBar(Composite parent) {
        Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        buttonBar.setLayout(new GridLayout(4, true));
        
        Button btnNew = new Button(buttonBar, SWT.PUSH);
        btnNew.setText("New Prescription");
        btnNew.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnNew.setFont(buttonFont);
        btnNew.setBackground(buttonColor);
        btnNew.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openNewPrescriptionDialog();
            }
        });
        
        Button btnUpdateStatus = new Button(buttonBar, SWT.PUSH);
        btnUpdateStatus.setText("Update Status");
        btnUpdateStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnUpdateStatus.setFont(buttonFont);
        btnUpdateStatus.setBackground(buttonColor);
        btnUpdateStatus.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectedPrescriptionStatus();
            }
        });
        
        Button btnDelete = new Button(buttonBar, SWT.PUSH);
        btnDelete.setText("Delete Prescription");
        btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnDelete.setFont(buttonFont);
        btnDelete.setBackground(buttonColor);
        btnDelete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteSelectedPrescription();
            }
        });
        
        Button btnRefresh = new Button(buttonBar, SWT.PUSH);
        btnRefresh.setText("Refresh");
        btnRefresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnRefresh.setFont(buttonFont);
        btnRefresh.setBackground(buttonColor);
        btnRefresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loadPrescriptions();
                clearForm();
            }
        });
    }
    
    private void loadPrescriptions() {
        tblPrescriptions.removeAll();
        try {
            List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
            for (Prescription prescription : prescriptions) {
                TableItem item = new TableItem(tblPrescriptions, SWT.NONE);
                
                // Get patient and doctor names
                String patientName = getPatientName(prescription.getPatientId());
                String doctorName = getDoctorName(prescription.getDoctorId());
                
                item.setText(0, patientName);
                item.setText(1, doctorName);
                item.setText(2, prescription.getMedicine());
                item.setText(3, prescription.getDosage() + " " + prescription.getDosageType());
                item.setText(4, prescription.getStatus());
                item.setText(5, prescription.getPrescriptionDate().toString());
                
                // Store full prescription object
                item.setData(prescription);
            }
        } catch (Exception e) {
            showError("Failed to load prescriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void searchPrescriptions() {
        String searchTerm = searchText.getText().trim();
        if (searchTerm.isEmpty()) {
            loadPrescriptions();
            return;
        }
        
        tblPrescriptions.removeAll();
        try {
            // This is a simple implementation - in a real system you'd have a proper search endpoint
            List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
            for (Prescription prescription : prescriptions) {
                // Simple filter to match patient ID
                if (String.valueOf(prescription.getPatientId()).contains(searchTerm)) {
                    TableItem item = new TableItem(tblPrescriptions, SWT.NONE);
                    item.setText(0, String.valueOf(prescription.getPatientId()));
                    item.setText(1, String.valueOf(prescription.getDoctorId()));
                    item.setText(2, prescription.getMedicine());
                    item.setText(3, prescription.getDosage() + " " + prescription.getDosageType());
                    item.setText(4, prescription.getStatus());
                    item.setText(5, prescription.getPrescriptionDate().toString());
                    item.setData(prescription);
                }
            }
        } catch (Exception e) {
            showError("Error searching prescriptions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showPrescriptionDetails(Prescription prescription) {
        Group formGroup = (Group) shell.getData("formGroup");
        if (formGroup == null || prescription == null) return;
        
        // Set field values from prescription
        setText(formGroup, "patientId", String.valueOf(prescription.getPatientId()));
        setText(formGroup, "doctorId", String.valueOf(prescription.getDoctorId()));
        setText(formGroup, "itemId", prescription.getItemId() != null ? String.valueOf(prescription.getItemId()) : "");
        setText(formGroup, "medicine", prescription.getMedicine());
        setText(formGroup, "dosage", String.valueOf(prescription.getDosage()));
        setComboText(formGroup, "dosageType", prescription.getDosageType());
        setText(formGroup, "frequency", String.valueOf(prescription.getFrequency()));
        setText(formGroup, "duration", String.valueOf(prescription.getDuration()));
        setComboText(formGroup, "foodRelation", prescription.getFoodRelation());
        setText(formGroup, "instructions", prescription.getInstructions());
        setComboText(formGroup, "route", prescription.getRoute());
        setComboText(formGroup, "status", prescription.getStatus());
        
        // Store the current prescription for reference
        formGroup.setData("currentPrescription", prescription);
    }
    
    private void setText(Group group, String fieldName, String value) {
        Control control = (Control) group.getData(fieldName);
        if (control instanceof Text) {
            ((Text) control).setText(value);
        }
    }
    
    private void setComboText(Group group, String fieldName, String value) {
        Control control = (Control) group.getData(fieldName);
        if (control instanceof Combo) {
            Combo combo = (Combo) control;
            String[] items = combo.getItems();
            for (int i = 0; i < items.length; i++) {
                if (items[i].equalsIgnoreCase(value)) {
                    combo.select(i);
                    return;
                }
            }
            combo.select(0);
        }
    }
    
    private void clearForm() {
        Group formGroup = (Group) shell.getData("formGroup");
        if (formGroup == null) return;
        
        setText(formGroup, "patientId", "");
        setText(formGroup, "doctorId", "");
        setText(formGroup, "itemId", "");
        setText(formGroup, "medicine", "");
        setText(formGroup, "dosage", "");
        setText(formGroup, "frequency", "");
        setText(formGroup, "duration", "");
        setText(formGroup, "instructions", "");
        
        setComboText(formGroup, "dosageType", "mg");
        setComboText(formGroup, "foodRelation", "Before Food");
        setComboText(formGroup, "route", "Oral");
        setComboText(formGroup, "status", "Pending");
        
        formGroup.setData("currentPrescription", null);
    }
    
    private void openNewPrescriptionDialog() {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("New Prescription");
        dialog.setLayout(new GridLayout(2, false));
        
        // Patient selection
        new Label(dialog, SWT.NONE).setText("Patient:");
        Combo cbPatient = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbPatient.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Doctor selection
        new Label(dialog, SWT.NONE).setText("Doctor:");
        Combo cbDoctor = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbDoctor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Medication/Item selection
        new Label(dialog, SWT.NONE).setText("Stock Item (Optional):");
        Combo cbMedication = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbMedication.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Load data into dropdown boxes
        try {
            // Load patients
            List<Patient> patients = patientService.getAllPatients();
            for (Patient patient : patients) {
                String displayText = patient.getName() + " (ID: " + patient.getId() + ")";
                cbPatient.add(displayText);
                cbPatient.setData(displayText, patient);
            }
            if (cbPatient.getItemCount() > 0) cbPatient.select(0);
            
            // Load doctors (staff)
            List<Staff> doctors = staffService.getStaffByRole("Doctor");
            for (Staff staff : doctors) {
                String displayText = staff.getName() + " (ID: " + staff.getId() + ")";
                cbDoctor.add(displayText);
                cbDoctor.setData(displayText, staff);
            }
            if (cbDoctor.getItemCount() > 0) cbDoctor.select(0);
            
            // Load medications with an empty option first
            cbMedication.add("-- Select Medication (Optional) --");
            List<Stock> medications = stockService.getAllStocks();
            for (Stock med : medications) {
                String displayText = med.getItemName() + " (ID: " + med.getItemId() + ")";
                cbMedication.add(displayText);
                cbMedication.setData(displayText, med);
            }
            cbMedication.select(0);
            
        } catch (Exception e) {
            showError("Error loading dropdown data: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Medicine name field (can be used independently of stock selection)
        new Label(dialog, SWT.NONE).setText("Medicine Name:");
        Text txtMedicine = new Text(dialog, SWT.BORDER);
        txtMedicine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Add listener to medication dropdown to auto-fill medicine name when selected
        cbMedication.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = cbMedication.getSelectionIndex();
                if (index > 0) { // Skip the first "--Select--" option
                    String selected = cbMedication.getItem(index);
                    Stock stock = (Stock) cbMedication.getData(selected);
                    if (stock != null) {
                        txtMedicine.setText(stock.getItemName());
                    }
                }
            }
        });
        
        // Dosage
        new Label(dialog, SWT.NONE).setText("Dosage:");
        Text txtDosage = new Text(dialog, SWT.BORDER);
        txtDosage.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Dosage Type
        new Label(dialog, SWT.NONE).setText("Dosage Type:");
        Combo cbDosageType = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbDosageType.setItems(new String[] {"mg", "ml", "tablet", "capsule", "puff"});
        cbDosageType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbDosageType.select(0);
        
        // Frequency
        new Label(dialog, SWT.NONE).setText("Frequency (per day):");
        Text txtFrequency = new Text(dialog, SWT.BORDER);
        txtFrequency.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Duration
        new Label(dialog, SWT.NONE).setText("Duration (days):");
        Text txtDuration = new Text(dialog, SWT.BORDER);
        txtDuration.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Food Relation
        new Label(dialog, SWT.NONE).setText("Food Relation:");
        Combo cbFoodRelation = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbFoodRelation.setItems(new String[] {"Before Food", "After Food", "With Food"});
        cbFoodRelation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbFoodRelation.select(0);
        
        // Instructions
        new Label(dialog, SWT.NONE).setText("Instructions:");
        Text txtInstructions = new Text(dialog, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData gdInstructions = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdInstructions.heightHint = 60;
        txtInstructions.setLayoutData(gdInstructions);
        
        // Route
        new Label(dialog, SWT.NONE).setText("Route:");
        Combo cbRoute = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbRoute.setItems(new String[] {"Oral", "Injection", "Topical", "Inhalation"});
        cbRoute.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbRoute.select(0);
        
        // Status
        new Label(dialog, SWT.NONE).setText("Status:");
        Combo cbStatus = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbStatus.setItems(new String[] {"Pending", "Completed", "Rejected"});
        cbStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cbStatus.select(0);
        
        // Buttons
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        GridData gdButtons = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        gdButtons.horizontalSpan = 2;
        buttonArea.setLayoutData(gdButtons);
        
        Button saveButton = new Button(buttonArea, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        saveButton.setFont(buttonFont);
        saveButton.setBackground(buttonColor);
        
        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.setFont(buttonFont);
        cancelButton.setBackground(buttonColor);
        
        // Save action
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    // Get selected patient
                    int patientIndex = cbPatient.getSelectionIndex();
                    if (patientIndex < 0) {
                        showError("Please select a patient");
                        return;
                    }
                    String patientText = cbPatient.getItem(patientIndex);
                    Patient patient = (Patient) cbPatient.getData(patientText);
                    if (patient == null) {
                        showError("Invalid patient selection");
                        return;
                    }
                    int patientId = patient.getId();
                    
                    // Get selected doctor
                    int doctorIndex = cbDoctor.getSelectionIndex();
                    if (doctorIndex < 0) {
                        showError("Please select a doctor");
                        return;
                    }
                    String doctorText = cbDoctor.getItem(doctorIndex);
                    Staff doctor = (Staff) cbDoctor.getData(doctorText);
                    if (doctor == null) {
                        showError("Invalid doctor selection");
                        return;
                    }
                    int doctorId = doctor.getId();
                    
                    // Medicine name
                    String medicine = txtMedicine.getText().trim();
                    if (medicine.isEmpty()) {
                        showError("Medicine name is required");
                        return;
                    }
                    
                    // Create prescription object
                    Prescription prescription = new Prescription();
                    prescription.setPatientId(patientId);
                    prescription.setDoctorId(doctorId);
                    
                    // Optional medication/item
                    int medicationIndex = cbMedication.getSelectionIndex();
                    if (medicationIndex > 0) { // Skip the first "--Select--" option
                        String medText = cbMedication.getItem(medicationIndex);
                        Stock med = (Stock) cbMedication.getData(medText);
                        if (med != null) {
                            prescription.setItemId(med.getItemId());
                        }
                    }
                    
                    // Set other fields
                    prescription.setMedicine(medicine);
                    
                    try {
                        double dosage = Double.parseDouble(txtDosage.getText().trim());
                        if (dosage <= 0) {
                            showError("Dosage must be greater than zero");
                            return;
                        }
                        prescription.setDosage(dosage);
                    } catch (NumberFormatException ex) {
                        showError("Dosage must be a valid number");
                        return;
                    }
                    
                    prescription.setDosageType(cbDosageType.getText());
                    
                    try {
                        int frequency = Integer.parseInt(txtFrequency.getText().trim());
                        if (frequency <= 0) {
                            showError("Frequency must be greater than zero");
                            return;
                        }
                        prescription.setFrequency(frequency);
                    } catch (NumberFormatException ex) {
                        showError("Frequency must be a valid number");
                        return;
                    }
                    
                    try {
                        int duration = Integer.parseInt(txtDuration.getText().trim());
                        if (duration <= 0) {
                            showError("Duration must be greater than zero");
                            return;
                        }
                        prescription.setDuration(duration);
                    } catch (NumberFormatException ex) {
                        showError("Duration must be a valid number");
                        return;
                    }
                    
                    prescription.setFoodRelation(cbFoodRelation.getText());
                    prescription.setInstructions(txtInstructions.getText());
                    prescription.setRoute(cbRoute.getText());
                    prescription.setStatus(cbStatus.getText());
                    prescription.setPrescriptionDate(new Timestamp(new Date().getTime()));
                    
                    // Save prescription
                    int result = prescriptionService.createPrescription(prescription);
                    
                    if (result > 0) {
                        showInfo("Success", "Prescription created successfully!");
                        dialog.dispose();
                        loadPrescriptions();
                    } else {
                        showError("Failed to create prescription");
                    }
                    
                } catch (Exception ex) {
                    showError("Error creating prescription: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
        // Cancel action
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialog.dispose();
            }
        });
        
        dialog.pack();
        dialog.setSize(500, dialog.getSize().y);
        centerDialog(dialog);
        dialog.open();
    }
    
    private void loadDropdownData(Group formGroup) {
        try {
            // Load patients
            Combo cbPatient = (Combo) formGroup.getData("patientCombo");
            List<Patient> patients = patientService.getAllPatients();
            cbPatient.removeAll();
            
            for (Patient patient : patients) {
                cbPatient.add(patient.getName() + " (ID: " + patient.getId() + ")");
                cbPatient.setData(String.valueOf(patient.getId()), patient);
            }
            
            // Load doctors
            Combo cbDoctor = (Combo) formGroup.getData("doctorCombo");
            List<Staff> doctors = staffService.getAllStaff();
            cbDoctor.removeAll();
            
            for (Staff doctor : doctors) {
                cbDoctor.add(doctor.getName() + " (ID: " + doctor.getId() + ")");
                cbDoctor.setData(String.valueOf(doctor.getId()), doctor);
            }
            
            // Load medications
            Combo cbMedication = (Combo) formGroup.getData("medicationCombo");
            List<Stock> medications = stockService.getAllStocks();
            cbMedication.removeAll();
            
            // Add an empty option for optional medication
            cbMedication.add("-- Select Medication (Optional) --");
            
            for (Stock medication : medications) {
                cbMedication.add(medication.getItemName() + " (ID: " + medication.getItemId() + ")");
                cbMedication.setData(String.valueOf(medication.getItemId()), medication);
            }
            
            // Select the first item in all combos
            if (cbPatient.getItemCount() > 0) cbPatient.select(0);
            if (cbDoctor.getItemCount() > 0) cbDoctor.select(0);
            if (cbMedication.getItemCount() > 0) cbMedication.select(0);
            
        } catch (Exception e) {
            showError("Failed to load dropdown data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateSelectedPrescriptionStatus() {
        TableItem[] selectedItems = tblPrescriptions.getSelection();
        if (selectedItems.length == 0) {
            showError("Please select a prescription to update");
            return;
        }
        
        Prescription prescription = (Prescription) selectedItems[0].getData();
        
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Update Prescription Status");
        dialog.setLayout(new GridLayout(2, false));
        
        // Status selection
        new Label(dialog, SWT.NONE).setText("New Status:");
        Combo cbStatus = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        cbStatus.setItems(new String[] {"Pending", "Completed", "Rejected"});
        cbStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Set current status
        for (int i = 0; i < cbStatus.getItems().length; i++) {
            if (cbStatus.getItem(i).equals(prescription.getStatus())) {
                cbStatus.select(i);
                break;
            }
        }
        
        // Buttons
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        GridData gdButtons = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        gdButtons.horizontalSpan = 2;
        buttonArea.setLayoutData(gdButtons);
        
        Button updateButton = new Button(buttonArea, SWT.PUSH);
        updateButton.setText("Update");
        updateButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        updateButton.setFont(buttonFont);
        updateButton.setBackground(buttonColor);
        
        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        cancelButton.setFont(buttonFont);
        cancelButton.setBackground(buttonColor);
        
        // Update action
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String newStatus = cbStatus.getText();
                
                if (newStatus.equals(prescription.getStatus())) {
                    showInfo("Info", "Status unchanged");
                    dialog.dispose();
                    return;
                }
                
                try {
                    boolean updated = false;
                    
                    // Use specialized method if available
                    if (prescriptionService instanceof com.prescriptionservicepublisher.PrescriptionServiceImplementation) {
                        com.prescriptionservicepublisher.PrescriptionServiceImplementation serviceImpl = 
                            (com.prescriptionservicepublisher.PrescriptionServiceImplementation) prescriptionService;
                        
                        updated = serviceImpl.updatePrescriptionStatus(
                            prescription.getPatientId(), 
                            prescription.getDoctorId(), 
                            prescription.getPrescriptionDate(), 
                            newStatus
                        );
                    } else {
                        // Fallback to regular update
                        prescription.setStatus(newStatus);
                        updated = prescriptionService.updatePrescription(prescription);
                    }
                    
                    if (updated) {
                        showInfo("Success", "Prescription status updated successfully!");
                        dialog.dispose();
                        loadPrescriptions();
                    } else {
                        showError("Failed to update prescription status");
                    }
                } catch (Exception ex) {
                    showError("Error updating prescription: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
        
        cancelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                dialog.dispose();
            }
        });
        
        dialog.pack();
        dialog.setSize(300, dialog.getSize().y);
        centerDialog(dialog);
        dialog.open();
    }
    
    private void deleteSelectedPrescription() {
        TableItem[] selectedItems = tblPrescriptions.getSelection();
        if (selectedItems.length == 0) {
            showError("Please select a prescription to delete");
            return;
        }
        
        Prescription prescription = (Prescription) selectedItems[0].getData();
        
        MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmBox.setText("Confirm Delete");
        confirmBox.setMessage("Are you sure you want to delete this prescription for patient " 
            + prescription.getPatientId() + "?");
        
        if (confirmBox.open() == SWT.YES) {
            try {
                boolean deleted = prescriptionService.deletePrescription(prescription.getId());
                
                if (deleted) {
                    showInfo("Success", "Prescription deleted successfully");
                    clearForm();
                    loadPrescriptions();
                } else {
                    showError("Failed to delete prescription");
                }
            } catch (Exception e) {
                showError("Error deleting prescription: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void centerDialog(Shell dialog) {
        Rectangle parentBounds = shell.getBounds();
        Rectangle dialogBounds = dialog.getBounds();
        int x = parentBounds.x + (parentBounds.width - dialogBounds.width) / 2;
        int y = parentBounds.y + (parentBounds.height - dialogBounds.height) / 2;
        dialog.setLocation(x, y);
    }
    
    private void showError(String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
        messageBox.setText("Error");
        messageBox.setMessage(message);
        messageBox.open();
    }
    
    private void showInfo(String title, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(title);
        messageBox.setMessage(message);
        messageBox.open();
    }
    
 // Helper methods to get names
    private String getPatientName(int patientId) {
        try {
            Patient patient = patientService.getPatientById(patientId);
            if (patient != null) {
                return patient.getName();
            }
        } catch (Exception e) {
        	System.out.print(e.getMessage());
        }
        return String.valueOf(patientId);
    }

    private String getDoctorName(int doctorId) {
        try {
            Staff doctor = staffService.getStaffById(doctorId);
            if (doctor != null) {
                return doctor.getName();
            }
        } catch (Exception e) {
        	System.out.print(e.getMessage());
        }
        return String.valueOf(doctorId);
    }
    
    private String getStockName(int itemId) {
        try {
             Stock item = stockService.getStockById(itemId);
            if (item != null) {
                return item.getItemName();
            }
        } catch (Exception e) {
        	System.out.print(e.getMessage());
        }
        return String.valueOf(itemId);
    }
}