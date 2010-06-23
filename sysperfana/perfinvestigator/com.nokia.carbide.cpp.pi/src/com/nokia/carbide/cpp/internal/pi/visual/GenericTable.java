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

package com.nokia.carbide.cpp.internal.pi.visual;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.actions.SaveSamples;
import com.nokia.carbide.cpp.internal.pi.actions.SetThresholdsDialog;
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveTable;
import com.nokia.carbide.cpp.internal.pi.model.ProfiledGeneric;
import com.nokia.carbide.cpp.internal.pi.save.SaveTableWizard;


public abstract class GenericTable
{
	// table column IDs in hopes it's quicker than comparing characters
	protected static final int COLUMN_ID_THREAD         =  1;
	protected static final int COLUMN_ID_BINARY         =  2;
	protected static final int COLUMN_ID_FUNCTION       =  3;
	protected static final int COLUMN_ID_SHOW           =  4;
	protected static final int COLUMN_ID_PERCENT_LOAD   =  5;
//	protected static final int COLUMN_ID_COLOR          =  6;
	protected static final int COLUMN_ID_SAMPLE_COUNT   =  7;
	protected static final int COLUMN_ID_PATH           =  8;
	protected static final int COLUMN_ID_IN_BINARY      =  9;
	protected static final int COLUMN_ID_IN_BINARY_PATH = 10;
	protected static final int COLUMN_ID_START_ADDR     = 11;
	protected static final int COLUMN_ID_PRIORITY       = 12;

	
	// these are never used as actual column headers - they indicate a double sort
	protected static final int COLUMN_ID_FULL_PATH      = 13;
	protected static final int COLUMN_ID_FULL_IN_PATH   = 14;

	public static final int COLUMN_ID_SWI_THREAD		= 15;
	public static final int COLUMN_ID_ADDRESS			= 16;
	public static final int COLUMN_ID_SWI_FUNCTION		= 17;
	public static final int COLUMN_ID_RETURN_ADDRESS	= 18;
	public static final int COLUMN_ID_SWI_COUNT			= 19;
	public static final int COLUMN_ID_IRQ_LINE			= 20;
	public static final int COLUMN_ID_IRQ_COUNT			= 21;
	public static final int COLUMN_ID_SWI_CHECK			= 22;

	
	protected static final int COLOR_COLUMN_INDEX = 0;
	
