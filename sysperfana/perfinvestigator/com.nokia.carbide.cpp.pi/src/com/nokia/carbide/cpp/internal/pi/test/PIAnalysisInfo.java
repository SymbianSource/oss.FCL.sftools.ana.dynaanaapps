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

package com.nokia.carbide.cpp.internal.pi.test;

import java.io.Serializable;
import java.util.Vector;


public class PIAnalysisInfo implements Serializable {
	
	private static final long serialVersionUID = 7840137496875268566L;

	public String pi_file_version;
    public Vector<Object> analysis_info;
    public Vector<Object> trace_info;
    public Vector<Object> additional_info;

	public PIAnalysisInfo()
	{
		pi_file_version = Messages.getString("PIAnalysisInfo.unknown"); //$NON-NLS-1$
		analysis_info   = new Vector<Object>();
		trace_info      = new Vector<Object>();
		additional_info = new Vector<Object>();
	}
}
