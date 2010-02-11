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

package com.nokia.carbide.cpp.internal.pi.model;

import javax.swing.JProgressBar;

public class ProgressBar extends JProgressBar {

	private static final long serialVersionUID = 4862691591715531894L;

	public ProgressBar() {
        super();
        this.setValue(0);
        this.setStringPainted(true);
        this.setIndeterminate(true);
        this.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        this.setFont(new java.awt.Font(Messages.getString("ProgressBar.defaultFont"), java.awt.Font.PLAIN, 10)); //$NON-NLS-1$
    }
    
	public void addValue(int value)
	{
		this.setValue(this.getValue() + value);
		this.validate();
	}
	
	public int getMaxValue()
	{
		return this.getMaximum();
	}
	
	public void advanceTo(int value)
	{
		try
		{
		  for (int i = this.getValue(); i < value; i++)
		  {
			addValue(1);
			Thread.sleep(10);
		  }
		}
		catch (Exception e) {}
	}
	
	public void runToEnd()
	{
		try
		{
		  for (int i = getValue(); i < getMaxValue(); i++)
		  {
			addValue(1);
			Thread.sleep(10);
		  }
		}
		catch (Exception e) {}
	}
}
