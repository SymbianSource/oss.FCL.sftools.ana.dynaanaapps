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

package com.nokia.carbide.cpp.internal.pi.button.ui;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nokia.carbide.cpp.pi.button.IBupEventMap;
import com.nokia.carbide.cpp.pi.button.IBupEventMapEditable;

public class BupMapTableViewer extends TableViewer {
	// Set the table column property names
	private final String KEY_CODE_COLUMN 		= Messages.getString("BupMapTableViewer.keycode"); //$NON-NLS-1$
	private final String ENUM_COLUMN 			= Messages.getString("BupMapTableViewer.enum"); //$NON-NLS-1$
	private final String LABEL_COLUMN 			= Messages.getString("BupMapTableViewer.label"); //$NON-NLS-1$
	private static final FontRegistry fontRegistry = new FontRegistry();
	private static Font regularFont = fontRegistry.get(""); //$NON-NLS-1$
	private static Font boldFont = fontRegistry.getBold(""); //$NON-NLS-1$
	private TableColumn columnKeyCode;
	private TableColumn columnEnum;
	private TableColumn columnLabel;
	
	// Set column names
	private String[] columnNames = new String[] { 
			KEY_CODE_COLUMN, 
			ENUM_COLUMN,
			LABEL_COLUMN
			};
	
	public class BupMapEntry {
		public int keyCode;
		public String enumString;
		public String label;
		public boolean modified;
	}

	private ArrayList<BupMapEntry> mapList = new ArrayList<BupMapEntry>();
	private BupMapEntrySorter mapEntrySorter = new BupMapEntrySorter();
	public BupMapTableViewer mapTableViewer = this;
	boolean modifible = false;
	
	public class BupMapEntrySorter extends ViewerSorter {
		// public constants indicating sorting type
		public final static int SORT_BY_KEYCODE = 0;
		public final static int SORT_BY_ENUM = 1;
		public final static int SORT_BY_LABEL = 2;
		
		TableColumn columnCurrentSort = columnKeyCode;
		int dirKeyCode = SWT.UP;
		int dirEnum = SWT.UP;
		int dirLabel = SWT.UP;
		
		public BupMapEntrySorter () {
			super();
		}
		
		// only sort by keyCode
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (!(o1 instanceof BupMapEntry) ||
					!(o2 instanceof BupMapEntry)) {
				return 0;
			}
			
			// ask table for sorting column criterion
			TableColumn tableColumn = ((TableViewer)viewer).getTable().getSortColumn();
			int result = 0;
			if (tableColumn == columnKeyCode) {
				result = new Integer(((BupMapEntry)o1).keyCode).compareTo(((BupMapEntry)o2).keyCode);
				if (dirKeyCode != SWT.UP) {
					result *= -1;
				}
			} else if (tableColumn == columnEnum) {
				result = ((BupMapEntry)o1).enumString.compareTo(((BupMapEntry)o2).enumString);
				if (dirEnum != SWT.UP) {
					result *= -1;
				}
			}else if (tableColumn == columnLabel) {
				result = ((BupMapEntry)o1).label.compareTo(((BupMapEntry)o2).label);
				if (dirLabel != SWT.UP) {
					result *= -1;
				}
			}

			return result;
		}