	// table column headings
	protected static final String COLUMN_HEAD_THREAD       = Messages.getString("GenericTable.thread"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_BINARY       = Messages.getString("GenericTable.binary"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_FUNCTION     = Messages.getString("GenericTable.function"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_SHOW         = Messages.getString("GenericTable.show");	// square root symbol used as check symbol //$NON-NLS-1$
	protected static final String COLUMN_HEAD_PERCENT_LOAD = Messages.getString("GenericTable.load"); //$NON-NLS-1$
//	protected static final String COLUMN_HEAD_COLOR        = "Color";
	public    static final String COLUMN_HEAD_SAMPLE_COUNT = Messages.getString("GenericTable.samples"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_PATH         = Messages.getString("GenericTable.path"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_IN_BINARY    = Messages.getString("GenericTable.inBinary"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_IN_BINARY_PATH = Messages.getString("GenericTable.pathOfBinary"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_START_ADDR   = Messages.getString("GenericTable.startAddr"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_PRIORITY     = Messages.getString("GenericTable.priority"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_MEMORY_CHUNKS = Messages.getString("GenericTable.chunks"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_MEMORY_STACK  = Messages.getString("GenericTable.stackHeap"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_MEMORY_TOTAL  = Messages.getString("GenericTable.memoryTotal"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_MEMORY_NAME   = Messages.getString("GenericTable.threadProcess"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_LIBRARY_NAME   = Messages.getString("GenericTable.libraryName"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_LIBRARY_LOAD_SIZE = Messages.getString("GenericTable.libraryLoadSize"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_LIBRARY_SELECTION_LOAD_COUNT = Messages.getString("GenericTable.librarySelectionLoadCount"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_GRAPHICS_MEMORY_NAME   = Messages.getString("GenericTable.process"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_GRAPHICS_MEMORY_PRIVATE = Messages.getString("GenericTable.private"); //$NON-NLS-1$
	protected static final String COLUMN_HEAD_GRAPHICS_MEMORY_SHARED  = Messages.getString("GenericTable.shared"); //$NON-NLS-1$
	
	// table column widths
	protected static final int COLUMN_WIDTH_SHOW           =  40;
	protected static final int COLUMN_WIDTH_PERCENT_LOAD   =  52;
//	protected static final int COLUMN_WIDTH_COLOR_MIN      =  22;
//	protected static final int COLUMN_WIDTH_COLOR_MAX      =  60;
    protected static final int COLUMN_WIDTH_THREAD_NAME    = 300;
    protected static final int COLUMN_WIDTH_FUNCTION_NAME  = 300;
    protected static final int COLUMN_WIDTH_BINARY_NAME    = 130;
    protected static final int COLUMN_WIDTH_PATH           = 250;
    protected static final int COLUMN_WIDTH_START_ADDRESS  =  80;
    protected static final int COLUMN_WIDTH_SAMPLE_COUNT   =  84;
    protected static final int COLUMN_WIDTH_HEAP           =  80;
    protected static final int COLUMN_WIDTH_STACK          =  80;
    protected static final int COLUMN_WIDTH_PRIORITY       =  85;
    protected static final int COLUMN_WIDTH_IN_BINARY      =  65;
    protected static final int COLUMN_WIDTH_IN_BINARY_PATH =  80;
    protected static final int COLUMN_WIDTH_8_DIGIT_HEX    = 100;
	protected static final int COLUMN_WIDTH_MEMORY_CHUNKS  =  80;
	protected static final int COLUMN_WIDTH_MEMORY_STACK   =  80;
	protected static final int COLUMN_WIDTH_GRAPHICS_MEMORY_PRIVATE  =  80;
	protected static final int COLUMN_WIDTH_GRAPHICS_MEMORY_SHARED   =  80;
	protected static final int COLUMN_WIDTH_MEMORY_TOTAL   =  80;
	protected static final int COLUMN_WIDTH_MEMORY_NAME    	= 300;
	protected static final int COLUMN_WIDTH_GRAPHICS_MEMORY_NAME    	= 300;
	protected static final int COLUMN_WIDTH_THREAD_IRQ_LINE = 300;
	protected static final int COLUMN_WIDTH_LIBRARY_LOAD_SIZE  =  80;
	protected static final int COLUMN_WIDTH_LIBRARY_NAME    = 250;
    protected static final int COLUMN_WIDTH_LIBRARY_SELECTION_COUNT   =  160;
    protected static final int COLUMN_WIDTH_ADDRESS_COUNT 	= 100;
	protected static final int COLUMN_WIDTH_RETURN_ADDRESS 	= 90;
	protected static final int COLUMN_WIDTH_COUNT 			= 50;
	protected static final int COLUMN_WIDTH_SWI_FUNCTION 	= 250;
	protected static final int COLUMN_WIDTH_CHECK_COLUMN	= 40;

	
    // colors are now included with the checkbox column
    protected static final int COLOR_COLUMN = 0;
    
	// NOTE: This text value must be unique to this column, but not
	// discernible to the eye. It is used to find the checkbox column
    // when you have a table item without its column number
    protected static final String SHOW_ITEM_VALUE = Messages.getString("GenericTable.space"); //$NON-NLS-1$
    
    // type of checkbox column - none, just a checkbox, checkbox with visible text
    protected static final int CHECKBOX_NONE    = 0;
    protected static final int CHECKBOX_NO_TEXT = 1;
    protected static final int CHECKBOX_TEXT    = 2;
    
    // create a CheckboxTableViewer, which contains a Table
	protected CheckboxTableViewer tableViewer;
    protected Table table;

    protected Vector<ProfiledGeneric> tableItemData = new Vector<ProfiledGeneric>();
    protected Vector<Object> columnData    = new Vector<Object>();
    
    // provide a routine for actions relating to the table
    abstract public void action(String actionString);
	
	// pop-up menu for table
	protected Menu contextMenu;
	
	// class for matching samples against checked items in an table
	public class AllInclude {
		public int totalItems;					// total number of threads, binaries, etc.
		public ArrayList<Object> list = null;	// threads, binaries, etc. checked
	}

	//optimized for performance, grrr
	Object[] finalValues = null;
	public Object[] getSelectedValues()
	{
 		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				if ((table == null) || (table.isDisposed()))
					finalValues = new Object[0];
				else {
					Object[] values = new Object[table.getSelectionCount()];
					TableItem[] selectedItems = table.getSelection();
					for (int i = 0; i < values.length; i++)
					{
					    values[i] = selectedItems[i].getData();
					}
					
					finalValues = values;
				}
			}
		});
		return finalValues;
	}
    
    //Only used in custom traces
    public Object[] getSelectedValueString()
	{
		if (table == null)
			return new Object[0];
		
		Object[] values = new Object[table.getSelectionCount()];
		TableItem[] selectedItems = table.getSelection();
		for (int i = 0; i < values.length; i++)
		{
			if (selectedItems[i].getData() instanceof ProfiledGeneric) {
				values[i] = ((ProfiledGeneric)selectedItems[i].getData()).getNameString();
			}
		}
		return values;
	}
    
	public void setSelectedIndices(int[] indices)
	{
		if ((indices == null) || (table == null))
			return;

	    // only select the indicated indices
        table.deselectAll();
        for (int j = 0; j < indices.length; j++)
        {
            table.select(indices[j]);
        }
	}
	
	public void setSelectedIndicesXOR(int[] indices)
	{
		if ((indices == null) || (table == null))
			return;

		// reverse the selection status of the indicated indices
        for (int j = 0; j < indices.length; j++)
        {
            if (table.isSelected(indices[j]))
            {
                table.deselect(indices[j]);
            }
            else
            {
                table.select(indices[j]);
            }
        }
	}
	
	public Integer getIndex(Object anObject)
	{
		if (anObject == null)
			return null;

	    int counter = 0;
	    Enumeration<ProfiledGeneric> e = tableItemData.elements();
	    while (e.hasMoreElements())
	    {
	        if (e.nextElement().equals(anObject))
	        {
	            return Integer.valueOf(counter);
	        }
	        counter++;
	    }
	    return null;
	}
	
	public int[] getIndices(Object[] objects)
	{
	    if (objects == null)
	    	return null;
	    
	    int indexCount = 0;
        int[] indices = new int[objects.length];
	    for (int i = 0; i < objects.length; i++)
	    {
	        Integer index = getIndex(objects[i]);
	        if (index != null)
	        {
	            indices[i] = index.intValue();
	            indexCount++;
	        }
	    }
	    if (indexCount != 0)
	        return indices;
	    else
	        return null;
	}
	
	public Menu getTablePopupMenu(Decorations parent, boolean enableSelected) {
	    Menu tablePopupMenu = new Menu(parent, SWT.POP_UP);

	    getCheckRows(tablePopupMenu, enableSelected);
		new MenuItem(tablePopupMenu, SWT.SEPARATOR);
		getRecolorItem(tablePopupMenu, Messages.getString("GenericTable.items"), enableSelected); //$NON-NLS-1$
		new MenuItem(tablePopupMenu, SWT.SEPARATOR);
		getChangeThresholds(tablePopupMenu);
		tablePopupMenu.setVisible(true);

		return tablePopupMenu;
	}

	public class TableKeyListener implements KeyListener {

		// listener for ctrl-A (select all rows), ctrl-C (copy highlighted rows to clipboard), etc.
		public void keyPressed(KeyEvent event) {
			// check for exact match of CTRL-A/CTRL-C/etc. (do not match CTRL-SHIFT-A, for example)
			if (event.stateMask == SWT.CTRL) {
				if (event.character == 1) { // Ctrl-A
					action("selectAll"); //$NON-NLS-1$
				} else if (event.character == 3) { // Ctrl-C
					action("copy"); //$NON-NLS-1$
				}
			}
		}

		public void keyReleased(KeyEvent event) {
		}
		
	}

	protected MenuItem getSortFullPathItem(Menu menu, boolean enabled) {
	    MenuItem fullPathItem = new MenuItem(menu, SWT.PUSH);

	    fullPathItem.setText(Messages.getString("GenericTable.sortByPath")); //$NON-NLS-1$
		fullPathItem.setEnabled(enabled);

		if (enabled) {
			fullPathItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("sortfullpath"); //$NON-NLS-1$
				}
			});
		}

		return fullPathItem;
	}

	protected MenuItem getSortInFullPathItem(Menu menu, boolean enabled) {
	    MenuItem fullPathItem = new MenuItem(menu, SWT.PUSH);

		fullPathItem.setText(Messages.getString("GenericTable.sortByInPath")); //$NON-NLS-1$
		fullPathItem.setEnabled(enabled);
		
		if (enabled) {
			fullPathItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("sortinfullpath"); //$NON-NLS-1$
				}
			});
		}

		return fullPathItem;
	}

	protected MenuItem getSelectAllItem(Menu menu, boolean enabled) {
	    MenuItem selectAllItem = new MenuItem(menu, SWT.PUSH);
	    
		selectAllItem.setText(Messages.getString("GenericTable.selectAll")); //$NON-NLS-1$
		selectAllItem.setEnabled(enabled);
		
		if (enabled) {
			selectAllItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("selectAll"); //$NON-NLS-1$
				}
			});
		}
		
		return selectAllItem;
	}

	protected MenuItem getCopyItem(Menu menu, boolean enabled) {
	    MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
	    
		copyItem.setText(Messages.getString("GenericTable.copy")); //$NON-NLS-1$
		copyItem.setEnabled(enabled);
		
		if (enabled) {
			copyItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("copy"); //$NON-NLS-1$
				}
			});
		}
		
		return copyItem;
	}

	protected MenuItem getCopyTableItem(Menu menu, boolean enabled) {
	    MenuItem copyTableItem = new MenuItem(menu, SWT.PUSH);

		copyTableItem.setText(Messages.getString("GenericTable.copyTable")); //$NON-NLS-1$
		copyTableItem.setEnabled(enabled);
		
		if (enabled) {
			copyTableItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
					action("copyTable"); //$NON-NLS-1$
				}
			});
		}
	
