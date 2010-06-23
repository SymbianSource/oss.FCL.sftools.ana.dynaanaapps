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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TestTableColorPalette {
	private Shell sShell = null;
	
	private final Map<Button,CLabel> testColorsMap = new HashMap<Button,CLabel>();
	private final FunctionColorPalette palette = new FunctionColorPalette();
	
	public static void main(final String[] args) {
		Display display = Display.getDefault();
		TestTableColorPalette thisClass = new TestTableColorPalette();
		thisClass.createSShell();
		thisClass.sShell.open();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch()){
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText(Messages.getString("TestColorPalette.shell")); //$NON-NLS-1$
		sShell.setSize(new Point(700, 700));

		RowLayout layout = new RowLayout();
		sShell.setLayout(layout);
		
		for (Integer i = 0; i < 6 * 6 * 7; i++)
		{
			final Integer finalI = i;
			palette.getColor(i.toString());
			final Button button = new Button(sShell, SWT.NONE);
			button.setText(i.toString());
			button.setBackground(palette.getColor(i.toString()));
			button.addSelectionListener(new SelectionAdapter() {
            	public void widgetSelected(final SelectionEvent e) {
            		if(palette.recolorEntryDialog(sShell, finalI))
            		{
            			Color myColor = palette.getColor(finalI);
            			CLabel myLabel = testColorsMap.get(button);
            			myLabel.setBackground(myColor);
            		}
            	}
			});
			CLabel cLabel = new CLabel(sShell, SWT.NONE);
			cLabel.setText(i.toString());
			cLabel.setBackground(palette.getColor(i.toString()));
			testColorsMap.put(button, cLabel);
		}
	}

}
