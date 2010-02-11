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

package com.nokia.carbide.cpp.internal.pi.wizards.ui;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.SdkChooserBase;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;

public class NewPIWizardPagePkgSdkDialog  extends TrayDialog
{
	SdkChooserBase sdkCommon = new SdkChooserBase();
	private Composite composite;
	private ISymbianSDK selection; 

	protected NewPIWizardPagePkgSdkDialog(Shell arg0) {
		super(arg0);
	}

	public Control createDialogArea(Composite parent) {
		selection = null;
		composite = sdkCommon.layout(parent);
		getShell().setText(Messages.getString("NewPIWizardPagePkgSdkDialog.shell.title")); //$NON-NLS-1$
		return composite;
	}
	
	public void okPressed() {
		selection = sdkCommon.getSelectedSdk();
		super.okPressed();
	}
	
	public ISymbianSDK getSelection() {
		return selection;
	}
}
