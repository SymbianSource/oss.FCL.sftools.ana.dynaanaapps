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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.nokia.carbide.cpp.internal.pi.analyser.StreamFileParser;
import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.internal.pi.utils.PIUtilities;

/**
 * Base group for the profiler data importers
 * 
 */
public abstract class AbstractBaseGroup extends Group {

	abstract class AbstractBaseSorter extends ViewerSorter {
		Table table;
		int column = 0;
		boolean sortAscending;

		public AbstractBaseSorter(Table table, int defaultColumn) {
			this.table = table;
			doSort(defaultColumn);
		}

		public void doSort(int column) {
			sortAscending = !sortAscending;

			// find the TableColumn corresponding to column, and give it a
			// column direction
			TableColumn sortByColumn = table.getColumn(column);
			if (sortByColumn != null) {
				table.setSortColumn(sortByColumn);
				table.setSortDirection(sortAscending ? SWT.UP : SWT.DOWN);
			}
			this.column = column;
		}

		@Override
		abstract public int compare(Viewer viewer, Object e1, Object e2);

	}

	abstract class AbstractLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	private List<ProfilerDataPlugins> profilerDataFiles = new ArrayList<ProfilerDataPlugins>();
	protected INewPIWizardSettings wizardSettings;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            instance of parent Composite
	 * @param wizardSettings
	 *            instance of the INewPIWizardSettings
	 */
	public AbstractBaseGroup(Composite parent,
			INewPIWizardSettings wizardSettings) {
		super(parent, SWT.NONE);
		this.wizardSettings = wizardSettings;
		// set default layout
		this.setLayout(new GridLayout(1, false));
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		createContent();

	}

	// allow subclassing
	protected void checkSubclass() {
	}

	/**
	 * Implement content for the group
	 */
	protected abstract void createContent();

	protected abstract Table getTable();

	/**
	 * Set this visible and hide given composite
	 * 
	 * @param hideComposite
	 *            to hide
	 */
	public void setVisible(Composite hideComposite) {
		setVisible(true);
		setLocation(hideComposite.getLocation());
		setSize(hideComposite.getSize());
		hideComposite.setVisible(false);
	}

	/**
	 * Add given profile data file into store
	 * 
	 * @param path
	 *            selected profile data file
	 * @throws IllegalArgumentException
	 *             if given file is not valid profile data file
	 */
	public void addProfileDataFile(IPath path) throws IllegalArgumentException {
		boolean exists = false;
		for (ProfilerDataPlugins pdp : profilerDataFiles) {
			if (pdp.getProfilerDataPath().equals(path)) {
				exists = true;
			}
		}
		if (!exists) {
			// check whether selected file a valid profile data file or not
			try {
				File file = path.toFile();
				if (!file.isFile() || file.length() <= 0) {
					throw new IllegalArgumentException();
				}
				new StreamFileParser(file).allTraceType();
				profilerDataFiles.add(new ProfilerDataPlugins(path,
						getPluginsForTraceFile(path)));
			} catch (Exception e) {
				throw new IllegalArgumentException(MessageFormat.format(
						Messages.getString("AbstractBaseGroup.isNotValidProfilerFile"), path //$NON-NLS-1$
								.lastSegment()));
			}
		} else {
			throw new IllegalArgumentException(MessageFormat.format(
					Messages.getString("AbstractBaseGroup.profilerFileIsExisted"), path.lastSegment())); //$NON-NLS-1$
		}
	}

	/**
	 * Add all valid profile data file from given directory's path
	 * 
	 * @param path
	 *            directory
	 * @return
	 */
	public void addDirectory(IPath path) throws IllegalArgumentException {
		if (path != null && path.toFile().isDirectory()) {
			boolean addedValidFile = false;
			try {
				File directory = path.toFile();
				if (!directory.isDirectory()) {
					throw new IllegalArgumentException();
				}
				File[] fileArray = directory.listFiles(new FileFilter() {
					public boolean accept(File file) {
						if (file.isFile()) {
							if (file.getPath().endsWith(".dat")) { //$NON-NLS-1$
								return true;
							}
						}
						return false;
					}
				});

				for (File file : fileArray) {
					try {
						addProfileDataFile(new Path(file.toString()));
						addedValidFile = true;
					} catch (Exception e) {
						// do nothing
					}
				}

			} catch (Exception e) {
				throw new IllegalArgumentException(MessageFormat.format(
						Messages.getString("AbstractBaseGroup.failedToImportFromFolder"), path //$NON-NLS-1$
								.toOSString()));
			}
			if (!addedValidFile) {
				throw new IllegalArgumentException(MessageFormat.format(
						Messages.getString("AbstractBaseGroup.notFoundProfilerDataFiles"), path //$NON-NLS-1$
								.toOSString()));
			}
		}
	}

	/**
	 * Remove selected item form given TableViewer
	 * 
	 * @param tableViewer
	 *            instance of the TableViewer
	 */
	public void removeSelectedItem(TableViewer tableViewer) {
		for (TableItem item : tableViewer.getTable().getSelection()) {
			if (item.getData() instanceof IPath) {
				IPath path = (IPath) item.getData();
				for (ProfilerDataPlugins pdp : profilerDataFiles) {
					if (pdp.getProfilerDataPath().equals(path)) {
						profilerDataFiles.remove(pdp);
						break;
					}
				}
			}
		}
	}

	/**
	 * Remove all item
	 */
	public void removeAll() {
		profilerDataFiles.clear();
	}

	/**
	 * Get list of the ProfilerDataPlugins
	 * 
	 * @return list of the ProfilerDataPlugins
	 */
	public List<ProfilerDataPlugins> getProfilerDataFiles() {
		return profilerDataFiles;
	}

	/**
	 * Get selected item from the table
	 * 
	 * @return instance of the ProfilerDataPlugins if found otherwise null is
	 *         returned
	 */
	public ProfilerDataPlugins getSelectedItem() {
		Table table = getTable();
		if (table.getSelectionCount() == 1) {
			IPath path = (IPath) table.getSelection()[0].getData();
			return getProfilerDataPlugins(path);
		}
		return null;
	}

	/**
	 * Updates given TableViewer so that it contains same data that
	 * ProfilerDataPlugins list.
	 * 
	 * @param tableViewer
	 *            instance of the TableViewer
	 */
	public void refreshTable(TableViewer tableViewer) {
		Table table = tableViewer.getTable();
		List<IPath> pathList = new ArrayList<IPath>();
		for (ProfilerDataPlugins pdp : profilerDataFiles) {
			pathList.add(pdp.getProfilerDataPath());
		}
		tableViewer.setInput(pathList);
		table.setSelection(0);
		tableViewer.refresh();
		wizardSettings.validatePage();
	}

	/**
	 * Get plugins list from given file
	 * 
	 * @param profilerPath profiler data file
	 * @return available plugins list from given file
	 * @throws IOException
	 */
	private List<ITrace> getPluginsForTraceFile(IPath profilerPath)
			throws IOException {
		return PIUtilities.getPluginsForTraceFile(profilerPath.toString());
	}

	/**
	 * Get ProfilerDataPlugins by given profiler data file
	 * 
	 * @param path 
	 * @return instance of the ProfilerDataPlugins if found otherwise null is
	 *         returned
	 */
	private ProfilerDataPlugins getProfilerDataPlugins(IPath path) {
		for (ProfilerDataPlugins pdp : profilerDataFiles) {
			if (pdp.getProfilerDataPath().equals(path)) {
				return pdp;
			}
		}
		return null;
	}
}
