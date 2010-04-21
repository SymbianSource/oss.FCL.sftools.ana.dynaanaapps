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

package com.nokia.carbide.cpp.pi.internal.ipc;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;


/**
 * Parser for performance counter event traces in binary format.
 * Converts parsed content into IpcSamples.
 */
public class IpcTraceParser extends Parser {
	private static final String IPC_EVENT_NOT_RECOGNISED = Messages.IpcTraceParser_0;
	private static final int MAX_IPC_EVENTS_TRACED = 14; //2 IPC events * 5 possible values + 4 optional L2 counter values
	private static final int MAX_IPC_NO_L2_EVENTS_TRACED = 10; //2 IPC events * 5 possible values
	
	
	//private static int CPU_CLOCK_TICK_DIV64 = 0xFFFF;
	private boolean debug = false; //CH: this could be replaced with Eclipse-style logging
	private boolean l2Enabled = false;
	
	long time = 0;
	
	private static final Map<Integer, String> eventTypeTable = new HashMap<Integer, String>();
	static {
		eventTypeTable.put(0x0,Messages.IpcTraceParser_1);
		eventTypeTable.put(0x1,Messages.IpcTraceParser_2);
		eventTypeTable.put(0x2,Messages.IpcTraceParser_3);
		eventTypeTable.put(0x3,Messages.IpcTraceParser_4);
		eventTypeTable.put(0x4,Messages.IpcTraceParser_5);
	}
	private static final Map<Integer, String> counterTypeTable = new HashMap<Integer, String>();
	static {
		counterTypeTable.put(0x0,Messages.IpcTraceParser_6);
		counterTypeTable.put(0x1,Messages.IpcTraceParser_7);
		counterTypeTable.put(0x2,Messages.IpcTraceParser_8);
		counterTypeTable.put(0x3,Messages.IpcTraceParser_9);
		counterTypeTable.put(0x4,Messages.IpcTraceParser_10);
		counterTypeTable.put(0x5,Messages.IpcTraceParser_11);
	}	
	private static final Map<Integer, String> l2EventTypeTable = new HashMap<Integer, String>();
	static {
		l2EventTypeTable.put(0x3,Messages.IpcTraceParser_12);
		l2EventTypeTable.put(0x4,Messages.IpcTraceParser_13);
		l2EventTypeTable.put(0x5,Messages.IpcTraceParser_14);
		l2EventTypeTable.put(0x6,Messages.IpcTraceParser_15);
		l2EventTypeTable.put(0x7,Messages.IpcTraceParser_16);
		l2EventTypeTable.put(0xC,Messages.IpcTraceParser_17);
		l2EventTypeTable.put(0xD,Messages.IpcTraceParser_18);
	}			

	/* (non-Javadoc)
	 * @see com.nokia.carbide.cpp.internal.pi.model.Parser#parse(java.io.File)
	 */
	@Override
	public ParsedTraceData parse(File file) throws Exception 
	{
		ParsedTraceData ptd = new ParsedTraceData();
		ptd.functionResolvers = null;
		ptd.staticData = null;
		ptd.traceData = new IpcTrace();
		doParsing(file,(IpcTrace)ptd.traceData);
		time = 0;
		
		return ptd;
	}
	
	private void doParsing(File f, IpcTrace trace) throws FileNotFoundException
 {
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		try {
			int len = dis.readByte();
			byte[] versionString = new byte[len];
			dis.read(versionString);
			this.traceVersion = new String(versionString);
			if (debug){
				System.out.println("IPC trace version:" + this.traceVersion); //$NON-NLS-1$				
			}

			List<Integer> counterTypes = new ArrayList<Integer>();
			List<Integer> counterL2Types = new ArrayList<Integer>();

			// IPC counters
			int firstCounter = dis.readUnsignedByte();
			counterTypes.add(firstCounter);
			int secondCounter = dis.readUnsignedByte();
			counterTypes.add(secondCounter);

			// this type is always the cpu clock tick div 64
			// int thirdData = CPU_CLOCK_TICK_DIV64;
			// valueTypeVector.add(new Integer(thirdData));

			if (dis.readUnsignedByte() != 0) {
				// L2 counters, add if available
				int thirdCounter = dis.readUnsignedByte();
				counterL2Types.add(thirdCounter);
				int fourthCounter = dis.readUnsignedByte();
				counterL2Types.add(fourthCounter);
				int fifthCounter = dis.readUnsignedByte();
				counterL2Types.add(fifthCounter);
				int sixthCounter = dis.readUnsignedByte();
				counterL2Types.add(sixthCounter);

				trace.setValueTypes(this.parseValueTypes(counterTypes, counterL2Types));
			} else {
				trace.setValueTypes(this.parseValueTypes(counterTypes, null));
			}

			// System.out.println("IPC first "+firstCounter+" second "+secondCounter);

			IpcSample s = null;
			while (true) {
				s = readSample(dis, s);
				// System.out.println(this.time+" "+Long.toHexString(s.values[0])+" "+Long.toHexString(s.values[1])+" "+Long.toHexString(s.values[2]));
				trace.addSample(s);
			}
		} catch (IOException ioe) {
			// CH: is this just caught for end-of-file condition?
		}
	}

