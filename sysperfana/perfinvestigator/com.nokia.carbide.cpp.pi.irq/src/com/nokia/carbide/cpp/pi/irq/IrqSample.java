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

import com.nokia.carbide.cpp.internal.pi.model.FunctionResolver;
import com.nokia.carbide.cpp.internal.pi.model.GenericSampleWithFunctions;
import com.nokia.carbide.cpp.internal.pi.model.IFunction;

/**
 * Irq sample class
 */
public class IrqSample extends GenericSampleWithFunctions {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 8688982614582925161L;

	public int repeatCount = 0;

	/* irq data */
	private long[] swiData;
	private int[] irqData;

	/* function what caused this interrupt(if this is SWI) */
	private IFunction swiLrFunction;

	/* type of the IRQ */
	public static final char TYPE_IRQ = 1;
	public static final char TYPE_SWI = 2;
	private char type;

	/**
	 * Constructor for software interrupt
	 * 
	 * @param sampleSynchTime
	 *            time of the interrupt
	 * @param swiInstr
	 *            intruction id
	 * @param swiThread
	 *            thread id
	 * @param swiLr
	 *            lr value
	 */

	public IrqSample(long sampleSynchTime, long swiInstr, long swiThread,
			long swiLr) {
		this.sampleSynchTime = sampleSynchTime;
		this.type = TYPE_SWI;
		this.swiData = new long[2];

		/* this.swiData[0] = ((swiInstr<<32)>>>32); Removed, This is not needed */

		this.swiData[0] = ((swiThread << 32) >>> 32);
		this.swiData[1] = ((swiLr << 32) >>> 32);

	}

	/**
	 * Constructor for hardware interrupt
	 * 
	 * @param sampleSynchTime
	 *            time of the interrupt
	 * @param irqLev1
	 * @param irqLev2
	 */
	public IrqSample(long sampleSynchTime, int irqLev1, int irqLev2) {
		this.sampleSynchTime = sampleSynchTime;

		this.type = TYPE_IRQ;
		this.irqData = new int[2];
		this.irqData[0] = irqLev1;
		this.irqData[1] = irqLev2;
	}

	/**
	 * @return swi lf function
	 */
	public IFunction getFunction() {
		return this.swiLrFunction;
	}

	/**
	 * @return type of the irq
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return lr value
	 */
	public long getLrValue() {
		if (this.swiData != null)
			return this.swiData[1];
		else
			return -1;
	}

	/**
	 * @return thread value
	 */
	public long getThreadValue() {
		if (this.swiData != null)
			return this.swiData[0];
		else
			return -1;
	}

	/**
	 * @return instruction value
	 */
	/*
	 * public long getInstrValue() { if(this.swiData != null) return
	 * this.swiData[0]; else return -1; }
	 */

	/**
	 * @return l1 value
	 */
	public int getIrqL1Value() {
		if (this.irqData != null)
			return this.irqData[0];
		else
			return -1;
	}

	/**
	 * @return l2 value
	 */
	public int getIrqL2Value() {
		if (this.irqData != null)
			return this.irqData[1];
		else
			return -1;
	}

	/**
	 * resolves name of the function from functionresolver
	 */
	@Override
	public void resolveFunction(FunctionResolver res) {
		if (this.type == TYPE_SWI) {
			if (res.getResolverName().equals(Messages.IrqSample_0)) {
				this.swiLrFunction = res
						.findFunctionForAddress(this.swiData[1]);
			}
		}
	}

}
