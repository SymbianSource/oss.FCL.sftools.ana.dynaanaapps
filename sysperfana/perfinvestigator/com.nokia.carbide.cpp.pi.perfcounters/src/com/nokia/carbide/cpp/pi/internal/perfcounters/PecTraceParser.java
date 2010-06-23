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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.pi.internal.perfcounters.ui.ProcessorSpeedInputDialog;


/**
 * Parser for performance counter event traces in binary format.
 * Converts parsed content into PecSamples.
 */
public class PecTraceParser extends Parser {
	private static final int INSTRUCTIONS_EXECUTED = 0x7;
	private static final int CPU_CLOCK_TICK_DIV64 = 0xFFFF;
	private static final int DUMMY_MIPS_ID = -1;
	private static final int DUMMY_CPU_CLOCK_ID = -2;
	private static final String DATA_FORMAT_VERSION_OLD = "Bappea_V1.24_PEC";
	
	
	private boolean debug = false;
	
	private long time = 0;
	
	/** true if dialogs etc is allowed here, false for quiet mode */
	private boolean allowUserInteraction;
	
	/** produce MIPS graph data */
	protected boolean mipsEnabled;
	/** the processor speed to use for MIPS graph calculations. Updated from data samples if data is available */
	protected int processorSpeed;
	
	/**
	 * Constructor
	 * @param allowUserInteraction true, if user interactions such as dialogs are allowed
	 */
	public PecTraceParser(boolean allowUserInteraction) {
		super();
		this.allowUserInteraction = allowUserInteraction;
	}

	private static final Map<Integer, String> EVENT_TYPE_TABLE = new HashMap<Integer, String>();
	static {
		EVENT_TYPE_TABLE.put(0x0,Messages.PecTraceParser_0);
		EVENT_TYPE_TABLE.put(0x1,Messages.PecTraceParser_1);
		EVENT_TYPE_TABLE.put(0x2,Messages.PecTraceParser_2);
		EVENT_TYPE_TABLE.put(0x3,Messages.PecTraceParser_3);
		EVENT_TYPE_TABLE.put(0x4, Messages.PecTraceParser_4);
		EVENT_TYPE_TABLE.put(0x5, Messages.PecTraceParser_5);
		EVENT_TYPE_TABLE.put(0x6, Messages.PecTraceParser_6);
		EVENT_TYPE_TABLE.put(INSTRUCTIONS_EXECUTED, Messages.PecTraceParser_7);
		EVENT_TYPE_TABLE.put(0x9, Messages.PecTraceParser_8);
		EVENT_TYPE_TABLE.put(0xA, Messages.PecTraceParser_9);
		EVENT_TYPE_TABLE.put(0xB, Messages.PecTraceParser_10);
		EVENT_TYPE_TABLE.put(0xC, Messages.PecTraceParser_11);
		EVENT_TYPE_TABLE.put(0xD, Messages.PecTraceParser_12);
		EVENT_TYPE_TABLE.put(0xF, Messages.PecTraceParser_13);
		EVENT_TYPE_TABLE.put(0x10, Messages.PecTraceParser_14);
		EVENT_TYPE_TABLE.put(0x11, Messages.PecTraceParser_15);
		EVENT_TYPE_TABLE.put(0x12, Messages.PecTraceParser_16);
		EVENT_TYPE_TABLE.put(0x20, Messages.PecTraceParser_17);
		EVENT_TYPE_TABLE.put(0x21, Messages.PecTraceParser_18);
		EVENT_TYPE_TABLE.put(0x22, Messages.PecTraceParser_19);
		EVENT_TYPE_TABLE.put(0xFF, Messages.PecTraceParser_20);
		EVENT_TYPE_TABLE.put(CPU_CLOCK_TICK_DIV64, Messages.PecTraceParser_21);
		EVENT_TYPE_TABLE.put(DUMMY_MIPS_ID, PecTrace.MIPS_NAME);
		EVENT_TYPE_TABLE.put(DUMMY_CPU_CLOCK_ID, Messages.PecTraceParser_23);
	}
				
	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.Parser#parse(java.io.File)
	 */
	@Override
	public ParsedTraceData parse(File file) throws FileNotFoundException 
	{
		ParsedTraceData ptd = new ParsedTraceData();
		ptd.functionResolvers = null;
		ptd.staticData = null;
		PecTrace pecTrace = new PecTrace();
		ptd.traceData = pecTrace;
		doParsing(file, pecTrace);
		time = 0;
		
		return ptd;
	}
	