	private IpcSample readSample(DataInputStream dis, IpcSample prevSample)
			throws IOException {
		// int headerByte = (dis.readInt()); // << 24) >>> 24;
		int headerByte1 = (dis.readByte() << 24) >>> 24;
		int headerByte2 = (dis.readByte() << 24) >>> 24;
		int headerByte3 = (dis.readByte() << 24) >>> 24;
		int headerByte4 = (dis.readByte() << 24) >>> 24;
		int headerByte = ((((headerByte1) | (headerByte2) << 8) | (headerByte3) << 16) | headerByte4 << 24);

		int headerByte11 = 0;
		int headerByte12 = 0;
		int headerByteL2 = 0;
		// int j = ((headerByte >>> 30)&1);
		if (((headerByte >>> 30) & 1) != 0 || l2Enabled) // flag bit in trace =>
															// L2 trace included
		{
			l2Enabled = true;
			headerByte11 = (dis.readByte() << 24) >>> 24;
			headerByte12 = (dis.readByte() << 24) >>> 24; // overflow bits
			headerByteL2 = ((headerByte11) | ((headerByte12) << 8));
		}

		int eventNumber = l2Enabled ? MAX_IPC_EVENTS_TRACED
				: MAX_IPC_NO_L2_EVENTS_TRACED;

		int[] neg = new int[eventNumber];
		int[] prev = new int[eventNumber];

		if (prevSample != null) {
			prev[0] = prevSample.values[0];
			prev[1] = prevSample.values[1];
			prev[2] = prevSample.values[2];
			prev[3] = prevSample.values[3];
			prev[4] = prevSample.values[4];
			prev[5] = prevSample.values[5];
			prev[6] = prevSample.values[6];
			prev[7] = prevSample.values[7];
			prev[8] = prevSample.values[8];
			prev[9] = prevSample.values[9];
			if (l2Enabled) {
				prev[10] = prevSample.values[10];
				prev[11] = prevSample.values[11];
				prev[12] = prevSample.values[12];
				prev[13] = prevSample.values[13];
			}
		}
		// /*if(debug) if(this.time > 7820 && this.time < 7830)*/
		// System.out.println("header: "+Long.toHexString(headerByte)+" = "+Integer.toBinaryString(headerByte));

		if (((headerByte >>> 29) & 1) != 0) {
			neg[0] = 1;
		}

		if (((headerByte >>> 28) & 1) != 0) {
			neg[1] = 1;
		}

		if (((headerByte >>> 27) & 1) != 0) {
			neg[2] = 1;
		}
		if (((headerByte >>> 26) & 1) != 0) {
			neg[3] = 1;
		}

		if (((headerByte >>> 25) & 1) != 0) {
			neg[4] = 1;
		}

		if (((headerByte >>> 24) & 1) != 0) {
			neg[5] = 1;
		}
		if (((headerByte >>> 23) & 1) != 0) {
			neg[6] = 1;
		}

		if (((headerByte >>> 22) & 1) != 0) {
			neg[7] = 1;
		}

		if (((headerByte >>> 21) & 1) != 0) {
			neg[8] = 1;
		}
		if (((headerByte >>> 20) & 1) != 0) {
			neg[9] = 1;
		}

		// check overflows of L2 counters
		if (l2Enabled) {
			if (((headerByteL2 >>> 11) & 1) != 0) {
				neg[10] = 1;
			}

			if (((headerByteL2 >>> 10) & 1) != 0) {
				neg[11] = 1;
			}

			if (((headerByteL2 >>> 9) & 1) != 0) {
				neg[12] = 1;
			}
			if (((headerByteL2 >>> 8) & 1) != 0) {
				neg[13] = 1;
			}

		}

		int[] len = new int[eventNumber];
		len[0] = (((headerByte >> 18) << 30) >>> 30) + 1;
		len[1] = (((headerByte >> 16) << 30) >>> 30) + 1;
		len[2] = (((headerByte >> 14) << 30) >>> 30) + 1;
		len[3] = (((headerByte >> 12) << 30) >>> 30) + 1;
		len[4] = (((headerByte >> 10) << 30) >>> 30) + 1;
		len[5] = (((headerByte >> 8) << 30) >>> 30) + 1;
		len[6] = (((headerByte >> 6) << 30) >>> 30) + 1;
		len[7] = (((headerByte >> 4) << 30) >>> 30) + 1;
		len[8] = (((headerByte >> 2) << 30) >>> 30) + 1;
		len[9] = (((headerByte) << 30) >>> 30) + 1;

		if (l2Enabled) {
			len[10] = (((headerByteL2 >> 6) << 30) >>> 30) + 1;
			len[11] = (((headerByteL2 >> 4) << 30) >>> 30) + 1;
			len[12] = (((headerByteL2 >> 2) << 30) >>> 30) + 1;
			len[13] = ((headerByteL2 << 30) >>> 30) + 1;
		}
		if (debug)
			if (this.time > 7820 && this.time < 7830)
				System.out.println("T:" + this.time + " len0:" + len[0] + " len1:" + len[1] + " len2:" + len[2]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (debug)
			if (this.time > 7900 && this.time < 7900)
				System.out.println("H: " + Integer.toBinaryString(headerByte) + " N0:" + neg[0] + " N1:" + neg[1] + " N2:" + neg[2]); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$

		int[] val = new int[eventNumber];
		val[0] = readVal(neg[0], len[0], dis);
		val[1] = readVal(neg[1], len[1], dis);
		val[2] = readVal(neg[2], len[2], dis);
		val[3] = readVal(neg[3], len[3], dis);
		val[4] = readVal(neg[4], len[4], dis);
		val[5] = readVal(neg[5], len[5], dis);
		val[6] = readVal(neg[6], len[6], dis);
		val[7] = readVal(neg[7], len[7], dis);
		val[8] = readVal(neg[8], len[8], dis);
		val[9] = readVal(neg[9], len[9], dis);

		if (l2Enabled) {
			val[10] = readVal(neg[10], len[10], dis);
			val[11] = readVal(neg[11], len[11], dis);
			val[12] = readVal(neg[12], len[12], dis);
			val[13] = readVal(neg[13], len[13], dis);
		}

		if (debug)
			if (this.time > 7820 && this.time < 7900)
				System.out.println("READ T:" + this.time + "   V0:" + Integer.toHexString(val[0]) + " V1:" + Long.toHexString(val[1]) + " V2:" + Long.toHexString(val[2])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		for (int i = 0; i < eventNumber; i++) {
			val[i] = prev[i] - val[i];
		}

		IpcSample ps = new IpcSample(val, this.time);

		if (debug)
			if (this.time > 7820 && this.time < 7900)
				System.out.println("T:" + this.time + "   V0:" + Long.toHexString(val[0]) + " V1:" + Long.toHexString(val[1]) + " V2:" + Long.toHexString(val[2])); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
		if (debug)
			if (this.time > 7820 && this.time < 7900)
				System.out.println("T:" + this.time + " " + val[0] + " " + val[1] + " " + val[2]); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
		this.time++;
		/*
		 * dis.readByte(); dis.readByte(); dis.readByte(); dis.readByte();
		 */
		return ps;
	}

	private int readVal(int neg,int len,DataInputStream dis) throws IOException
	{
		byte[] array = new byte[len];
		dis.read(array);
		
		//System.out.println("array: "+array);
		
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
		
		return (int)total;
	}
		
	private String[] parseValueTypes(List<Integer> counterTypes, List<Integer> counterL2Types){
		String[] s = new String[counterTypes.size() * eventTypeTable.size() + (counterL2Types == null ? 0 : counterL2Types.size())];
		int k = 0;
		for (int i = 0; i < eventTypeTable.size(); i++) {
			String eventType =  eventTypeTable.get(i);
			for (Integer counterType : counterTypes) {
				s[k] = String.format(Messages.IpcTraceParser_20, eventType, convertCounterType(counterType));
				k++;
			}
		}
		
		if (counterL2Types != null){
			for (Integer l2Type : counterL2Types) {
				s[k] = convertL2EventType(l2Type);
				k++;				
			}
		}
		return s;
	}
	
	private String convertL2EventType(Integer value) {
		String s = IpcTraceParser.l2EventTypeTable.get(value);
		if (s == null) {
			s = String.format(IPC_EVENT_NOT_RECOGNISED, value);
		}
		return s;
	}
	
	private String convertCounterType(Integer value) {
		String s = IpcTraceParser.counterTypeTable.get(value);
		if (s == null) {
			s = String.format(IPC_EVENT_NOT_RECOGNISED, value);
		}
		return s;
	}
}
