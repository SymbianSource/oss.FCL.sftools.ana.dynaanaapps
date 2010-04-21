/*
 * Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies). 
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cdt.builder.project.ICarbideBuildConfiguration;
import com.nokia.carbide.cdt.builder.project.ISISBuilderInfo;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.IPkgEntry;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.PkgEntryList;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.PkgListTree;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.PkgListTreeContentProvider;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.PkgListTreeLabelProvider;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.PkgListTreeViewer;

public class NewPIWizardPagePkgListTask 
extends NewPIWizardPage 
implements INewPIWizardSettings {

	// control
	private Composite composite = null;
//	private Label label = null;
	private PkgListTreeViewer projectTreeViewer;
	private Composite filelistComposite = null;
	private Composite buttonComposite = null;	
	private Button addPkgButton = null;
	private Button removeButton = null;
	private boolean isSisBuilderConfigurationChecked;
	private PkgListTreeContentProvider pkgListTreeContentProvider;
	
	// data model
	PkgListTree pkgListRoot = null;

	protected NewPIWizardPagePkgListTask() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPagePkgListTask.title")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPagePkgListTask.description"));	 //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(gridLayout);
//		label = new Label(composite, SWT.NONE);
//		label.setText(Messages.getString("NewPIWizardPagePkgListTask.label")); //$NON-NLS-1$
		createFilelistComposite();
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_PKG_LIST);
	}
	
	/**
	 * This method initializes filelistComposite	
	 *
	 */
	private void createFilelistComposite() {
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 2;
		filelistComposite = new Composite(composite, SWT.NONE);
		filelistComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		filelistComposite.setLayout(gridLayout2);
		projectTreeViewer = new PkgListTreeViewer(filelistComposite, SWT.H_SCROLL | SWT.BORDER);
		pkgListRoot = new PkgListTree();
		projectTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		projectTreeViewer.setContentProvider(pkgListTreeContentProvider = new PkgListTreeContentProvider(pkgListRoot));
		projectTreeViewer.setLabelProvider(new DecoratingLabelProvider(
				new PkgListTreeLabelProvider(), PlatformUI.getWorkbench()
                        .getDecoratorManager().getLabelDecorator()));
		projectTreeViewer.setInput(pkgListRoot);
		projectTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent arg0) {
				ISelection selection = arg0.getSelection();
				// if selecting disabled item, ditch the selection and restore viewer
				// selection using data object
				if (selection instanceof TreeSelection) {
					TreeSelection treeSelection = (TreeSelection) selection;
					TreePath[] paths = treeSelection.getPaths();
					if (paths.length == 1) {
						if (projectTreeViewer.getElementEnabled(paths[0].getLastSegment()) == false) {
							setupPageFromFromNewPIWizardSettings();
						}
					}
				}
				NewPIWizardSettings npiws = NewPIWizardSettings.getInstance();
				npiws.selectedAppFileList.clear();
				for (IPkgEntry pkg : projectTreeViewer.getSelectedPkgs()) {
					npiws.selectedAppFileList.add(pkg);
				}
				npiws.selectedProjectList.clear();
				for (IProject project : projectTreeViewer.getSelectedProjects()) {
					npiws.selectedProjectList.add(project);
				}
				npiws.selectedBuildConfigList.clear();
				for (ICarbideBuildConfiguration config : projectTreeViewer.getSelectedConfigs()) {
					npiws.selectedBuildConfigList.add(config);
				}
				npiws.availableAppFileList.clear();
				IPkgEntry[] allPkg = pkgListRoot.getPkgEntries();
				for (IPkgEntry pkg: allPkg) {
					npiws.availableAppFileList.add(pkg);
				}
			}	
		});
		projectTreeViewer.expandToLevel(2);	// just to project level
				
		createButtonComposite();
	}

	/**
	 * This method initializes buttonComposite	
	 *
	 */
	private void createButtonComposite() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		buttonComposite = new Composite(filelistComposite, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		buttonComposite.setLayout(gridLayout);
		addPkgButton = new Button(buttonComposite, SWT.PUSH);
		addPkgButton.setText(Messages.getString("NewPIWizardPagePkgListTask.pkg.button")); //$NON-NLS-1$
		removeButton = new Button(buttonComposite, SWT.PUSH);
		removeButton.setText(Messages.getString("NewPIWizardPagePkgListTask.remove.button")); //$NON-NLS-1$

		GridData buttonWidthGridData = new GridData();
		buttonWidthGridData.widthHint = Math.max(addPkgButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x, removeButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		addPkgButton.setLayoutData(buttonWidthGridData);
		removeButton.setLayoutData(buttonWidthGridData);
		addPkgButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleAddPkg();
			}	
		});
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemovePkg();
			}	
		});
	}
	
	private void handleAddPkg() {
		String pkgFilePath;
		FileDialog dialog = new FileDialog(getShell());
		String[] pkgExtensions = {"*.pkg", //$NON-NLS-1$
									"*.*"}; //$NON-NLS-1$
		String[] pkgNames = {Messages.getString("NewPIWizardPagePkgListTask.pkg.filter.name"), //$NON-NLS-1$
								Messages.getString("NewPIWizardPagePkgListTask.all.filter.name")}; //$NON-NLS-1$
		dialog.setFilterExtensions(pkgExtensions);
		dialog.setFilterNames(pkgNames);
		pkgFilePath = dialog.open();
		
		if (pkgFilePath != null) {
			NewPIWizardPagePkgSdkDialog sdkDialog = new NewPIWizardPagePkgSdkDialog(getShell());
			if (sdkDialog.open() == NewPIWizardPagePkgSdkDialog.OK) {
				IPkgEntry pkgEntry = PkgEntryList.getInstance().getPkgEntry(pkgFilePath, sdkDialog.getSelection());
				boolean found = false;
				for (IPkgEntry entry : pkgListRoot.getPkgEntries()) {
					if (pkgEntry.equals(entry)) {
						found = true;
						break;
					}
				}
				if (found == false) {
					pkgListRoot.addPkgEntry(pkgEntry);
				}
				removeButton.setEnabled(pkgListRoot.getPkgEntries().length != 0);
				// only expanded/visible items are available for selecting programatically
				projectTreeViewer.reveal(pkgEntry);		
				// This shows the new PKG in UI and force associate of the new PKG, so we can select later
				projectTreeViewer.refresh();
				//projectTreeViewer.initializeSelectedItems(selectedObjects.toArray());
				// reveal is not necessary, but we do it for safety
			}
		}
	}

	private void handleRemovePkg() {
		NewPIWizardRemovePkgDialog removeDialog = new NewPIWizardRemovePkgDialog(getShell(), pkgListRoot.getPkgEntries());
		removeDialog.open();
		IPkgEntry[] removeList = removeDialog.getRemovedList();
		if (removeList != null) {
			for (int i = 0; i < removeList.length; i++) {
				pkgListRoot.removePkgEntry(removeList[i]);
			}
			removeButton.setEnabled(pkgListRoot.getPkgEntries().length != 0);
			projectTreeViewer.refresh();	
		}
	}
	
	public void setupPageFromFromNewPIWizardSettings() {
		NewPIWizardSettings npiws = NewPIWizardSettings.getInstance();
		ArrayList<TreePath> selectedPath = new ArrayList<TreePath>();
		pkgListRoot.removeAllPkgEntries();
		
		// Build TreePath for all selected configurations and their project parent
		ICarbideBuildConfiguration needToUnselect = null;
		for (ICarbideBuildConfiguration selected : npiws.selectedBuildConfigList) {
			List<ISISBuilderInfo> sisBuilderInfoList = selected.getSISBuilderInfoList();
			boolean allPkgGood = true && sisBuilderInfoList.size() > 0;
			for (ISISBuilderInfo sisBuilderInfo : sisBuilderInfoList)
			{
				if (sisBuilderInfo == null) {
					allPkgGood = false;
				} else if (sisBuilderInfo.getPKGFullPath().toFile().exists() == false) {
					allPkgGood = false;
				}
		
			}
			if (allPkgGood) {
				TreePath configPath = projectTreeViewer.getTreePathForElement(selected);
				if (configPath != null) {
					selectedPath.add(configPath);
				}
				// check the project as well
				TreePath projectPath = projectTreeViewer.getTreePathForElement(selected.getCarbideProject().getProject());
				if (projectPath != null) {
					selectedPath.add(projectPath);
				}
			} else {
				needToUnselect = selected;	// selected configuration have non-exist PKG, need to unselect this later
			}
		}
		if(needToUnselect != null) {
			npiws.selectedBuildConfigList.remove(needToUnselect);
		}
		for (IProject selected : npiws.selectedProjectList) {
			// We deal with this when we set a build config
		}
		for (IPkgEntry entry : npiws.availableAppFileList) {
			pkgListRoot.addPkgEntry(entry);
			// only expanded/visible items are available for selecting programatically
			projectTreeViewer.reveal(entry);
		}
		removeButton.setEnabled(pkgListRoot.getPkgEntries().length != 0);
		// Build TreePath for all selected PKG
		for (IPkgEntry selected : npiws.selectedAppFileList) {
			TreePath pkgPath = projectTreeViewer.getTreePathForElement(selected);
			if (pkgPath != null) {
				selectedPath.add(pkgPath);
			}
		}

		// This shows the change in model(e.g. adding PKG) and force associate of the new tree nodes, so we can select later
		projectTreeViewer.refresh();
		projectTreeViewer.initializeSelectedItems(selectedPath.toArray(new TreePath[selectedPath.size()]));
		// Show the selection
		projectTreeViewer.refresh();
	}

	public void validatePage() {
	}

	@Override
	public void setVisible(boolean visable) {
		super.setVisible(visable);
		// show possible sis builder configuration warnings once during the wizard session
		if(visable && !isSisBuilderConfigurationChecked){
			isSisBuilderConfigurationChecked = true;
			pkgListTreeContentProvider.getCarbideCppProjects(true);
		}
	}
	
}
