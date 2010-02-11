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

public class BappeaAnalysisInfo implements Serializable
{
	private static final long serialVersionUID = -4724464175067752118L;

    public String bappea_file_version;
    public Vector<Object> analysis_info;
    public Vector<Object> trace_info;
    public Vector<Object> additional_info;

	public BappeaAnalysisInfo()
	{
		bappea_file_version = Messages.getString("BappeaAnalysisInfo.notDefined"); //$NON-NLS-1$
		analysis_info   = new Vector<Object>();
		trace_info      = new Vector<Object>();
		additional_info = new Vector<Object>();
	}
}