		return copyTableItem;
	}
	
	protected MenuItem getSaveTableItem(Menu menu, boolean enabled) {
	    MenuItem saveTableItem = new MenuItem(menu, SWT.PUSH);

		saveTableItem.setText(Messages.getString("GenericTable.saveTable")); //$NON-NLS-1$
		saveTableItem.setEnabled(enabled);
		
		if (enabled) {
			saveTableItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
					action("saveTable"); //$NON-NLS-1$
				}
			});
		}
	
		return saveTableItem;
	}

	private MenuItem getAddRows(Menu menu, boolean enabled) {
	    MenuItem addRows = new MenuItem(menu, SWT.PUSH);
	    
		addRows.setText(Messages.getString("GenericTable.checkHighlighted")); //$NON-NLS-1$
		addRows.setEnabled(enabled);

		if (enabled) {
			addRows.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("add"); //$NON-NLS-1$
				}
			});
		}

		return addRows;
	}

	private MenuItem getRemoveRows(Menu menu, boolean enabled) {
	    MenuItem removeRows = new MenuItem(menu, SWT.PUSH);
	    
		removeRows.setText(Messages.getString("GenericTable.uncheckHighlighted")); //$NON-NLS-1$
		removeRows.setEnabled(enabled);
		
		if (enabled) {
			removeRows.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("remove"); //$NON-NLS-1$
				}
			});
		}

		return removeRows;
	}

	private MenuItem getAddAllRows(Menu menu) {
	    MenuItem addAllRows = new MenuItem(menu, SWT.PUSH);
	    
		addAllRows.setText(Messages.getString("GenericTable.checkAll")); //$NON-NLS-1$
		addAllRows.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("addall"); //$NON-NLS-1$
			}
		});
		return addAllRows;
	}

	private MenuItem getRemoveAllRows(Menu menu) {
	    MenuItem removeAllRows = new MenuItem(menu, SWT.PUSH);

		removeAllRows.setText(Messages.getString("GenericTable.uncheckAll")); //$NON-NLS-1$
		removeAllRows.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("removeall"); //$NON-NLS-1$
			}
		});
		return removeAllRows;
	}

	public void getCheckRows(Menu menu, boolean enableSelected) {
		// check highlighted items
		getAddRows(menu, enableSelected);
		
		// uncheck highlighted items
		getRemoveRows(menu, enableSelected);
		
		// check all rows
		getAddAllRows(menu);
		
		// uncheck all rows
		getRemoveAllRows(menu);
	}
	
	protected MenuItem getRecolorItem(Menu menu, String itemType, boolean enabled) {
	    MenuItem recolorItem = new MenuItem(menu, SWT.PUSH);

	    recolorItem.setText(Messages.getString("GenericTable.recolor1") + itemType + Messages.getString("GenericTable.recolor2")); //$NON-NLS-1$ //$NON-NLS-2$
	    recolorItem.setEnabled(enabled);
	    
	    if (enabled) {
	    	recolorItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("recolor"); //$NON-NLS-1$
				}
			});
	    }

	    return recolorItem;
	}

	protected MenuItem getChangeThresholds(Menu menu) {
	    MenuItem thresholdItem = new MenuItem(menu, SWT.PUSH);

		thresholdItem.setText(Messages.getString("GenericTable.changeThresholds")); //$NON-NLS-1$
		thresholdItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
				// get the original thresholds
				// get the new thresholds
				new SetThresholdsDialog(e.display);
			}
		});
		return thresholdItem;
	}

	protected MenuItem getSourceLookUpItem(Menu menu, String itemType) {
	    MenuItem sourceLookupItem = new MenuItem(menu, SWT.PUSH);

	    sourceLookupItem.setText(Messages.getString("GenericTable.sourcelookup"));	//$NON-NLS-1$
		sourceLookupItem.addSelectionListener(new SelectionAdapter() { 
			public void widgetSelected(SelectionEvent e) {
			    action("sourceLookup"); //$NON-NLS-1$
			}
		});
		return sourceLookupItem;
	}	

	protected MenuItem getDisabledSourceLookUpItem(Menu menu, String itemType) {
	    MenuItem disabledSourceLookupItem = new MenuItem(menu, SWT.PUSH);

	    disabledSourceLookupItem.setText(Messages.getString("GenericTable.sourcelookup"));	//$NON-NLS-1$
		disabledSourceLookupItem.setEnabled(false);
		return disabledSourceLookupItem;
	}
	
	
	protected String copyHeading(Table table, int checkboxType, String separator, String ending)
	{
		String copyString = ""; //$NON-NLS-1$
		String addString;
		int[] columnOrder = table.getColumnOrder();
		
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn tableColumn = table.getColumn(columnOrder[i]);
			if (i > 0)
				copyString += separator;
			
			// Note: this code assumes that the original column 0 has the checkbox
			if (columnOrder[i] == 0 && checkboxType != CHECKBOX_NONE) {
				copyString += Messages.getString("GenericTable.checked");  //$NON-NLS-1$
				if (checkboxType == CHECKBOX_TEXT) {
					copyString += separator;
					addString = tableColumn.getText();
					if (addString.indexOf(separator) == -1)
						copyString += addString;
					else
						copyString += "\"" + addString + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else {
				copyString += tableColumn.getText();
			}
		}
		
		return copyString + ending;
	}

	protected String copySelected(Table table, int checkboxType, String separator, String ending)
	{
		String copyString = ""; //$NON-NLS-1$
		int[] columnOrder = table.getColumnOrder();
		boolean[] isHex   = (boolean[]) table.getData("isHex"); //$NON-NLS-1$

		int selectionCount = table.getSelectionCount();
		
		for (int i = 0; (selectionCount > 0) && i < table.getItemCount(); i++)
		{
			if (table.isSelected(i))
			{
				selectionCount--;
				copyString += copyRow(isHex, table.getItem(i), checkboxType, table.getColumnCount(), columnOrder, separator) + ending;
			}
		}
		
		return copyString;
	}

	protected String copyTable(Table table, int checkboxType, String separator, String ending)
	{
		String copyString = ""; //$NON-NLS-1$
		int[] columnOrder = table.getColumnOrder();
		boolean[] isHex   = (boolean[]) table.getData("isHex"); //$NON-NLS-1$

		for (int i = 0; i < table.getItemCount(); i++)
			copyString += copyRow(isHex, table.getItem(i), checkboxType, table.getColumnCount(), columnOrder, separator) + ending;
		
		return copyString;
	}
	
	protected String copyRow(boolean[] isHex, TableItem item, int checkboxType, int columnCount, int[] columnOrder, String separator)
	{
		String copyString = ""; //$NON-NLS-1$
		String addString;
		for (int i = 0; i < columnCount; i++) {
			if (i > 0)
				copyString += separator;
			
			int realColumn = columnOrder[i];
			
			// Note: this code assumes that the original column 0 has the checkbox
			if (realColumn == 0 && checkboxType != CHECKBOX_NONE) {
				copyString += item.getChecked() ? Messages.getString("GenericTable.true") : Messages.getString("GenericTable.false"); //$NON-NLS-1$ //$NON-NLS-2$
				if (checkboxType == CHECKBOX_TEXT) {
					copyString += separator;
					addString = item.getText(realColumn);
					if ((isHex != null) && (isHex.length > realColumn) && isHex[realColumn] && addString.length() > 0) {
						String string = "00000000" + addString; //$NON-NLS-1$
						copyString += "0x" + string.substring(string.length() - 8); //$NON-NLS-1$
					}
					else {
						if (addString.indexOf(separator) == -1)
							copyString += addString;
						else
							copyString += "\"" + addString + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
			else {
				addString = item.getText(realColumn);
				if ((isHex != null) && (isHex.length > realColumn) && isHex[realColumn] && addString.length() > 0) {
					String string = "00000000" + addString; //$NON-NLS-1$
					copyString += "0x" + string.substring(string.length() - 8); //$NON-NLS-1$
				}
				else {
					if (addString.indexOf(separator) == -1)
						copyString += addString;
					else
						copyString += "\"" + addString + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		
		return copyString;
	}

	public CheckboxTableViewer getTableViewer() {
		return this.tableViewer;
	}

	public Table getTable() {
		return this.table;
	}

    protected void actionSelectAll()
    {
        this.table.selectAll();
        this.table.redraw();
    }
    
    public String getCopyString(Table table, int checkboxType, boolean copyTable, String separator, String ending)
    {
		// copy all (copyTable == true) or part of a single table to the clipboard or to a file
        String copyString;

		// copy the column headings, followed by the desired table rows
		if (copyTable) {
			copyString = copyHeading(table, checkboxType, separator, ending);
			copyString += copyTable(table, checkboxType, separator, ending);
		}
		else
			copyString = copySelected(table, checkboxType, separator, ending);
		
		return copyString;
    }
    
    // class to pass table to the save wizard
    public class SaveTableString implements ISaveTable {
    	private Table table;
    	private int checkboxType;
    	private String separator;
    	private String ending;
    	
    	public SaveTableString(Table table, int checkboxType, String separator, String ending) {
    		this.table        = table;
    		this.checkboxType = checkboxType;
    		this.separator    = separator;
    		this.ending       = ending;
		}

    	public String getData() {
			return getCopyString(table, checkboxType, true, separator, ending);
		}
    }

	protected void actionCopyOrSave(boolean doCopy, Table table, int checkboxType, boolean copyTable, String separator, String ending)
	{
		// copy all (copyTable == true) or part of a single table to the clipboard or to a file
		if (doCopy) {
			// change the clipboard contents
	        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	        String copyString = getCopyString(table, checkboxType, copyTable, separator, ending);
			StringSelection contents = new StringSelection(copyString);
	        cb.setContents(contents, contents);
		} else {
			// save to a file
			SaveTableString getString = new SaveTableString(table, checkboxType, separator, ending);
			WizardDialog dialog;
			Wizard wizard = new SaveTableWizard(getString);
			dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);
	    	dialog.open();
		}
	}

	protected void actionSaveSamples(ISaveSamples saveSamples)
	{
		new SaveSamples(saveSamples);
	}
}