		/**
		 * @param currentSortColumn
		 */
		public void setSortCriteria(TableColumn columnNewSort) {

			if (columnCurrentSort != columnNewSort) {
				// update sorting column criterion to table
				mapTableViewer.getTable().setSortColumn(columnNewSort);
				columnCurrentSort = columnNewSort;
			} else {
				// update sorting order we keep locally, I wish there was getSortDirection(TableColumn)
				if (columnNewSort == columnKeyCode) {
					dirKeyCode = dirKeyCode == SWT.UP ? SWT.DOWN : SWT.UP;
				} else if (columnNewSort == columnEnum) {
					dirEnum = dirEnum == SWT.UP ? SWT.DOWN : SWT.UP;
				}else if (columnNewSort == columnLabel) {
					dirLabel = dirLabel == SWT.UP ? SWT.DOWN : SWT.UP;
				}
			}
			if (columnNewSort == columnKeyCode) {
				mapTableViewer.getTable().setSortDirection(dirKeyCode);
			} else if (columnNewSort == columnEnum) {
				mapTableViewer.getTable().setSortDirection(dirEnum);
			}else if (columnNewSort == columnLabel) {
				mapTableViewer.getTable().setSortDirection(dirLabel);
			}
		}
		
	}
	
	public class BupMapTableLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object arg0, int arg1) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			String result = ""; //$NON-NLS-1$
			BupMapEntry mapEntry = (BupMapEntry) element;
			
			switch(columnIndex) {
				case 0:
					result = Integer.toHexString(mapEntry.keyCode);
					break;
				case 1:
					result = mapEntry.enumString;
					break;
				case 2:
					result = mapEntry.label;
					break;
			}
			return result;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
		 */
		public Font getFont(Object element) {
			if (((BupMapEntry)element).modified) {
				return boldFont;
			}
			return regularFont;
		}
	}

	/**
	 * @param parent
	 */
	public BupMapTableViewer(Composite parent, boolean modify) {
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | (modify ? SWT.FULL_SELECTION | SWT.SINGLE : SWT.NONE));
		final int KEYCODE_WIDTH = 60;
		final int ENUM_WIDTH = 150;
		final int LABEL_WIDTH = 200;
		
		modifible = modify;
		
		setSorter(mapEntrySorter);
		
		SelectionListener columnSelectinListener = new SelectionListener () {

			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
		        // determine new sort column and direction
		        TableColumn currentSortColumn = (TableColumn) arg0.widget;
		        
		        mapEntrySorter.setSortCriteria(currentSortColumn);
		        
		        // sort the data based on column and direction criteria, sort well pick that up from table
		        mapTableViewer.getSorter().sort(mapTableViewer, mapList.toArray());
		        mapTableViewer.setInput(mapList.toArray());
		    }
		};
		
		columnKeyCode = new TableColumn(getTable(), SWT.LEFT, 0);	
		columnKeyCode.setText(Messages.getString("BupMapTableViewer.hex")); //$NON-NLS-1$
		columnKeyCode.setWidth(60);
		columnKeyCode.addSelectionListener (columnSelectinListener);

		columnEnum = new TableColumn(getTable(), SWT.LEFT, 1);	
		columnEnum.setText(Messages.getString("BupMapTableViewer.tKeyCode")); //$NON-NLS-1$
		columnEnum.setWidth(150);
		columnEnum.addSelectionListener (columnSelectinListener);
		
		columnLabel = new TableColumn(getTable(), SWT.LEFT, 2);	
		columnLabel.setText(Messages.getString("BupMapTableViewer.Label")); //$NON-NLS-1$
		columnLabel.setWidth(200);
		columnLabel.addSelectionListener (columnSelectinListener);
		
		setColumnProperties(columnNames);

		Table mappingTable = getTable();
		// initial size, so scroll bar show up
		GridDataFactory.fillDefaults().grab(true, true).hint(KEYCODE_WIDTH + ENUM_WIDTH + LABEL_WIDTH - 15, 200).applyTo(mappingTable);
		mappingTable.setHeaderVisible(true);
		mappingTable.setLinesVisible(true);
		
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new BupMapTableLabelProvider());
		
		setInput(mapList.toArray(new BupMapEntry[mapList.size()]));
	}
	
	public void loadMap(IBupEventMap map) {
		mapList.clear();
		Set<Integer> keyCodeSet = map.getKeyCodeSet();
		BupMapEntry entry;
		for (Integer keyCode : keyCodeSet) {
			entry = new BupMapEntry();
			entry.keyCode = keyCode.intValue();
			entry.enumString = map.getEnum(keyCode);
			entry.label = map.getLabel(keyCode);
			if (map instanceof IBupEventMapEditable) {
				entry.modified = ((IBupEventMapEditable)map).isModified(keyCode);
			} else {
				entry.modified = false;
			}
			mapList.add(entry);
		}
		setInput(mapList.toArray(new BupMapEntry[mapList.size()]));
	}
}
