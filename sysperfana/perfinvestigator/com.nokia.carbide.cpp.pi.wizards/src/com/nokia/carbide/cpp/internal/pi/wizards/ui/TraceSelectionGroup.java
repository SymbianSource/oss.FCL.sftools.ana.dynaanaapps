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

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;

import com.nokia.carbide.cpp.internal.pi.plugin.model.ITrace;

public class TraceSelectionGroup extends AbstractBaseGroup {

	private CheckboxTableViewer viewerTraceSelection;
	private ProfilerDataPlugins profilerDataPlugins;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            instance of the parent composite
	 * @param wizardSettings
	 *            instance of the INewPIWizardSettings
	 */
	public TraceSelectionGroup(Composite parent,
			INewPIWizardSettings wizardSettings) {
		super(parent, wizardSettings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.carbide.cpp.internal.pi.wizards.ui.AbstractBaseGroup#createContent
	 * ()
	 */
	public void createContent() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));
		this.setText(Messages.getString("TraceSelectionGroup.groupTitle")); //$NON-NLS-1$

		final Composite tablePanel = new Composite(this, SWT.NONE);
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tablePanel.setLayoutData(layoutData);

		viewerTraceSelection = CheckboxTableViewer.newCheckList(tablePanel,
				SWT.BORDER | SWT.FULL_SELECTION);
		viewerTraceSelection
				.setContentProvider(new IStructuredContentProvider() {

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
		viewerTraceSelection.setSorter(new ViewerSorter() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (profilerDataPlugins == null) {
					return 0;
				}
				ITrace trace1 = (ITrace) e1;
				ITrace trace2 = (ITrace) e2;
				int returnCode = 0;
				if (profilerDataPlugins.isMandatory(trace1) == true
						&& profilerDataPlugins.isMandatory(trace2) == true) {
					returnCode = 0;
				} else if (profilerDataPlugins.isMandatory(trace1) == true
						&& profilerDataPlugins.isMandatory(trace2) == false) {
					returnCode = -1;
				} else if (profilerDataPlugins.isMandatory(trace1) == false
						&& profilerDataPlugins.isMandatory(trace2) == true) {
					returnCode = 1;
				} else {
					returnCode = trace1.getTraceTitle().compareTo(
							trace2.getTraceTitle());
				}
				return returnCode;
			}

		});
		Table table = viewerTraceSelection.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		addActions();

		ColumnViewerToolTipSupport.enableFor(viewerTraceSelection,
				ToolTip.NO_RECREATE);

		TableViewerColumn column = new TableViewerColumn(viewerTraceSelection,
				SWT.NONE);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof ITrace) {
					return ((ITrace) element).getTraceTitle();
				}
				return element.toString();
			}

			@Override
			public String getToolTipText(Object element) {
				StringBuilder sb = new StringBuilder(((ITrace) element)
						.getTraceTitle());
				if (((ITrace) element).getTraceId() == 1) {
					sb.append(Messages.getString("TraceSelectionGroup.mandatory")); //$NON-NLS-1$
				}
				return sb.toString();
			}

			@Override
			public Color getForeground(Object element) {
				if (((ITrace) element).getTraceId() == 1) {
					return Display.getCurrent().getSystemColor(
							SWT.COLOR_DARK_GRAY);
				}
				return null;
			}

			@Override
			public Point getToolTipShift(Object object) {
				return new Point(5, 5);
			}

			@Override
			public int getToolTipTimeDisplayed(Object object) {
				return 5000;
			}
		});

		column.getColumn().setText(Messages.getString("TraceSelectionGroup.piView")); //$NON-NLS-1$

		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tablePanel.setLayout(tableColumnLayout);
		tableColumnLayout.setColumnData(column.getColumn(),
				new ColumnWeightData(1));// column weight 1 to fill the whole
		// table width

		viewerTraceSelection.setAllChecked(true);
		viewerTraceSelection.addCheckStateListener(new ICheckStateListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged
			 * (org.eclipse.jface.viewers.CheckStateChangedEvent)
			 */
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked() == false
						&& viewerTraceSelection.getGrayed(event.getElement())) {
					// mandatory view; don't allow to deselect it
					viewerTraceSelection.setChecked(event.getElement(), true);
				} else {
					ITrace plugin = (ITrace) event.getElement();
					profilerDataPlugins.setChecked(plugin, event.getChecked());
				}
			}
		});
	}

	/**
	 * Update group's title
	 * 
	 * @param fileName for the group title
	 */
	private void updateTitle(String fileName){
		if(fileName == null){
			this.setText(Messages.getString("TraceSelectionGroup.groupTitle"));
		}else{
			this.setText(MessageFormat.format(Messages.getString("TraceSelectionGroup.groupTitleFor"), fileName));
		}
		
	}
	
	/**
	 * Add actions 
	 */
	private void addActions() {
		final MenuManager mgr = new MenuManager();
		Action checkAllAction = new Action(Messages.getString("TraceSelectionGroup.actionCheckAll")) {  //$NON-NLS-1$
			@Override
			public void run() {				
				if (profilerDataPlugins != null) {
					profilerDataPlugins.checkAll();	
					viewerTraceSelection.setAllChecked(true);		
				}
			}
		};
		checkAllAction.setEnabled(true);

		Action uncheckAllAction = new Action(Messages.getString("TraceSelectionGroup.actionUncheckAll")) {  //$NON-NLS-1$
			@Override
			public void run() {				
				if (profilerDataPlugins != null) {
					profilerDataPlugins.unCheckAll();	
					for(ITrace trace : profilerDataPlugins.getPlugins()){
						viewerTraceSelection.setChecked(trace, profilerDataPlugins.isChecked(trace));
					}
				}
			}
		};
		uncheckAllAction.setEnabled(true);
		mgr.add(checkAllAction);
		mgr.add(uncheckAllAction);
		viewerTraceSelection.getControl().setMenu(
				mgr.createContextMenu(viewerTraceSelection.getControl()));
	}

	/**
	 * Update trace ids
	 * 
	 * @param profilerDataPlugins
	 */
	public void updateTraceIds(ProfilerDataPlugins profilerDataPlugins) {
		this.profilerDataPlugins = profilerDataPlugins;
		if(profilerDataPlugins == null){
			viewerTraceSelection.setInput(null);
			updateTitle(null);
		}else{			
			viewerTraceSelection.setInput(profilerDataPlugins.getPlugins());
			viewerTraceSelection.setAllChecked(true);
			for (ITrace trace : profilerDataPlugins.getPlugins()) {
				if (profilerDataPlugins.isMandatory(trace)) {
					viewerTraceSelection.setGrayed(trace, true);
				} else {
					viewerTraceSelection.setChecked(trace, profilerDataPlugins
							.isChecked(trace));
				}
			}			
			updateTitle(profilerDataPlugins.getProfilerDataPath().lastSegment());
		}

	}

	@Override
	public Table getTable() {
		return viewerTraceSelection.getTable();
	}
}
