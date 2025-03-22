package staffserviceconsumer;

import staffservicepublisher.IStaffService;
import staffservicepublisher.Staff;
import departmentservicepublisher.IDepartmentService;
import departmentservicepublisher.Department;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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

import java.util.List;

public class Activator implements BundleActivator {
    private ServiceReference<?> staffServiceRef;
    private ServiceReference<?> departmentServiceRef;
    private IStaffService staffService;
    private IDepartmentService departmentService;
    private Display display;
    private Shell shell;
    private Table staffTable;
    private Font titleFont;
    private Font buttonFont;
    private Color headerColor;
    private Color buttonColor;

    @Override
    public void start(BundleContext context) throws Exception {
        
        staffServiceRef = context.getServiceReference(IStaffService.class.getName());
        departmentServiceRef = context.getServiceReference(IDepartmentService.class.getName());

        staffService = (IStaffService) context.getService(staffServiceRef);
        departmentService = (IDepartmentService) context.getService(departmentServiceRef);

       
        Thread uiThread = new Thread(() -> createAndShowGUI());
        uiThread.start();
    }

    private void createAndShowGUI() {
        display = new Display();
        shell = new Shell(display);
        shell.setText("Staff Management System");
        shell.setSize(900, 600);
        shell.setLayout(new FillLayout());

        
        titleFont = new Font(display, new FontData("Arial", 14, SWT.BOLD));
        buttonFont = new Font(display, new FontData("Arial", 10, SWT.NORMAL));
        headerColor = new Color(display, 135, 206, 235); 
        buttonColor = new Color(display, 100, 149, 237); 

        
        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        tabFolder.setSimple(false);
        tabFolder.setUnselectedImageVisible(false);
        tabFolder.setUnselectedCloseVisible(false);

        
        CTabItem staffTab = new CTabItem(tabFolder, SWT.NONE);
        staffTab.setText("Staff Management");
        Composite staffComposite = createStaffTabContent(tabFolder);
        staffTab.setControl(staffComposite);

       
        tabFolder.setSelection(0);

       
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

    private Composite createStaffTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        // Header label
        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Staff Management System");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        GridData headerData = new GridData(SWT.FILL, SWT.TOP, true, false);
        headerData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerData);

        // Create top controls
        Composite topControls = new Composite(composite, SWT.NONE);
        topControls.setLayout(new GridLayout(4, false));
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

        // Create table to display staff
        staffTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        staffTable.setHeaderVisible(true);
        staffTable.setLinesVisible(true);
        staffTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        String[] titles = {"ID", "Name", "Role", "Department ID", "Department Name", "Contact"};
        for (String title : titles) {
            TableColumn column = new TableColumn(staffTable, SWT.NONE);
            column.setText(title);
            column.setWidth(150);
        }

        // Create bottom controls for actions
        Composite bottomControls = new Composite(composite, SWT.NONE);
        bottomControls.setLayout(new GridLayout(3, true));
        bottomControls.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

        Button addButton = new Button(bottomControls, SWT.PUSH);
        addButton.setText("Add Staff");
        addButton.setFont(buttonFont);
        addButton.setBackground(buttonColor);

        Button updateButton = new Button(bottomControls, SWT.PUSH);
        updateButton.setText("Update Staff");
        updateButton.setFont(buttonFont);
        updateButton.setBackground(buttonColor);

        Button deleteButton = new Button(bottomControls, SWT.PUSH);
        deleteButton.setText("Delete Staff");
        deleteButton.setFont(buttonFont);
        deleteButton.setBackground(buttonColor);

