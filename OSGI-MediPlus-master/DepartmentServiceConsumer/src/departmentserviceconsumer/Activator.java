package departmentserviceconsumer;

import departmentservicepublisher.Department;
import departmentservicepublisher.IDepartmentService;
import staffservicepublisher.Staff;

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
import org.eclipse.swt.widgets.Composite;
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

import java.util.List;

public class Activator implements BundleActivator {
    private ServiceReference<?> departmentServiceRef;
    private IDepartmentService departmentService;
    private Display display;
    private Shell shell;
    private Table departmentTable;
    private Font titleFont;
    private Font buttonFont;
    private Color headerColor;
    private Color buttonColor;

    @Override
    public void start(BundleContext context) throws Exception {
        // Get the department service
        departmentServiceRef = context.getServiceReference(IDepartmentService.class.getName());
        departmentService = (IDepartmentService) context.getService(departmentServiceRef);

        // Create UI in a separate thread
        Thread uiThread = new Thread(() -> createAndShowGUI());
        uiThread.start();
    }

    private void createAndShowGUI() {
        display = new Display();
        shell = new Shell(display);
        shell.setText("Department Management System");
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

        
        CTabItem departmentTab = new CTabItem(tabFolder, SWT.NONE);
        departmentTab.setText("Department Management");
        Composite departmentComposite = createDepartmentTabContent(tabFolder);
        departmentTab.setControl(departmentComposite);

        
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

    private Composite createDepartmentTabContent(CTabFolder parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        
        Label headerLabel = new Label(composite, SWT.CENTER);
        headerLabel.setText("Department Management System");
        headerLabel.setFont(titleFont);
        headerLabel.setForeground(display.getSystemColor(SWT.COLOR_DARK_BLUE));
        GridData headerData = new GridData(SWT.FILL, SWT.TOP, true, false);
        headerData.horizontalSpan = 2;
        headerLabel.setLayoutData(headerData);

        
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

       
        departmentTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        departmentTable.setHeaderVisible(true);
        departmentTable.setLinesVisible(true);
        departmentTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        String[] titles = {"ID", "Name"};
        for (String title : titles) {
            TableColumn column = new TableColumn(departmentTable, SWT.NONE);
            column.setText(title);
            column.setWidth(150);
        }

        
        Composite bottomControls = new Composite(composite, SWT.NONE);
        bottomControls.setLayout(new GridLayout(3, true));
        bottomControls.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

        Button addButton = new Button(bottomControls, SWT.PUSH);
        addButton.setText("Add Department");
        addButton.setFont(buttonFont);
        addButton.setBackground(buttonColor);

        Button updateButton = new Button(bottomControls, SWT.PUSH);
        updateButton.setText("Update Department");
        updateButton.setFont(buttonFont);
        updateButton.setBackground(buttonColor);

        Button deleteButton = new Button(bottomControls, SWT.PUSH);
        deleteButton.setText("Delete Department");
        deleteButton.setFont(buttonFont);
        deleteButton.setBackground(buttonColor);

        
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String searchTerm = searchText.getText().trim();
                searchDepartments(searchTerm);
            }
        });

        refreshButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshDepartmentList();
            }
        });

        addButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                openAddDepartmentDialog();
            }
        });

        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateSelectedDepartment();
            }
        });

        deleteButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteSelectedDepartment();
            }
        });

        
        refreshDepartmentList();

        return composite;
    }

    private void refreshDepartmentList() {
        departmentTable.removeAll();
        List<Department> departments = departmentService.getAllDepartments();
        for (Department department : departments) {
            TableItem item = new TableItem(departmentTable, SWT.NONE);
            item.setText(0, String.valueOf(department.getId()));
            item.setText(1, department.getName());
        }
    }

    private void searchDepartments(String term) {
        departmentTable.removeAll();
        List<Department> departments = departmentService.searchDepartments(term);
        for (Department department : departments) {
            TableItem item = new TableItem(departmentTable, SWT.NONE);
            item.setText(0, String.valueOf(department.getId()));
            item.setText(1, department.getName());
        }
    }

    private void openAddDepartmentDialog() {
        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Add New Department");
        dialog.setLayout(new GridLayout(2, false));

        
        Label nameLabel = new Label(dialog, SWT.NONE);
        nameLabel.setText("Name:");
        Text nameText = new Text(dialog, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        // Create buttons
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
                if (!name.isEmpty()) {
                    Department department = new Department(0, name); // ID will be auto-generated
                    boolean success = departmentService.addDepartment(department);
                    if (success) {
                        dialog.dispose();
                        refreshDepartmentList();
                        showMessage("Success", "Department added successfully!");
                    } else {
                        showMessage("Error", "Failed to add department");
                    }
                } else {
                    showMessage("Validation Error", "Department name cannot be empty");
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

    private void updateSelectedDepartment() {
        TableItem[] selectedItems = departmentTable.getSelection();
        if (selectedItems.length == 0) {
            showMessage("No Selection", "Please select a department to update");
            return;
        }

        TableItem selectedItem = selectedItems[0];
        int departmentId = Integer.parseInt(selectedItem.getText(0));

        Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        dialog.setText("Update Department");
        dialog.setLayout(new GridLayout(2, false));

        // Create input fields
        Label nameLabel = new Label(dialog, SWT.NONE);
        nameLabel.setText("Name:");
        Text nameText = new Text(dialog, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        nameText.setText(selectedItem.getText(1));

      
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
                if (!name.isEmpty()) {
                    Department department = new Department(departmentId, name);
                    boolean success = departmentService.updateDepartment(department);
                    if (success) {
                        dialog.dispose();
                        refreshDepartmentList();
                        showMessage("Success", "Department updated successfully!");
                    } else {
                        showMessage("Error", "Failed to update department");
                    }
                } else {
                    showMessage("Validation Error", "Department name cannot be empty");
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

    private void deleteSelectedDepartment() {
        TableItem[] selectedItems = departmentTable.getSelection();
        if (selectedItems.length == 0) {
            showMessage("No Selection", "Please select a department to delete");
            return;
        }

        TableItem selectedItem = selectedItems[0];
        int departmentId = Integer.parseInt(selectedItem.getText(0));

        MessageBox confirmBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        confirmBox.setText("Confirm Delete");
        confirmBox.setMessage("Are you sure you want to delete department " + selectedItem.getText(1) + "?");

        if (confirmBox.open() == SWT.YES) {
            boolean success = departmentService.deleteDepartment(departmentId);
            if (success) {
                refreshDepartmentList();
                showMessage("Success", "Department deleted successfully!");
            } else {
                showMessage("Error", "Failed to delete department");
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
       
        if (departmentServiceRef != null) {
            context.ungetService(departmentServiceRef);
        }

       
        if (display != null && !display.isDisposed()) {
            display.asyncExec(() -> {
                if (shell != null && !shell.isDisposed()) {
                    shell.dispose();
                }
            });
        }
    }
}