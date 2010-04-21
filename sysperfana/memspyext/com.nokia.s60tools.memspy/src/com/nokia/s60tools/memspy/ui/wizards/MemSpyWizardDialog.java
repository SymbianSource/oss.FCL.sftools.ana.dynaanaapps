/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
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



package com.nokia.s60tools.memspy.ui.wizards;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

public class MemSpyWizardDialog extends WizardDialog {
	@Override
	public void create() {
		super.create();
	}

	public MemSpyWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}
	
	public void enableBackCancelButtons(boolean enable) {
		getButton(IDialogConstants.BACK_ID).setEnabled(enable);
		getButton(IDialogConstants.CANCEL_ID).setEnabled(enable);


	}
	
	public void setCancelText(String newText){
		getButton(IDialogConstants.CANCEL_ID).setText(newText);

	}
	
}
