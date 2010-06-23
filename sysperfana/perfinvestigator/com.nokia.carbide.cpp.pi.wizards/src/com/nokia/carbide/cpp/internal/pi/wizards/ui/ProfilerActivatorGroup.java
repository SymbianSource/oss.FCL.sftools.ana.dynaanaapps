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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;
import com.nokia.carbide.cpp.pi.PiPlugin;
import com.nokia.carbide.cpp.pi.export.ITraceProvider;
import com.nokia.carbide.cpp.pi.wizards.WizardsPlugin;
import com.nokia.s60tools.ui.preferences.PreferenceUtils;

/**
 * Provides functionality of the profiler activator
 */
public class ProfilerActivatorGroup extends AbstractBaseGroup {

	private TableViewer profileDataTable;
	private Composite rightButtonsComposite;
	private Label connectionNameInUseLabel;
	private IWizardContainer wizardContainer;
	private NewPIWizardPage wizardPage;
	private TraceHandler traceHandler;
	private PluginSelectionGroup pluginSelectionGroup;
	private Group settingsButtonGroup;
	private Button refreshListButton;
	private Button connectionSettingsButton;
	private Button tracingButton;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            instance of the parent composite
	 * @param wizardSettings
	 *            instance of the INewPIWizardSettings
	 * @param wizardPage
	 *            instance of the NewPIWizardPage
	 * @param wizardContainer
	 *            instance of the IWizardContainer
	 */
	public ProfilerActivatorGroup(Composite parent,
			INewPIWizardSettings wizardSettings, NewPIWizardPage wizardPage,
			IWizardContainer wizardContainer) {
		super(parent, wizardSettings, true);
		this.wizardContainer = wizardContainer;
		this.wizardPage = wizardPage;
		traceHandler = new TraceHandler(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.wizards.ui.AbstractBaseGroup#createContent
	 * ()
	 */
	protected void createContent() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.setText(Messages.getString("NewPIWizardPageInputTask.fromDevice")); //$NON-NLS-1$

		final Composite leftTables = new Composite(this, SWT.NONE);
		leftTables.setLayout(new GridLayout());
		leftTables.setLayoutData(new GridData(GridData.FILL_BOTH));
		pluginSelectionGroup = new PluginSelectionGroup(leftTables,
				wizardSettings, true);

		new Label(leftTables, SWT.NONE).setText(Messages
				.getString("ProfilerActivatorGroup.profilerDataTableTitle")); //$NON-NLS-1$
		profileDataTable = new TableViewer(leftTables, SWT.BORDER | SWT.SINGLE
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);

		TableColumn column = new TableColumn(profileDataTable.getTable(),
				SWT.NONE);
		column.setText(Messages.getString("ProfilerActivatorGroup.columnName")); //$NON-NLS-1$
		column.setWidth(200);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(e.widget instanceof TableColumn))
					return;
				((AbstractBaseSorter) profileDataTable.getSorter()).doSort(0);
				profileDataTable.refresh();

			}
		});

		column = new TableColumn(profileDataTable.getTable(), SWT.NONE);
		column.setText(Messages.getString("ProfilerActivatorGroup.columnTime")); //$NON-NLS-1$
		column.setWidth(120);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(e.widget instanceof TableColumn))
					return;
				((AbstractBaseSorter) profileDataTable.getSorter()).doSort(1);
				profileDataTable.refresh();

			}
		});

		column = new TableColumn(profileDataTable.getTable(), SWT.NONE);
		column.setText(Messages.getString("ProfilerActivatorGroup.columnSize")); //$NON-NLS-1$
		column.setWidth(120);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (!(e.widget instanceof TableColumn))
					return;
				((AbstractBaseSorter) profileDataTable.getSorter()).doSort(2);
				profileDataTable.refresh();

			}
		});

		GridData fileLogsTableGridData = new GridData(GridData.FILL_BOTH);
		fileLogsTableGridData.minimumHeight = 120;
		profileDataTable.getTable().setLayoutData(fileLogsTableGridData);
		profileDataTable.getTable().setHeaderVisible(true);
		profileDataTable.getTable().setLinesVisible(true);
		profileDataTable.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List<?>) {
					return ((List<?>) inputElement).toArray();
				}
				return new Object[0];
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

		});

		profileDataTable.setSorter(new AbstractBaseSorter(profileDataTable
				.getTable(), 0) {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				ProfilerDataPlugins pdp1 = (ProfilerDataPlugins) e1;
				ProfilerDataPlugins pdp2 = (ProfilerDataPlugins) e2;
				int returnCode = 0;
				switch (column) {
				case 0:
					returnCode = pdp1
							.getProfilerDataPath()
							.lastSegment()
							.compareTo(pdp2.getProfilerDataPath().lastSegment());
					break;
				case 1:
					returnCode = compareNumber(pdp1.getTime(), pdp2.getTime());
					break;
				case 2:
					returnCode = compareNumber(pdp1.getSize(), pdp2.getSize());
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
				ProfilerDataPlugins plugins = (ProfilerDataPlugins) element;

				switch (columnIndex) {
				case 0:
					return plugins.getProfilerDataPath().lastSegment();
				case 1:
					SimpleDateFormat formatter = new SimpleDateFormat("mm:ss"); //$NON-NLS-1$
					return formatter.format(new Date(plugins.getTime()));
				case 2:
					return String.valueOf((plugins.getSize() + 512) / 1024);
				default:
					break;
				}
				return ""; //$NON-NLS-1$
			}
		});

		rightButtonsComposite = new Composite(this, SWT.NONE);

		GridData rightButtonsCompositeGridData = new GridData(
				GridData.HORIZONTAL_ALIGN_FILL);
		rightButtonsComposite.setLayoutData(rightButtonsCompositeGridData);
		rightButtonsComposite.setLayout(new GridLayout());

		// Connection settings button
		settingsButtonGroup = new Group(rightButtonsComposite, SWT.NONE);
		settingsButtonGroup.setLayout(new GridLayout());
		settingsButtonGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		refreshListButton = new Button(settingsButtonGroup, SWT.PUSH);
		refreshListButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		refreshListButton.setText(Messages
				.getString("ProfilerActivatorGroup.refreshButtonName")); //$NON-NLS-1$
		refreshListButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePlugins(traceHandler.fetchAvailablePlugins());
			}
		});

		connectionSettingsButton = new Button(settingsButtonGroup, SWT.PUSH);
		connectionSettingsButton.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		connectionSettingsButton
				.setText(Messages
						.getString("ProfilerActivatorGroup.connectionSettingsButtonName")); //$NON-NLS-1$
		connectionSettingsButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Shell shell = PiPlugin.getCurrentlyActiveWbWindowShell();
				PreferenceUtils.openPreferencePage(PiPlugin.getTraceProvider()
						.getTraceSourcePreferencePageId(), shell);
				updateConnectionText();
			}
		});
		// Connection settings labels
		Label connectionTextLabel = new Label(settingsButtonGroup, SWT.LEFT);
		connectionTextLabel.setText(Messages
				.getString("ProfilerActivatorGroup.currentlyUsingTitle")); //$NON-NLS-1$
		connectionTextLabel.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_CENTER));

		connectionNameInUseLabel = new Label(settingsButtonGroup, SWT.NONE);
		connectionNameInUseLabel.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_CENTER));
		Group traceButtonGroup = new Group(rightButtonsComposite, SWT.NONE);
		traceButtonGroup.setLayout(new GridLayout());
		traceButtonGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		new Label(traceButtonGroup, SWT.LEFT).setText(Messages
				.getString("ProfilerActivatorGroup.filePrefixTitle")); //$NON-NLS-1$
		final Text filePrefixText = new Text(traceButtonGroup, SWT.BORDER);
		filePrefixText.setText("PIProfiler_#"); //$NON-NLS-1$
		filePrefixText.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Start/Stop tracing button
		tracingButton = new Button(traceButtonGroup, SWT.PUSH);
		tracingButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tracingButton.setText(Messages
				.getString("ProfilerActivatorGroup.startTracingButtonName")); //$NON-NLS-1$
		tracingButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (PiPlugin.getTraceProvider().isListening()) {
					addTraceFile(traceHandler.stopTrace());
				} else {
					traceHandler.startTrace(filePrefixText.getText(),
							pluginSelectionGroup.getSelectedPluginIds());
				}
				updateButtons();
			}
		});

	}

	/**
	 * Compare two long number
	 * 
	 * @param one
	 * @param two
	 * @return result of the comparison
	 */
	private int compareNumber(long one, long two) {
		if (one > two) {
			return 1;
		} else if (one < two) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * updateConnectionText updates connection text to match used settings
	 */
	private void updateConnectionText(boolean forceUpdate) {
		traceHandler.updateCurrenConnection();
		if (pluginSelectionGroup.getSelectedPluginIds().length <= 0
				|| forceUpdate) {
			updatePlugins(traceHandler.fetchAvailablePlugins());
		}
	}
	
	/**
	 * updateConnectionText updates connection text to match used settings
	 */
	private void updateConnectionText() {
		traceHandler.updateCurrenConnection();
	}

	/**
	 * Set current connection name
	 * 
	 * @param connection
	 *            the current connection name
	 */
	public void setCurrentConnection(final String connection) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				connectionNameInUseLabel.setText(connection);
				getParent().layout(true, true);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.wizards.ui.AbstractBaseGroup#getTable()
	 */
	@Override
	public Table getTable() {
		return profileDataTable.getTable();
	}

	@Override
	public IStatus validateContent(NewPIWizard wizardPage) {
		List<ProfilerDataPlugins> dataFiles = getProfilerDataFiles();
		if (dataFiles != null && dataFiles.size() > 0) {
			wizardPage.setProfilerDataFiles(getProfilerDataFiles());
			return Status.OK_STATUS;
		} else {
			return new Status(
					Status.INFO,
					WizardsPlugin.PLUGIN_ID,
					Messages
							.getString("ProfilerActivatorGroup.profilerGroupDescription")); //$NON-NLS-1$
		}

	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			updateConnectionText(false);
		}
		super.setVisible(visible);

	}

	private void updatePlugins(final List<ITrace> plugins) {
		pluginSelectionGroup.updateTraceIds(new ProfilerDataPlugins(
				new Path(""), plugins)); //$NON-NLS-1$
		updateButtons();
	}

	/**
	 * Add given trace data file to profiler data file table
	 * 
	 * @param path
	 */
	private void addTraceFile(final IPath path) {
		if (path == null) {
			return;
		}
		ProfilerDataPlugins dataPlugins = removeWithPath(path
				.removeFileExtension().addFileExtension(
						ITraceProvider.BASE_FILE));
		if (dataPlugins != null) {
			try {
				addProfilerDataFile(path, dataPlugins.getTime(), path.toFile()
						.length());
				refreshTable(profileDataTable, true);
			} catch (Exception e) {
				wizardPage
						.setErrorMessage(e.getMessage()
								+ " " + Messages.getString("ProfilerActivatorGroup.tryDeselectPluginsFromList")); //$NON-NLS-1$ //$NON-NLS-2$
				wizardPage.setPageComplete(false);
				refreshTable(profileDataTable, false);
				path.toFile().delete();
			}
		}
	}

	public NewPIWizardPage getWizardPage() {
		return wizardPage;
	}

	public IWizardContainer getWizardContainer() {
		return wizardContainer;
	}

	/**
	 * Update trace data files
	 * 
	 * @param path
	 * @param time
	 * @param size
	 */
	public void updateTraceDataFile(final IPath path, final long time,
			final long size) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				// remove if both time and size are -1
				if (time == -1 && size == -1) {
					removeWithPath(path);
				} else {
					updateProfilerDataFile(path, time, size);
				}
				refreshTable(profileDataTable, false);
			}
		});
	}

	/**
	 * Update buttons
	 */
	public void updateButtons() {
		if (PiPlugin.getTraceProvider().isListening()) {
			wizardPage.setErrorMessage(null);
			refreshListButton.setEnabled(false);
			connectionSettingsButton.setEnabled(false);
			((NewPIWizardPageInputTask) wizardPage)
					.setButtonGroupEnabled(false);
			tracingButton.setText(Messages
					.getString("ProfilerActivatorGroup.stopTracingButtonName")); //$NON-NLS-1$
		} else {
			refreshListButton.setEnabled(true);
			connectionSettingsButton.setEnabled(true);
			((NewPIWizardPageInputTask) wizardPage).setButtonGroupEnabled(true);
			tracingButton
					.setText(Messages
							.getString("ProfilerActivatorGroup.startTracingButtonName")); //$NON-NLS-1$
		}

		if (pluginSelectionGroup.getSelectedPluginIds().length <= 0) {
			tracingButton.setEnabled(false);
		} else {
			tracingButton.setEnabled(true);
		}
	}

	/**
	 * Generate next dat file name
	 * 
	 * @param path
	 * @return
	 */
	private IPath generateDatFile(IPath path) {
		while (path.toFile().exists()) {
			String file = path.lastSegment();
			String fileExtension = path.getFileExtension();
			file = file.substring(0, file.lastIndexOf("." + fileExtension)); //$NON-NLS-1$
			int index = file.lastIndexOf("_"); //$NON-NLS-1$
			if (index != -1) {
				try {
					int count = Integer.valueOf(file.substring(index + 1));
					count++;
					file = file.substring(0, index + 1) + count;
					path = path.removeLastSegments(1).append(
							file + "." + fileExtension); //$NON-NLS-1$
				} catch (Exception e) {
					path = path.removeLastSegments(1).append(
							file + "_0." + fileExtension); //$NON-NLS-1$
				}

			} else {
				path = path.removeLastSegments(1).append(
						file + "_0." + fileExtension); //$NON-NLS-1$
			}
		}
		return path;

	}

	/**
	 * Handle temporary profiler data files in case user is tried to close
	 * wizard and user has traced some data from device
	 * 
	 * @param forceRemove
	 */
	public void handleTemporaryProfilerDataFiles(boolean forceRemove) {
		final List<ProfilerDataPlugins> plugins = new ArrayList<ProfilerDataPlugins>();
		plugins.addAll(getProfilerDataFiles());
		getProfilerDataFiles().clear();
		if (forceRemove) {
			deleteProfilerDataFiles(plugins);
		} else if (plugins.size() > 0) {
			boolean answer = MessageDialog
					.openQuestion(
							Display.getDefault().getActiveShell(),
							Messages
									.getString("ProfilerActivatorGroup.questionDialogTitle"), Messages.getString("ProfilerActivatorGroup.questionDialogMessage")); //$NON-NLS-1$ //$NON-NLS-2$
			if (answer) {
				// open file dialog for selecting a crash file
				DirectoryDialog dialog = new DirectoryDialog(Display
						.getDefault().getActiveShell());
				dialog
						.setText(Messages
								.getString("ProfilerActivatorGroup.directorySelectionDialogTitle")); //$NON-NLS-1$
				final String result = dialog.open();
				if (result != null) {
					WorkspaceJob job = new WorkspaceJob(
							Messages
									.getString("ProfilerActivatorGroup.movingProfilerDataFilesJob") + result) { //$NON-NLS-1$
						@Override
						public IStatus runInWorkspace(IProgressMonitor monitor)
								throws CoreException {
							monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
							for (ProfilerDataPlugins plugin : plugins) {
								IPath dataFile = plugin.getProfilerDataPath();
								IPath targetFile = generateDatFile(new Path(
										result).append(dataFile.lastSegment()));
								try {
									moveFile(dataFile, targetFile,
											new SubProgressMonitor(monitor,
													IProgressMonitor.UNKNOWN));
								} catch (Exception e) {
									List<ProfilerDataPlugins> list = new ArrayList<ProfilerDataPlugins>();
									list.add(plugin);
									deleteProfilerDataFiles(list);
									e.printStackTrace();
									MessageDialog
											.openError(
													getShell(),
													Messages
															.getString("ProfilerActivatorGroup.errorDialogTitle"), MessageFormat.format(Messages.getString("ProfilerActivatorGroup.failedToMoveFileTo"), dataFile.lastSegment(), result)); //$NON-NLS-1$ //$NON-NLS-2$
								}
							}
							monitor.done();
							return Status.OK_STATUS;
						}
					};
					job.schedule();
				} else {
					deleteProfilerDataFiles(plugins);
				}
			} else {
				deleteProfilerDataFiles(plugins);
			}
		}
	}

	/**
	 * Delete profiler data files
	 * 
	 * @param plugins
	 */
	private void deleteProfilerDataFiles(final List<ProfilerDataPlugins> plugins) {
		WorkspaceJob job = new WorkspaceJob(Messages
				.getString("ProfilerActivatorGroup.removeProfilerDataFilesJob")) { //$NON-NLS-1$

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor)
					throws CoreException {
				monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
				for (ProfilerDataPlugins plugin : plugins) {
					IPath dataFile = plugin.getProfilerDataPath();
					monitor
							.setTaskName(MessageFormat
									.format(
											Messages
													.getString("ProfilerActivatorGroup.deletingProfilerDataFiles"), dataFile.lastSegment())); //$NON-NLS-1$
					if (dataFile.toFile().exists()) {
						while (!dataFile.toFile().delete()) {
							if (monitor.isCanceled()) {
								break;
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}

				}
				;
				monitor.done();
				return Status.OK_STATUS;
			}

		};
		job.schedule();

	}

	/**
	 * Move profiler data file from temporary location to user selected target
	 * 
	 * @param source
	 * @param target
	 * @param monitor
	 * @throws IOException
	 */
	private void moveFile(final IPath source, IPath target,
			IProgressMonitor monitor) throws IOException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			monitor
					.beginTask(
							MessageFormat
									.format(
											Messages
													.getString("ProfilerActivatorGroup.copyingProfilerDataFiles"), source.lastSegment(), target.removeLastSegments(1)), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			in = new FileInputStream(source.toFile());
			out = new FileOutputStream(target.toFile());
			int bufferSize = 1024;
			byte[] buf = new byte[bufferSize];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				out.flush();
			}

		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			monitor
					.setTaskName(MessageFormat
							.format(
									Messages
											.getString("ProfilerActivatorGroup.deletingProfilerDataFiles"), source.lastSegment())); //$NON-NLS-1$
			while (!source.toFile().delete()) {
				if (monitor.isCanceled()) {
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			monitor.done();
		}
	}
}
