/*
 * Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies). 
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

package com.nokia.carbide.cpp.pi.internal.perfcounters;

import com.nokia.carbide.cpp.pi.peccommon.PecCommonLegendElement;
import com.nokia.carbide.cpp.pi.peccommon.PecCommonTrace;

/**
 * The model class for Performance Counter traces. This manages the data
 * for the PEC trace graphs, and is responsible for creating the graphs. 
 */
public class PecTrace extends PecCommonTrace {
	private static final long serialVersionUID = 4425739452429422333L;
	
	private boolean generateMipsGraph = false;
	/** Name of the MIPS graph */
	public static final  String MIPS_NAME = Messages.PecTrace_0;

	/**
	 * Constructor
	 */
	public PecTrace() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.pi.peccommon.PecCommonTrace#createLegendElements(com.nokia.carbide.cpp.pi.peccommon.PecCommonLegendElement[], java.lang.String[], int[], long[], int[], int[])
	 */
	@Override
	protected PecCommonLegendElement[] createLegendElements(
			PecCommonLegendElement[] existingElements, String[] typeStrings,
			int[] cnts, long[] sums, int[] mins, int[] maxs) {
    	int c = typeStrings.length;
    	boolean create = existingElements == null;    	
    	PecLegendElement[] les = create ? new PecLegendElement[c] : (PecLegendElement[])existingElements;
		char shortTitle = 'A';
		
		for (int i = 0; i < c; i++) {
			
			PecLegendElement legendElement = create ? new PecLegendElement(i, typeStrings[i], shortTitle, typeStrings[i].equals(MIPS_NAME)) : (PecLegendElement)existingElements[i];
			legendElement.setCnt(cnts[i]);
			legendElement.setSum(sums[i]);
			legendElement.setMax(maxs[i]);
			legendElement.setMin(mins[i]);
			float[] xOverY = new float[c];
			for (int j = 0; j < c; j++) {
				if (i == j){
					xOverY[j] = 1f;
				} else if (sums[j] == 0){
					xOverY[j] = 0;
				} else {
					xOverY[j] = sums[i] / (float)sums[j];
				}
			}
			legendElement.setxOverY(xOverY);
			les[i] = legendElement;
			shortTitle ++;
		}
		return les;
	}

	/**
	 * @param generateMipsGraph true, if the MIPS graph is to be generated, false otherwise
	 */
	public void setGenerateMipsGraph(boolean generateMipsGraph) {
		this.generateMipsGraph = generateMipsGraph;
	}

	/**
	 * @return true, if the MIPS graph is to be generated, false otherwise
	 */
	public boolean generateMipsGraph() {
		return generateMipsGraph;
	}
	
}
