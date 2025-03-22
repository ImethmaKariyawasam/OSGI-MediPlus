package appointmentserviceconsumer;

import com.appointmentServicePublisher.IAppointmentService;
import com.appointmentServicePublisher.Appointment;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.text.SimpleDateFormat;

public class AppointmentUI {
    private Shell shell;
    private IAppointmentService appointmentService;
    private Table appointmentTable;
    private Text searchText;
    private Combo filterCombo;
    private Display display;
    private Color headerColor;
    private Color successColor;
    private Color warningColor;
    private Font headerFont;
    private Font boldFont;
    
    // Status constants for consistent display
    private static final String STATUS_SCHEDULED = "Scheduled";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String STATUS_CANCELLED = "Cancelled";

    public AppointmentUI(IAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public void open() {
        display = new Display();
        shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setText("Healthcare Appointment Management System");
        shell.setLayout(new GridLayout(1, false));
        
        // Create colors and fonts
        headerColor = new Color(display, 41, 128, 185);
        successColor = new Color(display, 46, 204, 113);
        warningColor = new Color(display, 231, 76, 60);
        
        FontData headerFontData = new FontData("Arial", 14, SWT.BOLD);
        headerFont = new Font(display, headerFontData);
        
        FontData boldFontData = new FontData("Arial", 10, SWT.BOLD);
        boldFont = new Font(display, boldFontData);

        // Create header
        Composite headerComp = new Composite(shell, SWT.NONE);
        headerComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        headerComp.setLayout(new GridLayout(1, false));
        headerComp.setBackground(headerColor);
        
        Label headerLabel = new Label(headerComp, SWT.CENTER);
        headerLabel.setText("Healthcare Appointment Management");
        headerLabel.setFont(headerFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        headerLabel.setBackground(headerColor);
        headerLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
        
        // Create toolbar area
        Composite toolbarComp = new Composite(shell, SWT.NONE);
        toolbarComp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        toolbarComp.setLayout(new GridLayout(4, false));
        
        // Add search functionality
        Label searchLabel = new Label(toolbarComp, SWT.NONE);
        searchLabel.setText("Search:");
        searchLabel.setFont(boldFont);
        
        searchText = new Text(toolbarComp, SWT.BORDER | SWT.SEARCH);
        searchText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        searchText.setMessage("Enter patient name or ID");
        
        // Add filter functionality
        Label filterLabel = new Label(toolbarComp, SWT.NONE);
        filterLabel.setText("Filter by Status:");
        filterLabel.setFont(boldFont);
        
        filterCombo = new Combo(toolbarComp, SWT.READ_ONLY | SWT.DROP_DOWN);
        filterCombo.setItems(new String[]{"All", STATUS_SCHEDULED, STATUS_COMPLETED, STATUS_CANCELLED});
        filterCombo.select(0);
        filterCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        // Add button bar
        Composite buttonBar = new Composite(shell, SWT.NONE);
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        buttonBar.setLayout(new RowLayout(SWT.HORIZONTAL));
        
        Button refreshButton = new Button(buttonBar, SWT.PUSH);
        refreshButton.setText("Refresh");
        refreshButton.setLayoutData(new RowData(120, 40));
        
        Button scheduleButton = new Button(buttonBar, SWT.PUSH);
        scheduleButton.setText("Schedule New");
        scheduleButton.setLayoutData(new RowData(150, 40));
        
        Button editButton = new Button(buttonBar, SWT.PUSH);
        editButton.setText("Edit Selected");
        editButton.setLayoutData(new RowData(150, 40));
        
        Button cancelButton = new Button(buttonBar, SWT.PUSH);
        cancelButton.setText("Cancel Selected");
        cancelButton.setLayoutData(new RowData(150, 40));
        
        // Create a table to display appointments with improved styling
        appointmentTable = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData tableGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableGridData.heightHint = 400;
        appointmentTable.setLayoutData(tableGridData);
        appointmentTable.setHeaderVisible(true);
        appointmentTable.setLinesVisible(true);

        // Add columns to the table
        String[] columnNames = {"ID", "Patient", "Doctor", "Date & Time", "Reason", "Status", "Actions"};
        int[] columnWidths = {70, 120, 120, 180, 200, 100, 120};
        
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn column = new TableColumn(appointmentTable, SWT.NONE);
            column.setText(columnNames[i]);
            column.setWidth(columnWidths[i]);
            column.setResizable(true);
        }
        
        // Create status bar
        Composite statusBar = new Composite(shell, SWT.NONE);
        statusBar.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        statusBar.setLayout(new GridLayout(2, false));
        
        Label statusLabel = new Label(statusBar, SWT.NONE);
        statusLabel.setText("Ready");
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label countLabel = new Label(statusBar, SWT.RIGHT);
        countLabel.setText("Total: 0 appointments");
        countLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

        // Add event handlers
        refreshButton.addListener(SWT.Selection, e -> {
            viewAppointments();
            statusLabel.setText("Appointments refreshed at " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        });
        
        scheduleButton.addListener(SWT.Selection, e -> {
            openScheduleAppointmentDialog();
        });
        
        editButton.addListener(SWT.Selection, e -> {
            if (appointmentTable.getSelectionCount() > 0) {
                TableItem selectedItem = appointmentTable.getSelection()[0];
                int appointmentId = Integer.parseInt(selectedItem.getText(0));
                openEditAppointmentDialog(appointmentId);
            } else {
                showMessage("Please select an appointment to edit", SWT.ICON_INFORMATION);
            }
        });
        
        cancelButton.addListener(SWT.Selection, e -> {
            if (appointmentTable.getSelectionCount() > 0) {
                TableItem selectedItem = appointmentTable.getSelection()[0];
                int appointmentId = Integer.parseInt(selectedItem.getText(0));
                cancelAppointment(appointmentId);
            } else {
                showMessage("Please select an appointment to cancel", SWT.ICON_INFORMATION);
            }
        });
        
        searchText.addListener(SWT.Modify, e -> {
            viewAppointments(); // Refresh with filter
        });
        
        filterCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewAppointments(); // Refresh with filter
            }
        });
        
