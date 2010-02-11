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

package com.nokia.carbide.cpp.pi.button;

import com.nokia.carbide.cpp.internal.pi.model.GenericSample;

public class BupSample extends GenericSample {

	private static final long serialVersionUID = -498083219245040352L;
	
	private int keyCode = 0;
	private String label = null;
	private String comment = null;
	// version 1.3
	private boolean labelModified = false;	// any one need to reset this should use resetLabelToMapDefault or declare a new sample
	
	public BupSample (long sampleTime, int code, IBupEventMap map) {
		this.sampleSynchTime = sampleTime;
		this.keyCode = code;
		
		// if event is touch event add labels for it.
		if ( code == 69999 ) {
			this.label = Messages.getString("BupSample.0"); //$NON-NLS-1$
		}
		else if ( code == 70000 ){
			this.label = Messages.getString("BupSample.1"); //$NON-NLS-1$
		}
		else{
			this.label = map.getLabel(code);
		}
		this.comment = null;
		this.labelModified = false;
	}
	
	public int getKeyCode() {
		return keyCode;
	}
	
	public void setLabel(String label) {
		this.label = label;
		this.labelModified = true;
	}
	
	public String getLabel() {
		return label;
	}
	
	// insist on not exposing labelModified, use this for map switching and take care of modified flag
	public void resetLabelToMapDefault(IBupEventMap map) {
		label = map.getLabel(keyCode);
		labelModified = false;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public boolean isLabelModified() {
		return labelModified;
	}
}
