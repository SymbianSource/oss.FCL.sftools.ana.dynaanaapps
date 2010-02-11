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

package com.nokia.carbide.cpp.pi.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/* 
 * 
 * This class just make a list of numbers and assign color to them
 * Testers should 
 * 1.visually verify color if it looks good to them 
 * 2.try to assign different color to different entries
 * 3.try to assign the same color to different entries and observe error message
 * 
 * */

public class TestDataMiningPalette {

	private Shell sShell = null;

	private Map<Button,CLabel> dataMiningColors = new HashMap<Button,CLabel>();
	private DataMiningPalette palette = new DataMiningPalette();
	private ArrayList<Integer> sosThreadlist = new ArrayList<Integer>();
	private ArrayList<Integer> threadlist = new ArrayList<Integer>();

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		TestDataMiningPalette thisClass = new TestDataMiningPalette();
		thisClass.createSShell();
		thisClass.sShell.open();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText(Messages.getString("TestDataMiningPalette.shell")); //$NON-NLS-1$
		sShell.setSize(new Point(1000, 700));

		RowLayout layout = new RowLayout();
		sShell.setLayout(layout);

		for (int i = 0; i < 216; i++) {
			sosThreadlist.add(i);
		}
		for (int i = 216; i < 216 + 255; i++) {
			threadlist.add(i);
		}
		palette.assignSOSColor(sosThreadlist);
		palette.assignColor(threadlist);
		
		sosThreadlist.addAll(threadlist);
		
		Iterator entryItr = sosThreadlist.iterator();

		while(entryItr.hasNext())
		{
			Object entry = entryItr.next();
			
			final Button button = new Button(sShell, SWT.NONE);
			button.setText(entry.toString());
			button.setBackground(new Color(Display.getCurrent(), palette.getRGB(entry)));
			button.addSelectionListener(new SelectionAdapter() {
            	public void widgetSelected(SelectionEvent e) {
            		Integer index = new Integer(button.getText());
            		if(palette.recolorEntryDialog(sShell, index))
            		{
            			RGB newRGB = palette.getRGB(index);
            			CLabel myLabel = dataMiningColors.get(button);
            			myLabel.setBackground(new Color(Display.getCurrent(), newRGB));
            		}
            	}
			});
			CLabel cLabel = new CLabel(sShell, SWT.NONE);
			cLabel.setText(entry.toString());
			cLabel.setBackground(new Color(Display.getCurrent(), palette.getRGB(entry)));
			dataMiningColors.put(button, cLabel);
		}
	}

}