	private void doParsing(File f, PecTrace trace) throws FileNotFoundException
	{
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		try
		{
			int len = dis.readByte();
			byte[] versionString = new byte[len];
			dis.read(versionString);
			this.traceVersion = new String(versionString);
			if(debug)System.out.println("PEC trace version:"+this.traceVersion); //$NON-NLS-1$
			
			int firstData = dis.readUnsignedByte();
			int secondData = dis.readUnsignedByte();
			int thirdData = CPU_CLOCK_TICK_DIV64;

			if (this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
				if (allowUserInteraction && (firstData == INSTRUCTIONS_EXECUTED || secondData == INSTRUCTIONS_EXECUTED)){
					Display.getDefault().syncExec( new Runnable() {

						public void run () {
							// in future, if we don't want to have a dialog in the core parser class
							// we could call into an interface which ProcessorSpeedInputDialog would have to implement
							ProcessorSpeedInputDialog dialog = new ProcessorSpeedInputDialog(
									PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow().getShell());
							if (dialog.open() == Window.OK){
								processorSpeed = dialog.getIntValue();
								mipsEnabled = true;
							}
						}
					});
				}
			}
			
			// >=Bappea_V1.25_PEC
			else {
				if (firstData == INSTRUCTIONS_EXECUTED || secondData == INSTRUCTIONS_EXECUTED) {
					mipsEnabled = true;
				}

				long cpuClockRate = readCpuClockRate(dis);
				if(cpuClockRate > 0){
					// Hz => MHz
					trace.setCpuClockRate((int) cpuClockRate / 1000000);
				}
			
			}
			
			int graphCount = 0;
			
			if (this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
				graphCount = 4;
			}
			// >=Bappea_V1.25_PEC
			else {
				graphCount = 5;
			}
			
			Integer[] valueTypeVector = new Integer[mipsEnabled ? graphCount : 3];
			
			valueTypeVector[0] = Integer.valueOf(firstData);
			valueTypeVector[1] = Integer.valueOf(secondData);
			// this type is always the cpu clock tick div 64
			valueTypeVector[2] = Integer.valueOf(thirdData);
			
			if (mipsEnabled){
				valueTypeVector[3] = DUMMY_MIPS_ID;
				// >=Bappea_V1.25_PEC
				if (!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD))
				{
					valueTypeVector[4] = DUMMY_CPU_CLOCK_ID;
				}
			}
			
			trace.setValueTypes(this.parseValueTypes(valueTypeVector));
			
			PecSample s = null;			
			while(true)
			{
				s = readSample(dis,s);
				trace.addSample(s);
			}
		}
		catch (IOException ioe)
		{
			//TODO: should we log this or handle it?
			//my guess is this signifies the end of file
		}
	}

	private PecSample readSample(DataInputStream dis,PecSample prevSample) throws IOException
	{
		int headerByte = 0;
		int negBitOffset = 0;
		int sampleBitOffset = 0;
		
		if (this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
			headerByte = (dis.readByte() << 24) >>> 24;
		}
		// >=Bappea_V1.25_PEC
		else {
			int headerByte1 = (dis.readByte() << 24) >>> 24;
			int headerByte2 = (dis.readByte() << 24) >>> 24;
			headerByte = ((headerByte1) | (headerByte2) << 8);
			negBitOffset = 1;
			sampleBitOffset = 2;
		}
		
		int neg0 = 0;
		int neg1 = 0;
		int neg2 = 0;
		int neg3 = 0;
		
		int prev0 = 0;
		int prev1 = 0;
		int prev2 = 0;
		int prev3 = 0;
		
		if(prevSample != null)
		{
			prev0 = prevSample.values[0];
			prev1 = prevSample.values[1];
			prev2 = prevSample.values[2]/64;
			prev3 = processorSpeed;
		}
		if(debug) if(this.time > 7820 && this.time < 7830) System.out.println("header: "+Long.toHexString(headerByte)+" = "+Integer.toBinaryString(headerByte));  //$NON-NLS-1$//$NON-NLS-2$
		
		if( ((headerByte >>> 7 + negBitOffset + sampleBitOffset)&1) != 0)
		{
			neg0 = 1;
		}
		
		if( ((headerByte >>> 6 + negBitOffset + sampleBitOffset)&1) != 0)
		{
			neg1 = 1;
		}
		
		if( ((headerByte >>> 5 + negBitOffset + sampleBitOffset)&1) != 0)
		{
			neg2 = 1;
		}
		
		// >=Bappea_V1.25_PEC
		if(!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD) && ((headerByte >>> 7)&1) != 0) {
			neg3 = 1;
		}
	
		int len0 = (((headerByte >> 3 + sampleBitOffset) << 30) >>> 30)+1;
		int len1 = (((headerByte >> 1 + sampleBitOffset) << 30) >>> 30)+1;
		int len2 = ((((headerByte >> sampleBitOffset) << 31) >>> 31)+1)*2;
		int len3 = -1;
		// >=Bappea_V1.25_PEC
		if(!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
			len3 = ((((headerByte) << 30) >>> 30)+1);
		}
		
		if(debug) {
			if(!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
				if(debug) if(this.time > 7820 && this.time < 7830) System.out.println("T:"+this.time+" len0:"+len0+" len1:"+len1+" len2:"+len2+" len3:"+len3); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				if(debug) if(this.time > 7900 && this.time < 7900) System.out.println("H: "+Integer.toBinaryString(headerByte)+" N0:"+neg0+" N1:"+neg1+" N2:"+neg2+" N3:"+neg3);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

			} else {
				if(debug) if(this.time > 7820 && this.time < 7830) System.out.println("T:"+this.time+" len0:"+len0+" len1:"+len1+" len2:"+len2); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if(debug) if(this.time > 7900 && this.time < 7900) System.out.println("H: "+Integer.toBinaryString(headerByte)+" N0:"+neg0+" N1:"+neg1+" N2:"+neg2);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
			}
		}
		
		long val0 = readVal(neg0,len0,dis);
		long val1 = readVal(neg1,len1,dis);
		long val2 = readVal(neg2,len2,dis);
		long val3 = -1;
		
		// >=Bappea_V1.25_PEC
		if(!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
			if (mipsEnabled) {
				val3 = readVal(neg3,len3,dis);
			}
		}
		
		if(debug) {
			if(!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
				if(this.time > 7820 && this.time < 7900) System.out.println("READ T:"+this.time+"   V0:"+Long.toHexString(val0)+" V1:"+Long.toHexString(val1)+" V2:"+Long.toHexString(val2)+" V3:"+Long.toHexString(val3));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			} else {
				if(this.time > 7820 && this.time < 7900) System.out.println("READ T:"+this.time+"   V0:"+Long.toHexString(val0)+" V1:"+Long.toHexString(val1)+" V2:"+Long.toHexString(val2));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
			}
		}	
		
		val0 = prev0-val0;
		val1 = prev1-val1;
		val2 = prev2-val2;
		
		// >=Bappea_V1.25_PEC
		if(!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
			val3 = prev3-val3;
			processorSpeed = (int)val3;
		}
		
		int[] values;
		if (!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD) && mipsEnabled) {
			values = new int[]{(int)val0,(int)val1,((int)val2)*64, (int)((val3 / 1000000) * val1 / (val2*64)), ((int)(val3 / 1000000))};
		} else if (mipsEnabled) {
			values = new int[]{(int)val0,(int)val1,((int)val2)*64, (int)(processorSpeed * val1 / (val2*64))};
		} else {
			values = new int[]{(int)val0,(int)val1,((int)val2)*64};
		}
		
		PecSample ps = new PecSample(values, this.time);
		
		if (debug) {
			if(!this.traceVersion.equals(DATA_FORMAT_VERSION_OLD)) {
				if(this.time > 7820 && this.time < 7900) System.out.println("T:"+this.time+"   V0:"+Long.toHexString(val0)+" V1:"+Long.toHexString(val1)+" V2:"+Long.toHexString(val2)+" V3:"+Long.toHexString(val3));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				if(this.time > 7820 && this.time < 7900) System.out.println("T:"+this.time+" "+val0+" "+val1+" "+val2+" "+val3);   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4//$NON-NLS-5$$
			} else {
				if(this.time > 7820 && this.time < 7900) System.out.println("T:"+this.time+"   V0:"+Long.toHexString(val0)+" V1:"+Long.toHexString(val1)+" V2:"+Long.toHexString(val2));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if(this.time > 7820 && this.time < 7900) System.out.println("T:"+this.time+" "+val0+" "+val1+" "+val2);   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
			}
		}
		this.time++;
		
		return ps;
	}

	private long readVal(int neg,int len,DataInputStream dis) throws IOException
	{
		byte[] array = new byte[len];
		dis.read(array);
		
		if(debug)
			for(int k=0;k<array.length;k++)
			{
				if(this.time > 7820 && this.time < 7900) System.out.println(" "+Integer.toHexString(array[k])); //$NON-NLS-1$
			}
		
		long total = 0;
		for(int i=0;i<len;i++)
		{
			int value = ((array[i] << 24) >>> 24) << (i*8);			
			total |= value;
		}
		
		if(debug) if(this.time > 7820 && this.time < 7900) System.out.println("\n"+Long.toHexString(total)); //$NON-NLS-1$

		if(debug) if(this.time > 7820 && this.time < 7900) System.out.println("T:"+this.time+" "+Long.toHexString(total));  //$NON-NLS-1$//$NON-NLS-2$
		
		if(neg != 0) total = ~total;
		
		return total;
	}
		
	/**
	 * Converts the given event type identifiers into the appropriate event type strings.
	 * @param valueTypeIntegers list of event type identifiers
	 * @return String[] of event type strings
	 */
	private String[] parseValueTypes(Integer[] valueTypeIntegers){
		String[] s = new String[valueTypeIntegers.length];
		for (int i = 0; i < valueTypeIntegers.length; i++) {
			s[i] = convertValueType(valueTypeIntegers[i]);
		}
		
		return s;
	}
	
	private String convertValueType(int value) {
		String s = EVENT_TYPE_TABLE.get(Integer.valueOf(value));
		if (s == null){
			s = String.format(
					Messages.PecTraceParser_22,
					value);			
		}
		return s;

	}
	private long readCpuClockRate(DataInputStream dis) throws IOException
	{	
		long result = dis.readUnsignedByte();
		result += dis.readUnsignedByte() << 8;
		result += dis.readUnsignedByte() << 16;
		result += dis.readUnsignedByte() << 24;
		return result;
	}
}
