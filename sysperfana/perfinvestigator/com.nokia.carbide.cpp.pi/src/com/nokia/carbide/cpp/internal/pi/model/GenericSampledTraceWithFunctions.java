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


import java.util.Enumeration;

public abstract class GenericSampledTraceWithFunctions extends GenericSampledTrace implements RefinableTrace
{
	private static final long serialVersionUID = 8246297583039760015L;

	public void refineTrace(FunctionResolver resolver)
	{
		if (!resolver.canResolve())
			return;

		Enumeration sEnum = this.samples.elements();
		while (sEnum.hasMoreElements())
		{
			GenericSampleWithFunctions s = (GenericSampleWithFunctions)sEnum.nextElement();
			s.resolveFunction(resolver);
		}
	}

	public void finalizeTrace() {
	}
}
