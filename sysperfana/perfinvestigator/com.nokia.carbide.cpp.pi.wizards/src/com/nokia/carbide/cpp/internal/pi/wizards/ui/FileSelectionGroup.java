/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
 * All rights reserved.
 * This component and the accompanying materials are made available
 * under the terms of the License "Eclipse Public License v1.0"
 * which accompanies this distribution, and is available
 * at the URL "http://www.eclipse.org/legal/epl-v10.html".
 *
 * Initial Contributors:
 * Nokia Corporation - initial contribution.
 *
 * Contributors:
 *
 * Description: 
 *
 */

package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nokia.carbide.cpp.pi.PiPlugin;

/**
 * Provides content of the profile data file selection from file system group
 */
public class FileSelectionGroup extends AbstractBaseGroup {
	
	private TableViewer profileDataTable;
	
	/**
	 * Constructor 
	 * 
	 * @param parent instance of the parent composite 
	 * @param wizardSettings instance of the INewPIWizardSettings
	 */
	public FileSelectionGroup(Composite parent,
			INewPIWizardSettings wizardSettings) {
		super(parent, wizardSettings);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.wizards.ui.AbstractBaseGroup#createContent()
	 */
	protected void createContent() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.setText(Messages.getString("FileSelectionGroup.title")); //$NON-NLS-1$
		
		
		profileDataTable = new TableViewer(this, SWT.BORDER| SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
				
		TableColumn column = new TableColumn(profileDataTable.getTable(), SWT.NONE);
		column.setText(Messages.getString("FileSelectionGroup.profilerDataFileName")); //$NON-NLS-1$
		column.setWidth(200);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e)
	        {
	        	if (!(e.widget instanceof TableColumn))
	        		return;
	        	((AbstractBaseSorter)profileDataTable.getSorter()).doSort(0);
	        	profileDataTable.refresh();
	        	
	        }
		});		
		
		column = new TableColumn(profileDataTable.getTable(), SWT.NONE);
		column.setText(Messages.getString("FileSelectionGroup.profilerDataFilePath")); //$NON-NLS-1$
		column.setWidth(300);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e)
	        {
	        	if (!(e.widget instanceof TableColumn))
	        		return;
	        	((AbstractBaseSorter)profileDataTable.getSorter()).doSort(1);
	        	profileDataTable.refresh();
	        	
	        }
		});
	
		GridData fileLogsTableGridData = new GridData(GridData.FILL_BOTH);
		profileDataTable.getTable().setLayoutData(fileLogsTableGridData);
		profileDataTable.getTable().setHeaderVisible(true);
		profileDataTable.getTable().setLinesVisible(true);
		profileDataTable.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List<?>){
					return ((List<?>)inputElement).toArray();
				}
				return new Object[0];
			}
			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
		});
	
		profileDataTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if(profileDataTable.getTable().getSelectionCount() == 1){
					wizardSettings.validatePage();				
					
				}				
			}
		});
		
		
		profileDataTable.setSorter(	new AbstractBaseSorter(profileDataTable.getTable(), 0){
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				IPath path1 = (IPath)e1;
				IPath path2 = (IPath)e2;
				int returnCode = 0;
				switch (column) {
				case 0:
					returnCode = path1.lastSegment().compareTo(path2.lastSegment());
					break;
				case 1:
					returnCode = path1.removeLastSegments(1).toOSString().compareTo(path2.removeLastSegments(1).toOSString());
					break;

				default:
					break;
				}
				if (!sortAscending)
					returnCode = -returnCode;
				return returnCode;
			}			
		});		
	
		profileDataTable.setLabelProvider(new AbstractLabelProvider() {
			public String getColumnText(Object element, int columnIndex) {
				IPath path = (IPath)element;
				switch (columnIndex) {
				case 0:	
					return path.lastSegment();
				case 1:
					return path.removeLastSegments(1).toOSString();
				default:
					break;
				}
				return ""; //$NON-NLS-1$
			}			
		});


		Composite fileSelectionButtonComposite = new Composite(this, SWT.NONE);
		GridLayout threadButtonLayout = new GridLayout();
		GridData fileSelectionButtonGridData = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL);
		fileSelectionButtonComposite.setLayoutData(fileSelectionButtonGridData);
		fileSelectionButtonComposite.setLayout(threadButtonLayout);
		threadButtonLayout.numColumns = 1;

		// Add file button
		Button addFileButton = new Button(fileSelectionButtonComposite,
				SWT.PUSH);
		addFileButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addFileButton.setText(Messages.getString("FileSelectionGroup.actionAddFile")); //$NON-NLS-1$
		addFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// open file dialog for selecting a crash file
				FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
				dialog.setText(Messages.getString("FileSelectionGroup.dialogFileTitle")); //$NON-NLS-1$
				String[] filterExt = { "*.dat", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
				dialog.setFilterExtensions(filterExt);
				String path = dialog.open();
				if(path != null){
					IPath folder = new Path(path).removeLastSegments(1);					
					for(String fileName: dialog.getFileNames()){
						try {
							addProfileDataFile(folder.append(fileName));						
						} catch (IllegalArgumentException iae) {
							IStatus status = new Status(Status.ERROR,
									PiPlugin.PLUGIN_ID, iae.getMessage());
							ErrorDialog.openError(getShell(),
									Messages.getString("FileSelectionGroup.performanceInvestigator"), null, status); //$NON-NLS-1$
						}					
					}
					refreshTable(profileDataTable);
				}			
			}
		});

		// Add folder button
		Button addDirectoryButton = new Button(fileSelectionButtonComposite,
				SWT.PUSH);
		addDirectoryButton
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addDirectoryButton.setText(Messages.getString("FileSelectionGroup.actionAddDirectory")); //$NON-NLS-1$
		addDirectoryButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				// open file dialog for selecting a crash file
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog
						.setText(Messages.getString("FileSelectionGroup.dialogDirectoryTitle")); //$NON-NLS-1$
				String result = dialog.open();
				if (result != null) {
					try {
						addDirectory(new Path(result));			
						refreshTable(profileDataTable);
						
					} catch (IllegalArgumentException iae) {
						IStatus status = new Status(Status.ERROR,
								PiPlugin.PLUGIN_ID, iae.getMessage());
						ErrorDialog.openError(getShell(),
								Messages.getString("FileSelectionGroup.performanceInvestigator"), null, status); //$NON-NLS-1$
					}
				}
			}
		});

		// Remove one file button
		Button removeOneButton = new Button(fileSelectionButtonComposite,
				SWT.PUSH);
		removeOneButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeOneButton.setText(Messages.getString("FileSelectionGroup.actionRemove")); //$NON-NLS-1$
		removeOneButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSelectedItem(profileDataTable);
				refreshTable(profileDataTable);
			}

		});

		// Remove all files and folders button
		Button removeAllButton = new Button(fileSelectionButtonComposite,
				SWT.PUSH);
		removeAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeAllButton.setText(Messages.getString("FileSelectionGroup.actionRemoveAll")); //$NON-NLS-1$
		removeAllButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeAll();
				refreshTable(profileDataTable);
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.wizards.ui.AbstractBaseGroup#getTable()
	 */
	@Override
	public Table getTable() {
		return profileDataTable.getTable();
	}
}
