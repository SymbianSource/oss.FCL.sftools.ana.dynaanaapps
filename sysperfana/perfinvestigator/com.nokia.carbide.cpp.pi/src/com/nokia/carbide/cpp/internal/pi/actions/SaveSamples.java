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

package com.nokia.carbide.cpp.internal.pi.actions;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.interfaces.ISaveSamples;
import com.nokia.carbide.cpp.internal.pi.save.SaveSamplesWizard;


public class SaveSamples {
	public SaveSamples(ISaveSamples saveSamples) {
		WizardDialog dialog;
		Wizard wizard = new SaveSamplesWizard(saveSamples);
		dialog = new WizardDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), wizard);
    	dialog.open();
	}
}
