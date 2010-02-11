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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.CarbidePiWizardHelpIds;
import com.nokia.carbide.cpp.internal.pi.wizards.ui.util.SdkChooserBase;

public class NewPIWizardPageRomSdkSubTask extends NewPIWizardPage
{
	SdkChooserBase sdkCommon = new SdkChooserBase();
	private Composite composite;
	
	protected NewPIWizardPageRomSdkSubTask() {
		super(""); //$NON-NLS-1$
		setTitle(Messages.getString("NewPIWizardPageRomSdkSubTask.title")); //$NON-NLS-1$
	    setDescription(Messages.getString("NewPIWizardPageRomSdkSubTask.description")); //$NON-NLS-1$
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		composite = sdkCommon.layout (parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, CarbidePiWizardHelpIds.PI_IMPORT_WIZARD_ROM_SDK);
		setControl(composite);
	}
}
