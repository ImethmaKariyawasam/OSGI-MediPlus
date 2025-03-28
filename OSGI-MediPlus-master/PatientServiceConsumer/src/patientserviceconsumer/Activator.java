package patientserviceconsumer;

import com.patientPublisherService.IPatientService;
import com.patientPublisherService.Patient;
import com.vitalServicePublisher.IVitalsService;
import com.vitalServicePublisher.Vitals;
import com.diagnosisServicePublisher.IDiagnosisService;
import com.diagnosisServicePublisher.Diagnosis;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.MessageBox;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class Activator implements BundleActivator {
    private ServiceReference<?> patientServiceRef;
    private ServiceReference<?> vitalsServiceRef;
    private ServiceReference<?> diagnosisServiceRef;
    private Display display;
    private Shell shell;
    private IPatientService patientService;
    private IVitalsService vitalsService;
    private IDiagnosisService diagnosisService;
    private Table patientTable;
    private Font titleFont;
    private Font buttonFont;
    private Color headerColor;
    private Color buttonColor;
    
    @Override
    public void start(BundleContext context) throws Exception {
        // Get both services
        patientServiceRef = context.getServiceReference(IPatientService.class.getName());
        vitalsServiceRef = context.getServiceReference(IVitalsService.class.getName());
        diagnosisServiceRef = context.getServiceReference(IDiagnosisService.class.getName());
        
        patientService = (IPatientService) context.getService(patientServiceRef);
        vitalsService = (IVitalsService) context.getService(vitalsServiceRef);
        diagnosisService=(IDiagnosisService)context.getService(diagnosisServiceRef);

        // Create UI in a separate thread
        Thread uiThread = new Thread(() -> createAndShowGUI());
        uiThread.start();
    }
    
    private void createAndShowGUI() {
        display = new Display();
        shell = new Shell(display);
        shell.setText("Hospital Management System");
        shell.setSize(900, 600);
        shell.setLayout(new FillLayout());
        
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
        
        // Create Patient Management Tab
        CTabItem patientTab = new CTabItem(tabFolder, SWT.NONE);
        patientTab.setText("Patient Management");
        Composite patientComposite = createPatientTabContent(tabFolder);
        patientTab.setControl(patientComposite);
        
        // Create Vitals Management Tab
        CTabItem vitalsTab = new CTabItem(tabFolder, SWT.NONE);
        vitalsTab.setText("Vitals Management");
        Composite vitalsComposite = createVitalsTabContent(tabFolder);
        vitalsTab.setControl(vitalsComposite);
        
        // Create Diagnosis Management Tab
        CTabItem diagnosisTab = new CTabItem(tabFolder, SWT.NONE);
        diagnosisTab.setText("Diagnosis Management");
        Composite diagnosisComposite = createDiagnosisTabContent(tabFolder);
        diagnosisTab.setControl(diagnosisComposite);
        
        
        // Set the active tab
        tabFolder.setSelection(0);
        
        // Center the shell
        shell.open();
        
        // Start the event loop
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
    
    private Composite createPatientTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        
        // Header label
        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Patient Management System");
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
        
        Button addPatientButton = new Button(topControls, SWT.PUSH);
        addPatientButton.setText("Add New Patient");
        addPatientButton.setFont(buttonFont);
        addPatientButton.setBackground(buttonColor);
        
        // Create table to display patients
        patientTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        patientTable.setHeaderVisible(true);
        patientTable.setLinesVisible(true);
        patientTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        String[] titles = {"ID", "Name", "Age", "Gender", "Contact", "Medical History"};
        for (String title : titles) {
            TableColumn column = new TableColumn(patientTable, SWT.NONE);
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
        
        // Add event handlers
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String searchTerm = searchText.getText().trim();
                searchPatients(searchTerm);
            }
        });
        
        searchText.addTraverseListener(e -> {
            if (e.detail == SWT.TRAVERSE_RETURN) {
                searchPatients(searchText.getText().trim());
            }
        });
        
        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshPatientList();
            }
        });
        
        addPatientButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openAddPatientDialog();
            }
        });
        
        viewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewSelectedPatient();
            }
        });
        
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectedPatient();
            }
        });
        
        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteSelectedPatient();
            }
        });
        
        // Initial population of the table
        refreshPatientList();
        
        return composite;
    }
    
    private Composite createVitalsTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        
        // Header label
        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Vitals Management System");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        GridData headerData = new GridData(SWT.FILL, SWT.TOP, true, false);
        headerData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerData);
        
        // Create patient selector section
        Group patientSelector = new Group(composite, SWT.NONE);
        patientSelector.setText("Patient Selection");
        patientSelector.setLayout(new GridLayout(3, false));
        patientSelector.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Label patientIdLabel = new Label(patientSelector, SWT.NONE);
        patientIdLabel.setText("Patient ID:");
        
        Text patientIdText = new Text(patientSelector, SWT.BORDER);
        patientIdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button getVitalsButton = new Button(patientSelector, SWT.PUSH);
        getVitalsButton.setText("Get Vitals");
        getVitalsButton.setFont(buttonFont);
        getVitalsButton.setBackground(buttonColor);
        
        // Create vitals display section
        Group vitalsDisplay = new Group(composite, SWT.NONE);
        vitalsDisplay.setText("Vitals Information");
        vitalsDisplay.setLayout(new GridLayout(2, false));
        vitalsDisplay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Temperature
        Label tempLabel = new Label(vitalsDisplay, SWT.NONE);
        tempLabel.setText("Temperature (°C):");
        
        Text tempText = new Text(vitalsDisplay, SWT.BORDER);
        tempText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Heart Rate
        Label hrLabel = new Label(vitalsDisplay, SWT.NONE);
        hrLabel.setText("Heart Rate (bpm):");
        
        Text hrText = new Text(vitalsDisplay, SWT.BORDER);
        hrText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Systolic BP
        Label sysLabel = new Label(vitalsDisplay, SWT.NONE);
        sysLabel.setText("Systolic BP (mmHg):");
        
        Text sysText = new Text(vitalsDisplay, SWT.BORDER);
        sysText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Diastolic BP
        Label diaLabel = new Label(vitalsDisplay, SWT.NONE);
        diaLabel.setText("Diastolic BP (mmHg):");
        
        Text diaText = new Text(vitalsDisplay, SWT.BORDER);
        diaText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Respiratory Rate
        Label rrLabel = new Label(vitalsDisplay, SWT.NONE);
        rrLabel.setText("Respiratory Rate (breaths/min):");
        
        Text rrText = new Text(vitalsDisplay, SWT.BORDER);
        rrText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Action buttons
        Composite buttonComposite = new Composite(composite, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(3, true));
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        Button recordButton = new Button(buttonComposite, SWT.PUSH);
        recordButton.setText("Record Vitals");
        recordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        recordButton.setFont(buttonFont);
        recordButton.setBackground(buttonColor);
        
        Button updateVitalsButton = new Button(buttonComposite, SWT.PUSH);
        updateVitalsButton.setText("Update Vitals");
        updateVitalsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        updateVitalsButton.setFont(buttonFont);
        updateVitalsButton.setBackground(buttonColor);
        
        Button deleteVitalsButton = new Button(buttonComposite, SWT.PUSH);
        deleteVitalsButton.setText("Delete Vitals");
        deleteVitalsButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        deleteVitalsButton.setFont(buttonFont);
        deleteVitalsButton.setBackground(buttonColor);
        
        // Add event handlers
        getVitalsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    Vitals vitals = vitalsService.getVitals(patientId);
                    
                    if (vitals != null) {
                        tempText.setText(String.valueOf(vitals.getTemperature()));
                        hrText.setText(String.valueOf(vitals.getHeartRate()));
                        sysText.setText(String.valueOf(vitals.getBloodPressureSystolic()));
                        diaText.setText(String.valueOf(vitals.getBloodPressureDiastolic()));
                        rrText.setText(String.valueOf(vitals.getRespiratoryRate()));
                        
                        // Enable update and delete buttons
                        updateVitalsButton.setEnabled(true);
                        deleteVitalsButton.setEnabled(true);
                    } else {
                        showMessage("Vitals Not Found", "No vitals found for patient ID: " + patientId);
                        clearVitalsFields(tempText, hrText, sysText, diaText, rrText);
                        
                        // Disable update and delete buttons
                        updateVitalsButton.setEnabled(false);
                        deleteVitalsButton.setEnabled(false);
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please enter a valid patient ID");
                }
            }
        });
        
        recordButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    
                    boolean patient_found = patientService.getPatient(patientId);
                    
                    if(patient_found==false) {
                    	showMessage("Error", "No such patient is found in the database");
                    }
                    else {
                        if (validateVitalsFields(tempText, hrText, sysText, diaText, rrText)) {
                            Vitals vitals = createVitalsFromFields(tempText, hrText, sysText, diaText, rrText);
                            boolean success = vitalsService.recordVitals(patientId, vitals);
                            
                            if (success) {
                                showMessage("Success", "Vitals recorded successfully!");
                                updateVitalsButton.setEnabled(true);
                                deleteVitalsButton.setEnabled(true);
                            } else {
                                showMessage("Error", "Failed to record vitals");
                            }
                        }
                    }    
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please ensure all fields contain valid numbers");
                }
            }
        });
        
        updateVitalsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    
                    if (validateVitalsFields(tempText, hrText, sysText, diaText, rrText)) {
                        Vitals vitals = createVitalsFromFields(tempText, hrText, sysText, diaText, rrText);
                        boolean success = vitalsService.updateVitals(patientId, vitals);
                        
                        if (success) {
                            showMessage("Success", "Vitals updated successfully!");
                        } else {
                            showMessage("Error", "Failed to update vitals");
                        }
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please ensure all fields contain valid numbers");
                }
            }
        });
        
        deleteVitalsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    
                    MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                    confirmBox.setText("Confirm Delete");
                    confirmBox.setMessage("Are you sure you want to delete vitals for patient ID: " + patientId + "?");
                    
                    if (confirmBox.open() == SWT.YES) {
                        boolean success = vitalsService.deleteVitals(patientId);
                        
                        if (success) {
                            showMessage("Success", "Vitals deleted successfully!");
                            clearVitalsFields(tempText, hrText, sysText, diaText, rrText);
                            updateVitalsButton.setEnabled(false);
                            deleteVitalsButton.setEnabled(false);
                        } else {
                            showMessage("Error", "Failed to delete vitals");
                        }
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please enter a valid patient ID");
                }
            }
        });
        
        // Initially disable update and delete buttons
        updateVitalsButton.setEnabled(false);
        deleteVitalsButton.setEnabled(false);
        
        return composite;
    }
    
    private Composite createDiagnosisTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        
        // Header label
        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Diagnosis Management System");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        GridData headerData = new GridData(SWT.FILL, SWT.TOP, true, false);
        headerData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerData);
        
        // Create patient selector section
        Group patientSelector = new Group(composite, SWT.NONE);
        patientSelector.setText("Patient Selection");
        patientSelector.setLayout(new GridLayout(2, false));
        patientSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label patientIdLabel = new Label(patientSelector, SWT.NONE);
        patientIdLabel.setText("Patient ID:");
        
        Text patientIdText = new Text(patientSelector, SWT.BORDER);
        patientIdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label doctorLabel = new Label(patientSelector, SWT.NONE);
        doctorLabel.setText("Doctor Name:");
        
        Text doctorNameText = new Text(patientSelector, SWT.BORDER);
        doctorNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Button getDiagnosisButton = new Button(patientSelector, SWT.PUSH);
        getDiagnosisButton.setText("Get Diagnosis");
        getDiagnosisButton.setFont(buttonFont);
        getDiagnosisButton.setBackground(buttonColor);
        
        Button clearAllButton = new Button(patientSelector, SWT.PUSH);
        clearAllButton.setText("Clear All Fields");
        clearAllButton.setFont(buttonFont);
        clearAllButton.setBackground(buttonColor);
        
        // Create Diagnosis display section
        Group diagnosisDisplay = new Group(composite, SWT.NONE);
        diagnosisDisplay.setText("Vitals Information");
        diagnosisDisplay.setLayout(new GridLayout(2, false));
        diagnosisDisplay.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // Doctor Name
        Label doctorName = new Label(diagnosisDisplay, SWT.NONE);
        doctorName.setText("Doctor Name:");
        
        Text doctorText = new Text(diagnosisDisplay, SWT.BORDER);
        doctorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Diagnosis
        Label diagnosisLabel = new Label(diagnosisDisplay, SWT.NONE);
        diagnosisLabel.setText("Diagnosis:");
        
        Text diagnosisText = new Text(diagnosisDisplay, SWT.BORDER);
        diagnosisText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Type 
        Label typeLabel = new Label(diagnosisDisplay, SWT.NONE);
        typeLabel.setText("Diagnosis Type:");

        Composite radioGroup = new Composite(diagnosisDisplay, SWT.NONE);
        radioGroup.setLayout(new GridLayout(2, false)); // Two columns for radio buttons

        Button provincialRadio = new Button(radioGroup, SWT.RADIO);
        provincialRadio.setText("Provincial");

        Button principalRadio = new Button(radioGroup, SWT.RADIO);
        principalRadio.setText("Principal");
        
        // ICD10
        Label ICDLabel = new Label(diagnosisDisplay, SWT.NONE);
        ICDLabel.setText("ICD10 Code:");
        
        Text ICDText = new Text(diagnosisDisplay, SWT.BORDER);
        ICDText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        //Symptoms
        Label symptomLabel = new Label(diagnosisDisplay, SWT.NONE);
        symptomLabel.setText("Symptoms:");

        // Multi-line Text Area
        Text symptomText = new Text(diagnosisDisplay, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.heightHint = 60; // Adjust height for multi-line input
        symptomText.setLayoutData(gridData);
        
        // level 
        Label levelLabel = new Label(diagnosisDisplay, SWT.NONE);
        levelLabel.setText("Level:");

        Composite radioGroup1 = new Composite(diagnosisDisplay, SWT.NONE);
        radioGroup1.setLayout(new GridLayout(3, false)); // Three columns for radio buttons

        Button high = new Button(radioGroup1, SWT.RADIO);
        high.setText("High");
        
        Button medium = new Button(radioGroup1, SWT.RADIO);
        medium.setText("Medium"); // Corrected capitalization from "medium" to "Medium"

        Button low = new Button(radioGroup1, SWT.RADIO);
        low.setText("Low"); // Corrected capitalization from "low" to "Low"
        
        // Diagnosis Date
        Label dateLabel = new Label(diagnosisDisplay, SWT.NONE);
        dateLabel.setText("Diagnosis Date:");

        DateTime diagnosisDate = new DateTime(diagnosisDisplay, SWT.BORDER | SWT.DROP_DOWN);
        GridData dateGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        diagnosisDate.setLayoutData(dateGridData);
        
        // Action buttons
        Composite buttonComposite = new Composite(composite, SWT.NONE);
        buttonComposite.setLayout(new GridLayout(3, true));
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        
        Button recordButton = new Button(buttonComposite, SWT.PUSH);
        recordButton.setText("Record Diagnosis");
        recordButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        recordButton.setFont(buttonFont);
        recordButton.setBackground(buttonColor);
        
        Button updateDiagnosisButton = new Button(buttonComposite, SWT.PUSH);
        updateDiagnosisButton.setText("Update Diagnosis");
        updateDiagnosisButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        updateDiagnosisButton.setFont(buttonFont);
        updateDiagnosisButton.setBackground(buttonColor);
        
        Button deleteDiagnosisButton = new Button(buttonComposite, SWT.PUSH);
        deleteDiagnosisButton.setText("Delete Diagnosis");
        deleteDiagnosisButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        deleteDiagnosisButton.setFont(buttonFont);
        deleteDiagnosisButton.setBackground(buttonColor);
        
        // Add event handlers
        getDiagnosisButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    Diagnosis diagnosis = null;
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    boolean patientExists = patientService.getPatient(patientId);
                    String doctorName = doctorNameText.getText().trim();                    
                    if(patientId < 0 || doctorName.isEmpty() || patientExists == false) {
                        showMessage("Invalid Input", "Please enter a valid patient ID and Doctor Name Combination");
                    } else {
                        diagnosis = diagnosisService.getDiagnosis(patientId, doctorName);
                    }
                    
                    if (diagnosis != null) {
                        // Populate the diagnosis details in respective fields
                        doctorText.setText(diagnosis.getDoctorName());
                        diagnosisText.setText(diagnosis.getDiagnosis());
                        ICDText.setText(diagnosis.getIcd10Code());
                        symptomText.setText(diagnosis.getSymptoms());

                        // Set selected radio buttons based on diagnosis type
                        if (diagnosis.getType().equals("Provincial")) {
                            provincialRadio.setSelection(true);
                            principalRadio.setSelection(false);
                        } else {
                            principalRadio.setSelection(true);
                            provincialRadio.setSelection(false);
                        }

                        // Set the diagnosis level radio button
                        if (diagnosis.getSeverityLevel().equals("High")) {
                            high.setSelection(true);
                            medium.setSelection(false);
                            low.setSelection(false);
                        } else if (diagnosis.getSeverityLevel().equals("Medium")) {
                            medium.setSelection(true);
                            high.setSelection(false);
                            low.setSelection(false);
                        } else {
                            low.setSelection(true);
                            high.setSelection(false);
                            medium.setSelection(false);
                        }
                        
                        // Set the diagnosis date
                        if (diagnosis.getDiagnosisDate() != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(diagnosis.getDiagnosisDate());
                            diagnosisDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                        }

                        // Enable update and delete buttons
                        updateDiagnosisButton.setEnabled(true);
                        deleteDiagnosisButton.setEnabled(true);
                    } else {
                        showMessage("Diagnosis Not Found", "No Diagnosis found for patient ID: " + patientId);
                        clearAllFields(patientIdText, doctorNameText, doctorText, diagnosisText, ICDText, symptomText, 
                                      provincialRadio, principalRadio, high, medium, low, diagnosisDate, false);
                        
                        // Disable update and delete buttons
                        updateDiagnosisButton.setEnabled(false);
                        deleteDiagnosisButton.setEnabled(false);
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please enter a valid patient ID");
                }
            }
        });
        
        clearAllButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearAllFields(patientIdText, doctorNameText, doctorText, diagnosisText, ICDText, symptomText, 
                             provincialRadio, principalRadio, high, medium, low, diagnosisDate, true);
                updateDiagnosisButton.setEnabled(false);
                deleteDiagnosisButton.setEnabled(false);
            }
        });
        
        recordButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    boolean patientExists = patientService.getPatient(patientId);
                    
                    if(patientExists == false) {
                        showMessage("Invalid Input", "Patient does not exist in the database");
                    } else {
                        if (validateDiagnosisFields(doctorText, diagnosisText, ICDText, symptomText)) {
                            Diagnosis diagnosis = createDiagnosisFromFields(doctorText, diagnosisText, ICDText, symptomText, 
                                                provincialRadio, principalRadio, high, medium, low, diagnosisDate);
                            boolean success = diagnosisService.recordDiagnosis(patientId, diagnosis);

                            if (success) {
                                showMessage("Success", "Diagnosis recorded successfully!");
                                
                                // Sync doctor name with selection field
                                doctorNameText.setText(doctorText.getText().trim());
                                
                                // Retrieve the newly recorded diagnosis to update all fields
                                Diagnosis updatedDiagnosis = diagnosisService.getDiagnosis(patientId, doctorText.getText().trim());
                                if (updatedDiagnosis != null) {
                                    populateDiagnosisFields(updatedDiagnosis, doctorText, diagnosisText, ICDText, symptomText,
                                                          provincialRadio, principalRadio, high, medium, low, diagnosisDate);
                                }
                                
                                updateDiagnosisButton.setEnabled(true);
                                deleteDiagnosisButton.setEnabled(true);
                            } else {
                                showMessage("Error", "Failed to record diagnosis");
                            }
                        }   
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please ensure all fields contain valid information");
                }
            }
        });
        
        updateDiagnosisButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    String doctorName = doctorNameText.getText().trim(); 
                    if (validateDiagnosisFields(doctorText, diagnosisText, ICDText, symptomText)) {
                        Diagnosis diagnosis = createDiagnosisFromFields(doctorText, diagnosisText, ICDText, symptomText, 
                                            provincialRadio, principalRadio, high, medium, low, diagnosisDate);
                        boolean success = diagnosisService.updateDiagnosis(patientId, doctorName, diagnosis);

                        if (success) {
                            showMessage("Success", "Diagnosis updated successfully!");
                            
                            // Sync doctor name with selection field if it changed
                            doctorNameText.setText(doctorText.getText().trim());
                            
                            // Retrieve the updated diagnosis to refresh all fields
                            Diagnosis updatedDiagnosis = diagnosisService.getDiagnosis(patientId, doctorText.getText().trim());
                            if (updatedDiagnosis != null) {
                                populateDiagnosisFields(updatedDiagnosis, doctorText, diagnosisText, ICDText, symptomText,
                                                      provincialRadio, principalRadio, high, medium, low, diagnosisDate);
                            }
                        } else {
                            showMessage("Error", "Failed to update diagnosis");
                        }
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please ensure all fields contain valid information");
                }
            }
        });
        
        deleteDiagnosisButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    boolean success = false;
                    int patientId = Integer.parseInt(patientIdText.getText().trim());
                    String doctorName = doctorNameText.getText().trim();
                    if(patientId < 0 || doctorName.isEmpty()) {
                        showMessage("Invalid Input", "Please enter a valid patient ID and Doctor Name Combination");
                    } else {
                        success = diagnosisService.deleteDiagnosis(patientId, doctorName);    
                    }
                    if (success) {
                        showMessage("Success", "Diagnosis deleted successfully!");
                        clearAllFields(patientIdText, doctorNameText, doctorText, diagnosisText, ICDText, symptomText, 
                                     provincialRadio, principalRadio, high, medium, low, diagnosisDate, false);

                        // Disable update and delete buttons
                        updateDiagnosisButton.setEnabled(false);
                        deleteDiagnosisButton.setEnabled(false);
                    } else {
                        showMessage("Error", "Failed to delete diagnosis");
                    }
                } catch (NumberFormatException ex) {
                    showMessage("Invalid Input", "Please enter a valid patient ID");
                }
            }
        });
        
        // Initially disable update and delete buttons
        updateDiagnosisButton.setEnabled(false);
        deleteDiagnosisButton.setEnabled(false);
        
        // Set default radio button selections
        provincialRadio.setSelection(true);
        medium.setSelection(true);
        
        return composite;
    }

    // Helper methods to validate and create diagnosis
    private boolean validateDiagnosisFields(Text doctorText, Text diagnosisText, Text ICDText, Text symptomText) {
        if (diagnosisText.getText().trim().isEmpty() || ICDText.getText().trim().isEmpty() || 
            symptomText.getText().trim().isEmpty() || doctorText.getText().trim().isEmpty()) {
            showMessage("Validation Error", "Please fill in all diagnosis fields");
            return false;
        }
        return true;
    }

    private Diagnosis createDiagnosisFromFields(Text doctorText, Text diagnosisText, Text ICDText, Text symptomText, 
                                               Button provincialRadio, Button principalRadio, Button high, Button medium, 
                                               Button low, DateTime diagnosisDate) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setDoctorName(doctorText.getText().trim());
        diagnosis.setDiagnosis(diagnosisText.getText().trim());
        diagnosis.setIcd10Code(ICDText.getText().trim());
        diagnosis.setSymptoms(symptomText.getText().trim());

        // Set diagnosis type
        if (provincialRadio.getSelection()) {
            diagnosis.setType("Provincial");
        } else {
            diagnosis.setType("Principal");
        }

        // Set diagnosis level
        if (high.getSelection()) {
            diagnosis.setSeverityLevel("High");
        } else if (medium.getSelection()) {
            diagnosis.setSeverityLevel("Medium");
        } else {
            diagnosis.setSeverityLevel("Low");
        }

        // Set diagnosis date
        // Create a Date object from the DateTime control
        int year = diagnosisDate.getYear();
        int month = diagnosisDate.getMonth();  // Month is 0-based, so no need to add 1
        int day = diagnosisDate.getDay();

        // Create a Date object
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        java.util.Date diagnosisDateObj = calendar.getTime();

        // Set the diagnosis date in the Diagnosis object
        diagnosis.setDiagnosisDate(diagnosisDateObj);
        return diagnosis;
    }

    // Method to populate diagnosis fields from a diagnosis object
    private void populateDiagnosisFields(Diagnosis diagnosis, Text doctorText, Text diagnosisText, Text ICDText, 
                                       Text symptomText, Button provincialRadio, Button principalRadio, Button high, 
                                       Button medium, Button low, DateTime diagnosisDate) {
        if (diagnosis != null) {
            doctorText.setText(diagnosis.getDoctorName());
            diagnosisText.setText(diagnosis.getDiagnosis());
            ICDText.setText(diagnosis.getIcd10Code());
            symptomText.setText(diagnosis.getSymptoms());

            // Set selected radio buttons based on diagnosis type
            if (diagnosis.getType().equals("Provincial")) {
                provincialRadio.setSelection(true);
                principalRadio.setSelection(false);
            } else {
                principalRadio.setSelection(true);
                provincialRadio.setSelection(false);
            }

            // Set the diagnosis level radio button
            if (diagnosis.getSeverityLevel().equals("High")) {
                high.setSelection(true);
                medium.setSelection(false);
                low.setSelection(false);
            } else if (diagnosis.getSeverityLevel().equals("Medium")) {
                medium.setSelection(true);
                high.setSelection(false);
                low.setSelection(false);
            } else {
                low.setSelection(true);
                high.setSelection(false);
                medium.setSelection(false);
            }
            
            // Set the diagnosis date
            if (diagnosis.getDiagnosisDate() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(diagnosis.getDiagnosisDate());
                diagnosisDate.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            }
        }
    }

    // Method to clear all diagnosis fields
    private void clearAllFields(Text patientIdText, Text doctorNameText, Text doctorText, Text diagnosisText, 
                              Text ICDText, Text symptomText, Button provincialRadio, Button principalRadio, 
                              Button high, Button medium, Button low, DateTime diagnosisDate, boolean clearPatientInfo) {
        // Clear patient selection fields if requested
        if (clearPatientInfo) {
            patientIdText.setText("");
            doctorNameText.setText("");
        }
        
        // Clear diagnosis display fields
        doctorText.setText("");
        diagnosisText.setText("");
        ICDText.setText("");
        symptomText.setText("");
        
        // Reset radio buttons to default state
        provincialRadio.setSelection(true);
        principalRadio.setSelection(false);
        
        medium.setSelection(true);
        high.setSelection(false);
        low.setSelection(false);
        
        // Reset date to current date
        Calendar currentDate = Calendar.getInstance();
        diagnosisDate.setDate(
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        );
    }

    // For backward compatibility - redirects to clearAllFields with default options
    private void clearDiagnosisFields(Text doctorText, Text diagnosisText, Text ICDText, Text symptomText) {
        doctorText.setText("");
        diagnosisText.setText("");
        ICDText.setText("");
        symptomText.setText("");
    }
    
    private void refreshPatientList() {
        patientTable.removeAll();
        
        // Get all patients (we'll use search with empty string to get all)
        List<Patient> patients = patientService.searchPatients("");
        
        for (Patient patient : patients) {
            TableItem item = new TableItem(patientTable, SWT.NONE);
            item.setText(0, String.valueOf(patient.getId()));
            item.setText(1, patient.getName());
            item.setText(2, String.valueOf(patient.getAge()));
            item.setText(3, patient.getGender());
            item.setText(4, patient.getContact());
            item.setText(5, patient.getMedicalHistory());
        }
        
        // Adjust column widths
        for (TableColumn column : patientTable.getColumns()) {
            column.pack();
        }
    }
    
    private void searchPatients(String searchTerm) {
        patientTable.removeAll();
        
        List<Patient> patients = patientService.searchPatients(searchTerm);
        
        for (Patient patient : patients) {
            TableItem item = new TableItem(patientTable, SWT.NONE);
            item.setText(0, String.valueOf(patient.getId()));
            item.setText(1, patient.getName());
            item.setText(2, String.valueOf(patient.getAge()));
            item.setText(3, patient.getGender());
            item.setText(4, patient.getContact());
            item.setText(5, patient.getMedicalHistory());
        }
        
        // Adjust column widths
        for (TableColumn column : patientTable.getColumns()) {
            column.pack();
        }
    }
    
    private void openAddPatientDialog() {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Add New Patient");
        dialog.setLayout(new GridLayout(2, false));
        
        // Create input fields
        Label nameLabel = new Label(dialog, SWT.NONE);
        nameLabel.setText("Name:");
        Text nameText = new Text(dialog, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label ageLabel = new Label(dialog, SWT.NONE);
        ageLabel.setText("Age:");
        Text ageText = new Text(dialog, SWT.BORDER);
        ageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label genderLabel = new Label(dialog, SWT.NONE);
        genderLabel.setText("Gender:");
        Text genderText = new Text(dialog, SWT.BORDER);
        genderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label contactLabel = new Label(dialog, SWT.NONE);
        contactLabel.setText("Contact:");
        Text contactText = new Text(dialog, SWT.BORDER);
        contactText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label historyLabel = new Label(dialog, SWT.NONE);
        historyLabel.setText("Medical History:");
        Text historyText = new Text(dialog, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        GridData historyData = new GridData(SWT.FILL, SWT.FILL, true, true);
        historyData.heightHint = 100;
        historyText.setLayoutData(historyData);
        
        // Create buttons
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        GridData buttonGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonGridData.horizontalSpan = 2;
        buttonArea.setLayoutData(buttonGridData);
        
        Button saveButton = new Button(buttonArea, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        // Add event handlers
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (validatePatientInput(nameText, ageText, genderText, contactText)) {
                    try {
                        String name = nameText.getText().trim();
                        int age = Integer.parseInt(ageText.getText().trim());
                        String gender = genderText.getText().trim();
                        String contact = contactText.getText().trim();
                        String history = historyText.getText().trim();
                        
                        Patient newPatient = new Patient(name, age, gender, contact, history);
                        boolean success = patientService.addPatient(newPatient);
                        
                        if (success) {
                            dialog.dispose();
                            refreshPatientList();
                            showMessage("Success", "Patient added successfully!");
                        } else {
                            showMessage("Error", "Failed to add patient");
                        }
                    } catch (NumberFormatException ex) {
                        showMessage("Invalid Input", "Please enter a valid age");
                    }
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
        dialog.setSize(400, dialog.getSize().y);
        centerDialog(dialog);
        dialog.open();
    }
    
    private void viewSelectedPatient() {
        TableItem[] selectedItems = patientTable.getSelection();
        
        if (selectedItems.length == 0) {
            showMessage("No Selection", "Please select a patient to view");
            return;
        }
        
        TableItem selectedItem = selectedItems[0];
        int patientId = Integer.parseInt(selectedItem.getText(0));
        
        String patientDetails = patientService.getPatientDetails(patientId);
        
        if (patientDetails != null) {
            Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            dialog.setText("Patient Details");
            dialog.setLayout(new GridLayout(1, false));
            
            Text detailsText = new Text(dialog, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL);
            GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
            textData.widthHint = 400;
            textData.heightHint = 200;
            detailsText.setLayoutData(textData);
            detailsText.setText(patientDetails);
            
            Button closeButton = new Button(dialog, SWT.PUSH);
            closeButton.setText("Close");
            closeButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
            
            closeButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    dialog.dispose();
                }
            });
            
            dialog.pack();
            centerDialog(dialog);
            dialog.open();
        } else {
            showMessage("Error", "Could not retrieve patient details");
        }
    }
    
    private void updateSelectedPatient() {
        TableItem[] selectedItems = patientTable.getSelection();
        
        if (selectedItems.length == 0) {
            showMessage("No Selection", "Please select a patient to update");
            return;
        }
        
        TableItem selectedItem = selectedItems[0];
        int patientId = Integer.parseInt(selectedItem.getText(0));
        
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Update Patient");
        dialog.setLayout(new GridLayout(2, false));
        
        // Create input fields
        Label nameLabel = new Label(dialog, SWT.NONE);
        nameLabel.setText("Name:");
        Text nameText = new Text(dialog, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameText.setText(selectedItem.getText(1));
        
        Label ageLabel = new Label(dialog, SWT.NONE);
        ageLabel.setText("Age:");
        Text ageText = new Text(dialog, SWT.BORDER);
        ageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        ageText.setText(selectedItem.getText(2));
        
        Label genderLabel = new Label(dialog, SWT.NONE);
        genderLabel.setText("Gender:");
        Text genderText = new Text(dialog, SWT.BORDER);
        genderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        genderText.setText(selectedItem.getText(3));
        
        Label contactLabel = new Label(dialog, SWT.NONE);
        contactLabel.setText("Contact:");
        Text contactText = new Text(dialog, SWT.BORDER);
        contactText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        contactText.setText(selectedItem.getText(4));
        
        Label historyLabel = new Label(dialog, SWT.NONE);
        historyLabel.setText("Medical History:");
        Text historyText = new Text(dialog, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        GridData historyData = new GridData(SWT.FILL, SWT.FILL, true, true);
        historyData.heightHint = 100;
        historyText.setLayoutData(historyData);
        historyText.setText(selectedItem.getText(5));
        
        // Create buttons
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        GridData buttonGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        buttonGridData.horizontalSpan = 2;
        buttonArea.setLayoutData(buttonGridData);
        
        Button saveButton = new Button(buttonArea, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        // Add event handlers
// Add event handlers
saveButton.addSelectionListener(new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
        if (validatePatientInput(nameText, ageText, genderText, contactText)) {
            try {
                String name = nameText.getText().trim();
                int age = Integer.parseInt(ageText.getText().trim());
                String gender = genderText.getText().trim();
                String contact = contactText.getText().trim();
                String history = historyText.getText().trim();
                
                Patient updatedPatient = new Patient(patientId, name, age, gender, contact, history);
                boolean success = patientService.updatePatient(updatedPatient);
                
                if (success) {
                    dialog.dispose();
                    refreshPatientList();
                    showMessage("Success", "Patient updated successfully!");
                } else {
                    showMessage("Error", "Failed to update patient");
                }
            } catch (NumberFormatException ex) {
                showMessage("Invalid Input", "Please enter a valid age");
            }
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
dialog.setSize(400, dialog.getSize().y);
centerDialog(dialog);
dialog.open();
    }

private void deleteSelectedPatient() {
    TableItem[] selectedItems = patientTable.getSelection();
    
    if (selectedItems.length == 0) {
        showMessage("No Selection", "Please select a patient to delete");
        return;
    }
    
    TableItem selectedItem = selectedItems[0];
    int patientId = Integer.parseInt(selectedItem.getText(0));
    
    MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    confirmBox.setText("Confirm Delete");
    confirmBox.setMessage("Are you sure you want to delete patient " + selectedItem.getText(1) + "?");
    
    if (confirmBox.open() == SWT.YES) {
        boolean success = patientService.deletePatient(patientId);
        
        if (success) {
            refreshPatientList();
            showMessage("Success", "Patient deleted successfully!");
        } else {
            showMessage("Error", "Failed to delete patient");
        }
    }
}

private boolean validatePatientInput(Text nameText, Text ageText, Text genderText, Text contactText) {
    if (nameText.getText().trim().isEmpty()) {
        showMessage("Validation Error", "Patient name cannot be empty");
        nameText.setFocus();
        return false;
    }
    
    if (ageText.getText().trim().isEmpty()) {
        showMessage("Validation Error", "Age cannot be empty");
        ageText.setFocus();
        return false;
    }
    
    try {
        int age = Integer.parseInt(ageText.getText().trim());
        if (age < 0 || age > 150) {
            showMessage("Validation Error", "Please enter a valid age (0-150)");
            ageText.setFocus();
            return false;
        }
    } catch (NumberFormatException e) {
        showMessage("Validation Error", "Please enter a valid numeric age");
        ageText.setFocus();
        return false;
    }
    
    if (genderText.getText().trim().isEmpty()) {
        showMessage("Validation Error", "Gender cannot be empty");
        genderText.setFocus();
        return false;
    }
    
    if (contactText.getText().trim().isEmpty()) {
        showMessage("Validation Error", "Contact information cannot be empty");
        contactText.setFocus();
        return false;
    }
    
    return true;
}

private boolean validateVitalsFields(Text tempText, Text hrText, Text sysText, Text diaText, Text rrText) {
    try {
        // Validate temperature (normal range approximately 35-42°C)
        double temp = Double.parseDouble(tempText.getText().trim());
        if (temp < 30 || temp > 45) {
            showMessage("Validation Error", "Temperature should be between 30°C and 45°C");
            tempText.setFocus();
            return false;
        }
        
        // Validate heart rate (normal range approximately 40-200 bpm)
        int hr = Integer.parseInt(hrText.getText().trim());
        if (hr < 20 || hr > 250) {
            showMessage("Validation Error", "Heart rate should be between 20 and 250 bpm");
            hrText.setFocus();
            return false;
        }
        
        // Validate systolic BP (normal range approximately 70-220 mmHg)
        int sys = Integer.parseInt(sysText.getText().trim());
        if (sys < 50 || sys > 250) {
            showMessage("Validation Error", "Systolic BP should be between 50 and 250 mmHg");
            sysText.setFocus();
            return false;
        }
        
        // Validate diastolic BP (normal range approximately 40-130 mmHg)
        int dia = Integer.parseInt(diaText.getText().trim());
        if (dia < 30 || dia > 150) {
            showMessage("Validation Error", "Diastolic BP should be between 30 and 150 mmHg");
            diaText.setFocus();
            return false;
        }
        
        // Validate respiratory rate (normal range approximately 8-30 breaths/min)
        int rr = Integer.parseInt(rrText.getText().trim());
        if (rr < 5 || rr > 50) {
            showMessage("Validation Error", "Respiratory rate should be between 5 and 50 breaths/min");
            rrText.setFocus();
            return false;
        }
        
        return true;
    } catch (NumberFormatException e) {
        showMessage("Validation Error", "All vitals fields must contain valid numbers");
        return false;
    }
}

private Vitals createVitalsFromFields(Text tempText, Text hrText, Text sysText, Text diaText, Text rrText) {
    double temp = Double.parseDouble(tempText.getText().trim());
    int hr = Integer.parseInt(hrText.getText().trim());
    int sys = Integer.parseInt(sysText.getText().trim());
    int dia = Integer.parseInt(diaText.getText().trim());
    int rr = Integer.parseInt(rrText.getText().trim());
    
    return new Vitals(temp, hr, sys, dia, rr);
}

private void clearVitalsFields(Text tempText, Text hrText, Text sysText, Text diaText, Text rrText) {
    tempText.setText("");
    hrText.setText("");
    sysText.setText("");
    diaText.setText("");
    rrText.setText("");
}

private void showMessage(String title, String message) {
    MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
    messageBox.setText(title);
    messageBox.setMessage(message);
    messageBox.open();
}

private void centerDialog(Shell dialog) {
    Rectangle parentBounds = shell.getBounds();
    Rectangle dialogBounds = dialog.getBounds();
    int x = parentBounds.x + (parentBounds.width - dialogBounds.width) / 2;
    int y = parentBounds.y + (parentBounds.height - dialogBounds.height) / 2;
    dialog.setLocation(x, y);
}

@Override
public void stop(BundleContext context) throws Exception {
    // Unget services
    if (patientServiceRef != null) {
        context.ungetService(patientServiceRef);
    }
    
    if (vitalsServiceRef != null) {
        context.ungetService(vitalsServiceRef);
    }
    
    if (diagnosisServiceRef != null) {
        context.ungetService(diagnosisServiceRef);
    }
    
    // Dispose UI resources if not already disposed
    if (display != null && !display.isDisposed()) {
        display.asyncExec(() -> {
            if (shell != null && !shell.isDisposed()) {
                shell.dispose();
            }
        });
    }
}
}