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

package com.nokia.carbide.cpp.internal.pi.analyser;

import java.awt.Frame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public final class TestGUI extends Frame {

	private static final long serialVersionUID = -7535470144323980407L;

	private static TestGUI thisFrame;

	// recent files
	public static final int NUMBER_RECENT_FILES = 6;

	// status panel containing progressPanel
//	private static JPanel statusPanel = null;

	// progress panel containing progressBar
//	private static JPanel progressPanel = null;
//	private static ProgressBar progressBar;

	// graph popup menu and its items
    private transient Menu	 graphPopupMenu;	 
    private transient MenuItem zoomInItem;
    private transient MenuItem zoomOutItem;
    private transient MenuItem zoomToSelectionItem;
    private transient MenuItem zoomToTraceItem;

	/**
	 * This is the default constructor
	 */
	private TestGUI() {
		super();
	}
	
	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	public static TestGUI getInstance() {
		if (thisFrame == null)
		{
			thisFrame = new TestGUI();
		}
		return thisFrame;
	}

	public static void action(String actionString) {
		PIChangeEvent.action(actionString);
	}

	/**
	 * This method initializes graphPopupMenu
	 *
	 */
	public Menu getGraphPopupMenu(Decorations parent, boolean forceCreate) {
		if ((graphPopupMenu == null) || forceCreate) {
			if (graphPopupMenu != null)
				graphPopupMenu.dispose();
			
			graphPopupMenu = new Menu(parent, SWT.POP_UP);
			getZoomInItem(graphPopupMenu, forceCreate);
			getZoomOutItem(graphPopupMenu, forceCreate);
			getZoomToSelectionItem(graphPopupMenu, forceCreate);
			getZoomToTraceItem(graphPopupMenu, forceCreate);
//			new MenuItem(graphPopupMenu, SWT.SEPARATOR);
//			getSynchroniseItem(graphPopupMenu, forceCreate);
			graphPopupMenu.setVisible(true);
		}
		return graphPopupMenu;
	}

//	/**
//	 * This method initializes synchroniseItem
//	 *
//	 */
//	private MenuItem getSynchroniseItem(Menu menu, boolean forceCreate) {
//		if ((synchroniseItem == null) || forceCreate) {
//			if (synchroniseItem != null)
//				synchroniseItem.dispose();
//			
//			synchroniseItem = new MenuItem(menu, SWT.PUSH);
//			synchroniseItem.setText("Synchronize Graph Displays");
//			synchroniseItem.addSelectionListener(new SelectionAdapter() { 
//				public void widgetSelected(SelectionEvent e) {
//				    action("synchronise");
//				}
//			});
//		}
//		return synchroniseItem;
//	}

	/**
	 * This method initializes zoomInItem
	 *
	 */
	private MenuItem getZoomInItem(Menu menu, boolean forceCreate) {
		if ((zoomInItem == null) || forceCreate) {
			if (zoomInItem != null)
				zoomInItem.dispose();
			
			zoomInItem = new MenuItem(menu, SWT.PUSH);
			zoomInItem.setText(Messages.getString("TestGUI.zoomIn")); //$NON-NLS-1$
			zoomInItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("-"); //$NON-NLS-1$
				}
			});
		}
		return zoomInItem;
	}

	/**
	 * This method initializes zoomOutItem
	 */
	private MenuItem getZoomOutItem(Menu menu, boolean forceCreate) {
		if ((zoomOutItem == null) || forceCreate) {
			if (zoomOutItem != null)
				zoomOutItem.dispose();
			
			zoomOutItem = new MenuItem(menu, SWT.PUSH);
			zoomOutItem.setText(Messages.getString("TestGUI.zoomOut")); //$NON-NLS-1$
			zoomOutItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("+"); //$NON-NLS-1$
				}
			});
		}
		return zoomOutItem;
	}

	/**
	 * This method initializes zoomAreaItem
	 */
	private MenuItem getZoomToSelectionItem(Menu menu, boolean forceCreate) {
		if ((zoomToSelectionItem == null) || forceCreate) {
			if (zoomToSelectionItem != null)
				zoomToSelectionItem.dispose();
			
			zoomToSelectionItem = new MenuItem(menu, SWT.PUSH);
			zoomToSelectionItem.setText(Messages.getString("TestGUI.showSelected")); //$NON-NLS-1$
			zoomToSelectionItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("--"); //$NON-NLS-1$
				}
			});
		}
		return zoomToSelectionItem;
	}

	/**
	 * This method initializes zoomTraceItem
	 */
	private MenuItem getZoomToTraceItem(Menu menu, boolean forceCreate) {
		if ((zoomToTraceItem == null) || forceCreate) {
			if (zoomToTraceItem != null)
				zoomToTraceItem.dispose();
			
			zoomToTraceItem = new MenuItem(menu, SWT.PUSH);
			zoomToTraceItem.setText(Messages.getString("TestGUI.showGraph")); //$NON-NLS-1$
			zoomToTraceItem.addSelectionListener(new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent e) {
				    action("++"); //$NON-NLS-1$
				}
			});
		}
		return zoomToTraceItem;
	}
}
