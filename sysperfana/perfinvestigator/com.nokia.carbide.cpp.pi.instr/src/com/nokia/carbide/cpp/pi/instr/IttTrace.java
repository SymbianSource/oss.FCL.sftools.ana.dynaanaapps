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

package com.nokia.carbide.cpp.pi.instr;

import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTraceWithFunctions;
import com.nokia.carbide.cpp.internal.pi.visual.GenericTraceGraph;


public class IttTrace extends GenericSampledTraceWithFunctions
{	
	private static final long serialVersionUID = 8450351679961604426L;
	
	public void addSample(IttSample sample)
	{
		this.samples.add(sample);
	}

	public IttSample getIttSample(int number)
	{
		return (IttSample)this.samples.elementAt(number);
	}

	public GenericTraceGraph getTraceGraph() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace#getTraceGraph(int, com.nokia.carbide.cpp.internal.pi.analyser.AnalyseTab)
	 */
	public GenericTraceGraph getTraceGraph(int graphNumber) {
		return null;
	}
}
