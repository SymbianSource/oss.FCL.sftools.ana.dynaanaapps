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
package com.nokia.carbide.cpp.pi.internal.perfcounters.ui;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Input dialog for the processor speed, specified in MHz.
 *
 */
public class ProcessorSpeedInputDialog extends InputDialog {
	
	private static final String DIALOG_TITLE = Messages.ProcessorSpeedInputDialog_0;
	private static final String DIALOG_MESSAGE = Messages.ProcessorSpeedInputDialog_1;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	private static final int LOW_VALUE = 100;
	private static final int HIGH_VALUE = 1000;
	

	/**
	 * Constructor
	 * @param parentShell The shell for this dialog to open in
	 */
	public ProcessorSpeedInputDialog(Shell parentShell) {
		super(parentShell, DIALOG_TITLE, DIALOG_MESSAGE, EMPTY_STRING, new IInputValidator(){

			public String isValid(String newText) {
				if (newText.length() == 0){
					return Messages.ProcessorSpeedInputDialog_3;
				}
				
				try {
					int value = Integer.parseInt(newText);
					if (value < LOW_VALUE || value > HIGH_VALUE){
						return String.format(Messages.ProcessorSpeedInputDialog_4, LOW_VALUE, HIGH_VALUE);
					}
				} catch (NumberFormatException e) {
					return Messages.ProcessorSpeedInputDialog_5;
				}
				return null;
			}
		});
	}


	/**
	 * Returns the processor speed as an int value
	 * @return the processor speed
	 */
	public int getIntValue() {
		return Integer.parseInt(getValue());
	}

}
