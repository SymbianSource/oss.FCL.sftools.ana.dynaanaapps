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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

// try to work around some strange behavior around setErrorMessage we don't understand

public class NewPIWizardPage extends WizardPage {

	protected NewPIWizardPage(String arg0) {
		super(arg0);
	}

	public void createControl(Composite arg0) {
	}
	
	public void setVisible(boolean visable) {
		if (visable == true) {
			// I have no idea why, setErrorMessage could mess up
			// the title and flip to last page , force this in base 
			// class every time we show
			setTitle(this.getTitle());
	    	setDescription(this.getDescription());
		}
		super.setVisible(visable);
	}

}