        // Register dispose listeners to clean up resources
        shell.addDisposeListener(e -> {
            headerColor.dispose();
            successColor.dispose();
            warningColor.dispose();
            headerFont.dispose();
            boldFont.dispose();
        });

        shell.pack();
        shell.setSize(980, 700);
        shell.setMinimumSize(800, 600);
        
        // Center the shell on screen
        Monitor primary = display.getPrimaryMonitor();
        org.eclipse.swt.graphics.Rectangle bounds = primary.getBounds();
        org.eclipse.swt.graphics.Rectangle rect = shell.getBounds();
        int x = bounds.x + (bounds.width - rect.width) / 2;
        int y = bounds.y + (bounds.height - rect.height) / 2;
        shell.setLocation(x, y);
        
        shell.open();
        
        // Initial load of appointments
        viewAppointments();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    public void close() {
        if (shell != null && !shell.isDisposed()) {
            shell.dispose();
        }
    }

    private void viewAppointments() {
        // Clear the table before populating it
        appointmentTable.removeAll();

        // Get filter text and status
        String searchFilter = searchText.getText().toLowerCase();
        String statusFilter = filterCombo.getText();
        if (statusFilter.equals("All")) {
            statusFilter = "";
        }

        // Fetch appointments for the patient (this should be adapted to support multiple patients or search)
        List<Appointment> appointments = appointmentService.getAppointmentsByPatient(1);
        int displayedCount = 0;

        // Populate the table with appointment data
        for (Appointment appointment : appointments) {
            // Apply filters
            boolean matchesSearch = String.valueOf(appointment.getPatientId()).contains(searchFilter) ||
                                  appointment.getDoctorName().toLowerCase().contains(searchFilter) ||
                                  appointment.getReason().toLowerCase().contains(searchFilter);
            
            boolean matchesStatus = statusFilter.isEmpty() || appointment.getStatus().equals(statusFilter);
            
            if (matchesSearch && matchesStatus) {
                TableItem item = new TableItem(appointmentTable, SWT.NONE);
                item.setText(0, String.valueOf(appointment.getAppointmentId()));
                item.setText(1, String.valueOf(appointment.getPatientId()));
                item.setText(2, appointment.getDoctorName());
                
                // Format date nicely
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
                item.setText(3, sdf.format(appointment.getAppointmentDate()));
                
                item.setText(4, appointment.getReason());
                item.setText(5, appointment.getStatus());
                item.setText(6, "View Details");
                
                // Set color based on status
                if (appointment.getStatus().equals(STATUS_SCHEDULED)) {
                    item.setBackground(5, display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                } else if (appointment.getStatus().equals(STATUS_COMPLETED)) {
                    item.setBackground(5, successColor);
                } else if (appointment.getStatus().equals(STATUS_CANCELLED)) {
                    item.setBackground(5, warningColor);
                }
                
                displayedCount++;
            }
        }

        // Update the count label
        Label countLabel = (Label) ((Composite) shell.getChildren()[shell.getChildren().length - 1]).getChildren()[1];
        countLabel.setText("Displayed: " + displayedCount + " of " + appointments.size() + " appointments");

        // Adjust column widths to fit content
        for (TableColumn column : appointmentTable.getColumns()) {
            column.pack();
        }
    }

    private void openScheduleAppointmentDialog() {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Schedule New Appointment");
        dialog.setLayout(new GridLayout(2, false));
        
        // Add a header
        Label headerLabel = new Label(dialog, SWT.NONE);
        headerLabel.setText("Enter Appointment Details");
        headerLabel.setFont(headerFont);
        GridData headerData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        headerData.verticalIndent = 10;
        headerLabel.setLayoutData(headerData);

        // Add input fields with better spacing and layout
        addFormField(dialog, "Patient ID:", "Enter patient ID", false);
        Text patientIdText = (Text) dialog.getChildren()[dialog.getChildren().length - 1];
        
        addFormField(dialog, "Doctor Name:", "Enter doctor's full name", false);
        Text doctorText = (Text) dialog.getChildren()[dialog.getChildren().length - 1];
        
        // Add date/time selection
        Label dateLabel = new Label(dialog, SWT.NONE);
        dateLabel.setText("Appointment Date:");
        dateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
        Composite dateTimeComp = new Composite(dialog, SWT.NONE);
        dateTimeComp.setLayout(new GridLayout(2, false));
        dateTimeComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        DateTime dateControl = new DateTime(dateTimeComp, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
        dateControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        DateTime timeControl = new DateTime(dateTimeComp, SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
        timeControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        addFormField(dialog, "Reason for Visit:", "Enter reason for appointment", true);
        Text reasonText = (Text) dialog.getChildren()[dialog.getChildren().length - 1];
        
        // Add duration selection
        Label durationLabel = new Label(dialog, SWT.NONE);
        durationLabel.setText("Duration:");
        durationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
        Composite durationComp = new Composite(dialog, SWT.NONE);
        durationComp.setLayout(new GridLayout(2, false));
        durationComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Combo durationCombo = new Combo(durationComp, SWT.DROP_DOWN | SWT.READ_ONLY);
        durationCombo.setItems(new String[]{"15 minutes", "30 minutes", "45 minutes", "60 minutes"});
        durationCombo.select(1); // Default to 30 minutes
        durationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Create buttons with better styling
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        GridData buttonGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
        buttonGridData.verticalIndent = 15;
        buttonArea.setLayoutData(buttonGridData);

        Button saveButton = new Button(buttonArea, SWT.PUSH);
        saveButton.setText("Save Appointment");
        saveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Add event handlers
        saveButton.addListener(SWT.Selection, e -> {
            try {
                int patientId = Integer.parseInt(patientIdText.getText().trim());
                String doctorName = doctorText.getText().trim();
                String reason = reasonText.getText().trim();

                // Validate inputs
                if (doctorName.isEmpty() || reason.isEmpty()) {
                    showMessage("Please fill in all required fields", SWT.ICON_ERROR);
                    return;
                }

                // Create a Date object from the DateTime controls
                Calendar cal = Calendar.getInstance();
                cal.set(dateControl.getYear(), dateControl.getMonth(), dateControl.getDay(), 
                        timeControl.getHours(), timeControl.getMinutes(), 0);
                Date appointmentDate = cal.getTime();

                // Create a new appointment
                Appointment appointment = new Appointment(0, patientId, doctorName, appointmentDate, 
                                                         reason, STATUS_SCHEDULED);
                boolean success = appointmentService.scheduleAppointment(appointment);

                if (success) {
                    showMessage("Appointment scheduled successfully!", SWT.ICON_INFORMATION);
                    dialog.dispose();
                    viewAppointments(); // Refresh the appointment table
                } else {
                    showMessage("Failed to schedule appointment. Please try again.", SWT.ICON_ERROR);
                }
            } catch (NumberFormatException ex) {
                showMessage("Please enter a valid Patient ID", SWT.ICON_ERROR);
            }
        });

        cancelButton.addListener(SWT.Selection, e -> dialog.dispose());

        dialog.pack();
        dialog.setSize(450, dialog.getSize().y);
        centerDialog(dialog);
        dialog.open();
    }
    
    private void openEditAppointmentDialog(int appointmentId) {
        // Get the appointment from the service
        Appointment appointment = appointmentService.getAppointment(appointmentId);
        if (appointment == null) {
            showMessage("Could not find appointment with ID: " + appointmentId, SWT.ICON_ERROR);
            return;
        }
        
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Edit Appointment");
        dialog.setLayout(new GridLayout(2, false));
        
        // Add a header
        Label headerLabel = new Label(dialog, SWT.NONE);
        headerLabel.setText("Edit Appointment Details");
        headerLabel.setFont(headerFont);
        GridData headerData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        headerData.verticalIndent = 10;
        headerLabel.setLayoutData(headerData);

        // Add input fields with better spacing and layout
        addFormField(dialog, "Patient ID:", "Enter patient ID", false);
        Text patientIdText = (Text) dialog.getChildren()[dialog.getChildren().length - 1];
        patientIdText.setText(String.valueOf(appointment.getPatientId()));
        
        addFormField(dialog, "Doctor Name:", "Enter doctor's full name", false);
        Text doctorText = (Text) dialog.getChildren()[dialog.getChildren().length - 1];
        doctorText.setText(appointment.getDoctorName());
        
        // Add date/time selection
        Label dateLabel = new Label(dialog, SWT.NONE);
        dateLabel.setText("Appointment Date:");
        dateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
        Composite dateTimeComp = new Composite(dialog, SWT.NONE);
        dateTimeComp.setLayout(new GridLayout(2, false));
        dateTimeComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointment.getAppointmentDate());
        
        DateTime dateControl = new DateTime(dateTimeComp, SWT.DATE | SWT.DROP_DOWN | SWT.BORDER);
        dateControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        dateControl.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        
        DateTime timeControl = new DateTime(dateTimeComp, SWT.TIME | SWT.DROP_DOWN | SWT.BORDER);
        timeControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        timeControl.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);
        
        addFormField(dialog, "Reason for Visit:", "Enter reason for appointment", true);
        Text reasonText = (Text) dialog.getChildren()[dialog.getChildren().length - 1];
        reasonText.setText(appointment.getReason());
        
        // Add duration selection
        Label durationLabel = new Label(dialog, SWT.NONE);
        durationLabel.setText("Duration:");
        durationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
        Composite durationComp = new Composite(dialog, SWT.NONE);
        durationComp.setLayout(new GridLayout(2, false));
        durationComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Combo durationCombo = new Combo(durationComp, SWT.DROP_DOWN | SWT.READ_ONLY);
        durationCombo.setItems(new String[]{"15 minutes", "30 minutes", "45 minutes", "60 minutes"});
        // Select the appropriate duration
        int durationIndex = 1; // Default to 30 minutes
        if (appointment.getDuration() == 15) durationIndex = 0;
        else if (appointment.getDuration() == 45) durationIndex = 2;
        else if (appointment.getDuration() == 60) durationIndex = 3;
        durationCombo.select(durationIndex);
        durationCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Add status selection
        Label statusLabel = new Label(dialog, SWT.NONE);
        statusLabel.setText("Status:");
        statusLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        
        Combo statusCombo = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        statusCombo.setItems(new String[]{STATUS_SCHEDULED, STATUS_COMPLETED, STATUS_CANCELLED});
        // Select the current status
        int statusIndex = 0; // Default to Scheduled
        if (appointment.getStatus().equals(STATUS_COMPLETED)) statusIndex = 1;
        else if (appointment.getStatus().equals(STATUS_CANCELLED)) statusIndex = 2;
        statusCombo.select(statusIndex);
        statusCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // Create buttons with better styling
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        GridData buttonGridData = new GridData(SWT.CENTER, SWT.CENTER, true, false, 2, 1);
        buttonGridData.verticalIndent = 15;
        buttonArea.setLayoutData(buttonGridData);

        Button saveButton = new Button(buttonArea, SWT.PUSH);
        saveButton.setText("Save Changes");
        saveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // Add event handlers
        saveButton.addListener(SWT.Selection, e -> {
            try {
                int patientId = Integer.parseInt(patientIdText.getText().trim());
                String doctorName = doctorText.getText().trim();
                String reason = reasonText.getText().trim();
                String status = statusCombo.getText();

                // Validate inputs
                if (doctorName.isEmpty() || reason.isEmpty()) {
                    showMessage("Please fill in all required fields", SWT.ICON_ERROR);
                    return;
                }

                // Create a Date object from the DateTime controls
                Calendar calendar = Calendar.getInstance();
                calendar.set(dateControl.getYear(), dateControl.getMonth(), dateControl.getDay(), 
                        timeControl.getHours(), timeControl.getMinutes(), 0);
                Date appointmentDate = calendar.getTime();
                
                // Get duration in minutes from the selected option
                int duration = 30; // Default
                String durationStr = durationCombo.getText();
                if (durationStr.equals("15 minutes")) duration = 15;
                else if (durationStr.equals("45 minutes")) duration = 45;
                else if (durationStr.equals("60 minutes")) duration = 60;

                // Update the appointment object
                appointment.setPatientId(patientId);
                appointment.setDoctorName(doctorName);
                appointment.setAppointmentDate(appointmentDate);
                appointment.setReason(reason);
                appointment.setStatus(status);
                appointment.setDuration(duration);
                
                boolean success = appointmentService.updateAppointment(appointment);

                if (success) {
                    showMessage("Appointment updated successfully!", SWT.ICON_INFORMATION);
                    dialog.dispose();
                    viewAppointments(); // Refresh the appointment table
                } else {
                    showMessage("Failed to update appointment. Please try again.", SWT.ICON_ERROR);
                }
            } catch (NumberFormatException ex) {
                showMessage("Please enter a valid Patient ID", SWT.ICON_ERROR);
            }
        });

        cancelButton.addListener(SWT.Selection, e -> dialog.dispose());

        dialog.pack();
        dialog.setSize(450, dialog.getSize().y);
        centerDialog(dialog);
        dialog.open();
    }
    
    private void cancelAppointment(int appointmentId) {
        MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmBox.setText("Confirm Cancellation");
        confirmBox.setMessage("Are you sure you want to cancel this appointment?");
        
        if (confirmBox.open() == SWT.YES) {
            boolean success = appointmentService.cancelAppointment(appointmentId);
            if (success) {
                showMessage("Appointment cancelled successfully!", SWT.ICON_INFORMATION);
                viewAppointments(); // Refresh the appointment table
            } else {
                showMessage("Failed to cancel appointment. Please try again.", SWT.ICON_ERROR);
            }
        }
    }
    
    private void addFormField(Shell parent, String labelText, String placeholder, boolean multiline) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelText);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
        
        int style = SWT.BORDER;
        if (multiline) {
            style |= SWT.MULTI | SWT.WRAP | SWT.V_SCROLL;
        }
        
        Text text = new Text(parent, style);
        GridData textGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        if (multiline) {
            textGridData.heightHint = 80;
            textGridData.verticalAlignment = SWT.FILL;
            textGridData.grabExcessVerticalSpace = true;
        }
        text.setLayoutData(textGridData);
        text.setMessage(placeholder);
    }
    
    private void showMessage(String message, int style) {
        MessageBox messageBox = new MessageBox(shell, style | SWT.OK);
        messageBox.setText(style == SWT.ICON_ERROR ? "Error" : "Information");
        messageBox.setMessage(message);
        messageBox.open();
    }
    
    private void centerDialog(Shell dialog) {
        org.eclipse.swt.graphics.Rectangle parentBounds = shell.getBounds();
        org.eclipse.swt.graphics.Rectangle dialogBounds = dialog.getBounds();
        int x = parentBounds.x + (parentBounds.width - dialogBounds.width) / 2;
        int y = parentBounds.y + (parentBounds.height - dialogBounds.height) / 2;
        dialog.setLocation(x, y);
    }
}