        // Add event handlers
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String searchTerm = searchText.getText().trim();
                searchStaff(searchTerm);
            }
        });

        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshStaffList();
            }
        });

        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openAddStaffDialog();
            }
        });

        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectedStaff();
            }
        });

        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteSelectedStaff();
            }
        });

        // Initial population of the table
        refreshStaffList();

        return composite;
    }

    private void refreshStaffList() {
        staffTable.removeAll();
        List<Staff> staffList = staffService.getAllStaff();
        for (Staff staff : staffList) {
            TableItem item = new TableItem(staffTable, SWT.NONE);
            item.setText(0, String.valueOf(staff.getId())); // Staff ID
            item.setText(1, staff.getName()); // Staff Name
            item.setText(2, staff.getRole()); // Staff Role
            item.setText(3, String.valueOf(staff.getDepartmentId())); // Department ID
            item.setText(4, staff.getDepartmentName()); // Department Name
            item.setText(5, staff.getContact()); // Contact
        }
    }

    private void searchStaff(String searchTerm) {
        staffTable.removeAll();
        List<Staff> staffList = staffService.searchStaff(searchTerm);
        for (Staff staff : staffList) {
            TableItem item = new TableItem(staffTable, SWT.NONE);
            item.setText(0, String.valueOf(staff.getId())); 
            item.setText(1, staff.getName()); 
            item.setText(2, staff.getRole()); 
            item.setText(3, String.valueOf(staff.getDepartmentId())); 
            item.setText(4, staff.getDepartmentName()); 
            item.setText(5, staff.getContact()); 
        }
    }

    private void openAddStaffDialog() {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Add New Staff");
        dialog.setLayout(new GridLayout(2, false));

        // Create input fields
        Label nameLabel = new Label(dialog, SWT.NONE);
        nameLabel.setText("Name:");
        Text nameText = new Text(dialog, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label roleLabel = new Label(dialog, SWT.NONE);
        roleLabel.setText("Role:");
        Text roleText = new Text(dialog, SWT.BORDER);
        roleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label departmentLabel = new Label(dialog, SWT.NONE);
        departmentLabel.setText("Department:");
        Combo departmentCombo = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        departmentCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Populate department combo box
        List<Department> departments = departmentService.getAllDepartments();
        for (Department department : departments) {
            departmentCombo.add(department.getName());
        }

        Label contactLabel = new Label(dialog, SWT.NONE);
        contactLabel.setText("Contact:");
        Text contactText = new Text(dialog, SWT.BORDER);
        contactText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        buttonArea.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        Button saveButton = new Button(buttonArea, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setFont(buttonFont);
        saveButton.setBackground(buttonColor);

        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setFont(buttonFont);
        cancelButton.setBackground(buttonColor);

        // Add event handlers
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String name = nameText.getText().trim();
                String role = roleText.getText().trim();
                String departmentName = departmentCombo.getText();
                String contact = contactText.getText().trim();

                
                int departmentId = -1;
                for (Department department : departments) {
                    if (department.getName().equals(departmentName)) {
                        departmentId = department.getId();
                        break;
                    }
                }

                if (departmentId == -1) {
                    showMessage("Error", "Invalid department selected.");
                    return;
                }

                Staff newStaff = new Staff(0, name, role, contact, departmentId);
                boolean success = staffService.addStaff(newStaff);

                if (success) {
                    dialog.dispose();
                    refreshStaffList();
                    showMessage("Success", "Staff added successfully!");
                } else {
                    showMessage("Error", "Failed to add staff.");
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

    private void updateSelectedStaff() {
        TableItem[] selectedItems = staffTable.getSelection();
        if (selectedItems.length == 0) {
            showMessage("No Selection", "Please select a staff member to update.");
            return;
        }

        TableItem selectedItem = selectedItems[0];
        int staffId = Integer.parseInt(selectedItem.getText(0));

        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Update Staff");
        dialog.setLayout(new GridLayout(2, false));

        
        Label nameLabel = new Label(dialog, SWT.NONE);
        nameLabel.setText("Name:");
        Text nameText = new Text(dialog, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameText.setText(selectedItem.getText(1));

        Label roleLabel = new Label(dialog, SWT.NONE);
        roleLabel.setText("Role:");
        Text roleText = new Text(dialog, SWT.BORDER);
        roleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        roleText.setText(selectedItem.getText(2));

        Label departmentLabel = new Label(dialog, SWT.NONE);
        departmentLabel.setText("Department:");
        Combo departmentCombo = new Combo(dialog, SWT.DROP_DOWN | SWT.READ_ONLY);
        departmentCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        
        List<Department> departments = departmentService.getAllDepartments();
        for (Department department : departments) {
            departmentCombo.add(department.getName());
        }

        // Set the selected department
        String currentDepartment = selectedItem.getText(4); 
        departmentCombo.setText(currentDepartment);

        Label contactLabel = new Label(dialog, SWT.NONE);
        contactLabel.setText("Contact:");
        Text contactText = new Text(dialog, SWT.BORDER);
        contactText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        contactText.setText(selectedItem.getText(5));

        
        Composite buttonArea = new Composite(dialog, SWT.NONE);
        buttonArea.setLayout(new GridLayout(2, true));
        buttonArea.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        Button saveButton = new Button(buttonArea, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.setFont(buttonFont);
        saveButton.setBackground(buttonColor);

        Button cancelButton = new Button(buttonArea, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setFont(buttonFont);
        cancelButton.setBackground(buttonColor);

        // Add event handlers
        saveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String name = nameText.getText().trim();
                String role = roleText.getText().trim();
                String departmentName = departmentCombo.getText();
                String contact = contactText.getText().trim();

                
                int departmentId = -1;
                for (Department department : departments) {
                    if (department.getName().equals(departmentName)) {
                        departmentId = department.getId();
                        break;
                    }
                }

                if (departmentId == -1) {
                    showMessage("Error", "Invalid department selected.");
                    return;
                }

                Staff updatedStaff = new Staff(staffId, name, role, contact, departmentId);
                boolean success = staffService.updateStaff(updatedStaff);

                if (success) {
                    dialog.dispose();
                    refreshStaffList();
                    showMessage("Success", "Staff updated successfully!");
                } else {
                    showMessage("Error", "Failed to update staff.");
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

    private void deleteSelectedStaff() {
        TableItem[] selectedItems = staffTable.getSelection();
        if (selectedItems.length == 0) {
            showMessage("No Selection", "Please select a staff member to delete.");
            return;
        }

        TableItem selectedItem = selectedItems[0];
        int staffId = Integer.parseInt(selectedItem.getText(0));

        MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmBox.setText("Confirm Delete");
        confirmBox.setMessage("Are you sure you want to delete staff " + selectedItem.getText(1) + "?");

        if (confirmBox.open() == SWT.YES) {
            boolean success = staffService.deleteStaff(staffId);
            if (success) {
                refreshStaffList();
                showMessage("Success", "Staff deleted successfully!");
            } else {
                showMessage("Error", "Failed to delete staff.");
            }
        }
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
        if (staffServiceRef != null) {
            context.ungetService(staffServiceRef);
        }
        if (departmentServiceRef != null) {
            context.ungetService(departmentServiceRef);
        }
    }
}