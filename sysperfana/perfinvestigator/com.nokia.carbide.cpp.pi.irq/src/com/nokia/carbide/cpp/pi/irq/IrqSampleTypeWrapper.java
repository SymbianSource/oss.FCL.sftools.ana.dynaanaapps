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

package com.nokia.carbide.cpp.pi.irq;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import org.eclipse.swt.graphics.RGB;

/**
 * Class for irq sample type wrapper. This class is used for representing one
 * irq line/sw function
 */
public class IrqSampleTypeWrapper implements Serializable {
	/**
	 * Serial version uid
	 */
	private static final long serialVersionUID = -377498088520328159L;

	/* protorype of this sample type */
	private IrqSample prototypeSample;

	/* vector containing all samples */
	public Vector<IrqSample> samples;

	/* color of this irq line/function */
	public RGB rgb;

	public int count = 0;
	// 0 = address, 1 = name
	public int sortMode = 1;

	private boolean selected = true;

	/**
	 * Constructor
	 * 
	 * @param sampleType
	 *            prototypeSample
	 * @param colorSet
	 *            colorset containing all colors that are in use so far
	 */
	public IrqSampleTypeWrapper(IrqSample sampleType, HashSet<RGB> colorSet,
			Hashtable<String, RGB> colorsOfThreadsAndFunctions) {
		prototypeSample = sampleType;
		samples = new Vector<IrqSample>();
		samples.add(prototypeSample);

		String key = Messages.IrqSampleTypeWrapper_0;
		if (prototypeSample.getType() == IrqSample.TYPE_IRQ) {
			key = Integer.toString(prototypeSample.getIrqL1Value());
		} else {
			if (prototypeSample.getFunction() != null) {
				key = prototypeSample.getFunction().getFunctionName();
			}
		}

		if (!colorsOfThreadsAndFunctions.containsKey(key)) {
			getRandomColor(colorSet);
			colorsOfThreadsAndFunctions.put(key, rgb);
		} else {
			rgb = colorsOfThreadsAndFunctions.get(key);
		}

	}

	/**
	 * Creates random color that is not yet found from given colorset
	 * 
	 * @param colorSet
	 *            colorset that is used
	 */
	private void getRandomColor(HashSet<RGB> colorSet) {
		// Get random color for irq line or thread. Make sure that color is not
		// yet found from colorset.
		Random r = new Random();
		rgb = new RGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		while (colorSet.contains(rgb)) {
			rgb = new RGB(r.nextInt(256), r.nextInt(256), r.nextInt(256));
		}
		colorSet.add(rgb);
	}

	/**
	 * Adds one sample into wrapper
	 * 
	 * @param sample
	 *            sample that is added
	 */
	public void addSample(IrqSample sample) {
		if (sample.getType() == prototypeSample.getType()) {
			if (sample.getType() == IrqSample.TYPE_IRQ) {
				if (sample.getIrqL1Value() == prototypeSample.getIrqL1Value()
						&& sample.getIrqL2Value() == prototypeSample
								.getIrqL2Value()) {
					samples.add(sample);
				}
			} else if (sample.getType() == IrqSample.TYPE_SWI) {
				if (sample.getLrValue() == prototypeSample.getLrValue()) {
					samples.add(sample);
				}
			}
		}
	}

	/**
	 * @return prototype sample of this wrapper
	 */
	public IrqSample getPrototypeSample() {
		return prototypeSample;
	}

	/**
	 * @return all samples of this wrapper
	 */
	public Vector<IrqSample> getSamples() {
		return samples;
	}

	/**
	 * @return true if this function/irq line is selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * @param selected
	 *            sets selection value
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public static String getLineText(long lineID) {
		return Messages.IrqSampleTypeWrapper_1 + Long.toHexString(lineID);
	}

}