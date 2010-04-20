/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
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

package com.nokia.s60tools.crashanalyser.ui.editors;

import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableKeyListener implements KeyListener{

    private static final int CTRL_C = 3;
	private Table table = null;
	private TableCursor cursor = null;

	/**
	 * Create new TableKeyListener.
	 * 
	 * @param table Table
	 * @param cursor Cursor
	 */
	public TableKeyListener(Table table, TableCursor cursor) {
		this.table = table;
	    this.cursor = cursor;	        
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {

		switch (e.character) {

		case CTRL_C:
			// Copy the cell content to clipboard

	        try {
	            Clipboard clipBoard = new Clipboard(Display.getCurrent());
	            TextTransfer textTransfer = TextTransfer.getInstance();

	            TableItem[] items = table.getSelection();

	            if (items == null || items.length == 0) {
	            	return;
	            }

	            int columnIndex = cursor.getColumn();
	            clipBoard.setContents(new Object[] { items[0].getText(columnIndex) }, new Transfer[] { textTransfer });

	        } catch (Exception ex) {
	            	// ignore
	        }
	        break;

	    default:
	        return;

	    }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		// Do nothing.
	}
	    
}
