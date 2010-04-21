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

package com.nokia.carbide.cpp.pi.memory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.pi.priority.PriSample;
import com.nokia.carbide.cpp.pi.priority.PriThread;
import com.nokia.carbide.cpp.pi.priority.PriTrace;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class MemTraceParser extends Parser
{
    private boolean debug = false;
    private String version;
    
	private Vector<MemThread> memSamples = new Vector<MemThread>();
	private Vector<PriThread> priSamples = new Vector<PriThread>();
	private char[] buffer = new char[80];
	private String threadName;
	private String processName;
	private int threadId;
	private int sampleTime;
	private int heapSize;
	private int stackSize;
	private int processPriority;
	private int threadPriority;
	private int defaultSamplingTime = 3000;
	private int samplingTime;
	private MemSample sample;
	private MemTrace memTrace;
	private PriSample priSample;
	private PriTrace priTrace;
	private byte a, b, c, d;
	private int readCount = 0;

	// constants
	public static int SAMPLE_CODE_INITIAL_CHUNK = 0;
	public static int SAMPLE_CODE_NEW_CHUNK = 1;
	public static int SAMPLE_CODE_UPDATE_CHUNK = 2;
	public static int SAMPLE_CODE_DELETE_CHUNK = 3;
	
	public MemTraceParser() //throws Exception
	{
	}
	
	public ParsedTraceData parse(File file) throws Exception 
	{
		if (!file.exists() || file.isDirectory())
	    {
	      throw new Exception(Messages.getString("MemTraceParser.cannotOpenTraceFile")); //$NON-NLS-1$
	    }
		if (debug)
			System.out.println(Messages.getString("MemTraceParser.traceFileLength") + file.length()); //$NON-NLS-1$

	    parseMemTrace(file);
	    int versionNumber = convertVersionStringToInt(version);
	    memTrace.setVersion(versionNumber);
	    
	    samplingTime = calcSamplingTime();
		
		ParsedTraceData ptd = new ParsedTraceData();
		ptd.traceData = this.getTrace();
		
		/* TODO some debug prints remove
		MemTrace memTrace = (MemTrace)ptd.traceData;
		int ii = 1;
		for (Enumeration e = memTrace.getSamples(); e.hasMoreElements(); )
		{
			MemSample memSample = (MemSample) e.nextElement();

			
			if(memSample.thread.threadId == 0xc81dc318){
				System.out.println("Threadname: " + memSample.thread.threadName +  ", time:" + Long.toString(memSample.sampleSynchTime) + ", stack:" + Integer.toString(memSample.stackSize) + ", heap:" + Integer.toString(memSample.heapSize));
				ii++;
			}
		}*/
		
		return ptd;
	}
	
	private void parseNewMemFile(String version,DataInputStream dis) throws Exception
	{
		if (version.equals("0.85") || version.equals("0.91") || version.equals("1.00")||version.equals("1.10")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{
			parseV085MemFile(dis);
		}
		else throw new Exception(Messages.getString("MemTraceParser.traceVersionException1") + version //$NON-NLS-1$
				                 + Messages.getString("MemTraceParser.traceVersionException2")); //$NON-NLS-1$
	}
	
	private void parseV085MemFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		if (debug)System.out.println(Messages.getString("MemTraceParser.readVersionDebug") + new String(version)); //$NON-NLS-1$
		this.version = new String(version);
		
		this.readV085MemSample(dis);
	}
	
	private void parseV110MemFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		if (debug) System.out.println(Messages.getString("MemTraceParser.readVersionDebug") + new String(version)); //$NON-NLS-1$
		this.version = new String(version);
		
		this.readV110MemSample(dis);
	}

	private void parseV155MemFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		if (debug) System.out.println(Messages.getString("MemTraceParser.readVersion")+new String(version)); //$NON-NLS-1$
		this.version = new String(version);
		
		this.readV155MemSample(dis);
	}
	
	private void parseV157MemFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		if (debug) System.out.println("Read version "+new String(version));
		this.version = new String(version);
		
		this.readV157MemSample(dis);
	}
	
	private void parseV110PriFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		if (debug) System.out.println(Messages.getString("MemTraceParser.readVersionDebug") + new String(version)); //$NON-NLS-1$
		this.version = new String(version);
		
		this.readV110PriSample(dis);
	}

	public String getProfilerVersion()
	{
	    return version;
	}
	
	private void readV085MemSample(DataInputStream dis) throws Exception
	{
	    Vector<long[]> rawV085Samples = new Vector<long[]>();
	    Vector<long[]> lastSampleRaw = new Vector<long[]>();

		memTrace = new MemTrace();
	    priTrace = new PriTrace();
	   		
		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);
				
		// then read if there is thread name data
		int length = dis.readUnsignedByte();readCount++;
		if (length != 1)
			throw new Exception(Messages.getString("MemTraceParser.parseErrorTypeMissing")); //$NON-NLS-1$
		
		int mode = dis.readUnsignedByte(); readCount++;

		try
		{
			// read the first length
			length = dis.readUnsignedByte(); readCount++;

			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			while(true)
			{
				//System.out.println("ReadCount:"+this.readCount);
				if (length == 0) //haxor =)
				{
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					
					length = dis.readUnsignedByte(); readCount++;
					
					if (length == 4)
					{
						// there was only the sample header here
						System.out.println(Messages.getString("MemTraceParser.missingSampleNumber") + sample); //$NON-NLS-1$
						mode = 0x00;
					}
					else if (length != 1)
					{
						throw new Exception(Messages.getString("MemTraceParser.parseErrorWrongLength") + length); //$NON-NLS-1$
					}
					else
					{
						lastSampleRaw.clear();
					}
				}
				
				if (length == 1)
				{
					mode = dis.readUnsignedByte(); readCount++;

					// read the next length
					length = dis.readUnsignedByte(); readCount++;

					lastSampleRaw.clear(); //lisätty 16.11.05 -aniskane
				}
				
				if (mode == 0xaa)
				{
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);
				   	int index = rawName.indexOf(':');
				   	if (index != -1)
				   	{
				   		processName = rawName.substring(0,index);
				   		threadName = rawName.substring(rawName.lastIndexOf(':')+ 1, rawName.length());
				   	}
				   	else
				   	{
				   		processName = rawName;
				   		threadName = rawName;
				   	}
								    	
					memTrace.addFirstSynchTime(Integer.valueOf((int)threadId),Integer.valueOf((int)sample));
				   	memSamples.add(new MemThread(Integer.valueOf((int)threadId), threadName, processName));
				   	priTrace.addFirstSynchTime(Integer.valueOf((int)threadId), Integer.valueOf(sampleTime));
				   	priSamples.add(new PriThread(Integer.valueOf((int)threadId), threadName, processName));

				   	// read the next length
					length = dis.readUnsignedByte(); readCount++;
					//System.out.println("Next length " + length);
				}
				else if (mode == 0xdd)
				{
					// reading data
					long[] elements = this.readDataElements085Mem(dis, length, sample);
					
					rawV085Samples.add(elements);
					lastSampleRaw.add(elements);

					// read the next length
					length = dis.readUnsignedByte(); readCount++;
				}
				else if (mode == 0x00)
				{
					// duplicate the previous sample to replace the missing sample
					Enumeration<long[]> e = lastSampleRaw.elements();
					while(e.hasMoreElements())
					{
						long[] element = e.nextElement();
						long[] newElement = new long[7];
						newElement[0] = element[0];
						newElement[1] = element[1];
						newElement[2] = element[2];
						newElement[3] = element[3];
						newElement[4] = element[4];
						newElement[5] = element[5];
						// replace the sample number with the missing one
						newElement[6] = sample;
						// add the duplicated sample
						rawV085Samples.add(newElement);
					}
					
					// read the length, it is already known
					sample = readTUint(dis);
					System.out.println(Messages.getString("MemTraceParser.sampleRead") + sample); //$NON-NLS-1$
					length = dis.readUnsignedByte(); readCount++;;
				}
				else
				{
					throw new Exception(Messages.getString("MemTraceParser.parseErrorWrongMode") + mode); //$NON-NLS-1$
				}
			}
		}
		catch (EOFException e)
		{
			if (debug) System.out.println(Messages.getString("MemTraceParser.finishedReading")); //$NON-NLS-1$
			this.addRawV085Samples(rawV085Samples);
		}
	}

	private void readV110MemSample(DataInputStream dis) throws Exception
	{
	    Vector<long[]> rawV110MemSamples = new Vector<long[]>();
	    Vector<long[]> lastSampleRaw = new Vector<long[]>();

		memTrace = new MemTrace();

		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);

		// then read if there is thread name data
		int length = dis.readUnsignedByte(); readCount++;
		if (length != 1)
			throw new Exception(Messages.getString("MemTraceParser.parseErrorTypeMissing")); //$NON-NLS-1$
		
		int mode = dis.readUnsignedByte(); readCount++;

		try
		{
			length = dis.readUnsignedByte(); readCount++;
			
			while(true)
			{
				if (length == 0) 
				{
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					
					length = dis.readUnsignedByte(); readCount++;

					if (length == 4)
					{
						// there was only the sample header here
						System.out.println(Messages.getString("MemTraceParser.missingSampleNumber") + sample); //$NON-NLS-1$
						mode = 0x00;
					}
					else if (length != 1)
					{
						throw new Exception(Messages.getString("MemTraceParser.parseErrorWrongLength") + length); //$NON-NLS-1$
					}
					else
					{
						lastSampleRaw.clear();
					}
				}
				
				if (length == 1)
				{
					mode = dis.readUnsignedByte(); readCount++;

					// read the next length
					length = dis.readUnsignedByte(); readCount++;

					lastSampleRaw.clear(); //lisätty 16.11.05 -aniskane
				}
				
				if (mode == 0xaa)
				{
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);

				   	int index = rawName.indexOf(':');
				   	if (index != -1)
				   	{
				   		processName = rawName.substring(0,index);
				   		threadName = rawName.substring(rawName.lastIndexOf(':') + 1, rawName.length());
				   	}
				   	else
				   	{
				   		processName = rawName;
				   		threadName = rawName;
				   	}
								    	
					memTrace.addFirstSynchTime(Integer.valueOf((int)threadId), Integer.valueOf((int)sample));
				   	memSamples.add(new MemThread(Integer.valueOf((int)threadId), threadName, processName));

					length = dis.readUnsignedByte(); readCount++;
				}
				else if (mode == 0xdd)
				{
					// reading data
					long[] elements = this.readDataElements110Mem(dis, length, sample);
					
					rawV110MemSamples.add(elements);
					lastSampleRaw.add(elements);

					// read the next length
					length = dis.readUnsignedByte(); readCount++;
				}
				else if (mode == 0x00)
				{
					// duplicate the previous sample to replace the missing sample
					Enumeration<long[]> e = lastSampleRaw.elements();
					while(e.hasMoreElements())
					{
						long[] element = e.nextElement();
						long[] newElement = new long[7];
						newElement[0] = element[0];
						newElement[1] = element[1];
						newElement[2] = element[2];
						newElement[3] = element[3];
						newElement[4] = element[4];

						// replace the sample number with the missing one
						newElement[5] = sample;

						// add the duplicated sample
						rawV110MemSamples.add(newElement);
					}
					
					// read the length, it is already known
					sample = readTUint(dis);
					if (debug) System.out.println(Messages.getString("MemTraceParser.sampleRead") + sample); //$NON-NLS-1$
					length = dis.readUnsignedByte();readCount++;;
				}
				else
				{
					throw new Exception(Messages.getString("MemTraceParser.parseErrorWrongMode")+mode); //$NON-NLS-1$
				}
			}
		}
		catch (EOFException e)
		{
			System.out.println(Messages.getString("MemTraceParser.finishedReading")); //$NON-NLS-1$
			this.addRawV110MemSamples(rawV110MemSamples);
		}
	}

	private void readV155MemSample(DataInputStream dis) throws Exception
	{
	    Vector<long[]> rawV155MemSamples = new Vector<long[]>();
	    Vector<long[]> lastSampleRaw = new Vector<long[]>();

		memTrace = new MemTrace();

		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);
				
		// then read if there is thread name data
		int length = dis.readUnsignedByte();readCount++;
		if (length != 1) throw new Exception(Messages.getString("MemTraceParser.missingType")); //$NON-NLS-1$
		
		int mode = dis.readUnsignedByte();readCount++;

		try
		{
			length = dis.readUnsignedByte();readCount++;
			
			while(true)
			{
				if (length == 0) 
				{
					if (debug)System.out.println(Messages.getString("MemTraceParser.newSample")); //$NON-NLS-1$
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					
					length = dis.readUnsignedByte();readCount++;
					
					if (length == 4)
					{
						// there was only the sample header here
						System.out.println(Messages.getString("MemTraceParser.missingSampleNum")+sample); //$NON-NLS-1$
						mode = 0x00;
					}
					else if (length != 1)
					{
						throw new Exception(Messages.getString("MemTraceParser.wrongLength")+length); //$NON-NLS-1$
					}
					else
					{
						lastSampleRaw.clear();
					}
				}
				
				if (length == 1)
				{
					mode = dis.readUnsignedByte();readCount++;

					// read the next length
					length = dis.readUnsignedByte();readCount++;
					if (debug)System.out.println(Messages.getString("MemTraceParser.nextLength")+length); //$NON-NLS-1$
					lastSampleRaw.clear(); //lisätty 16.11.05 -aniskane
				}
				
				if (mode == 0xaa)
				{
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);
					if (debug)System.out.println(Messages.getString("MemTraceParser.rawName1")+rawName+Messages.getString("MemTraceParser.rawName2")+threadId); //$NON-NLS-1$ //$NON-NLS-2$
				   	int index = rawName.indexOf(':');
				   	if (index != -1)
				   	{
				   		processName = rawName.substring(0,index);
				   		//threadName = rawName.substring(rawName.lastIndexOf(':')+1,rawName.length());
				   		threadName = rawName.substring(rawName.indexOf(':')+2,rawName.length());
				   	}
				   	else
				   	{
				   		processName = rawName;
				   		threadName = rawName;
				   	}
					if (processName.startsWith("T_")) //$NON-NLS-1$
					{
						processName = processName.substring(2);
						threadName += "_T"; //$NON-NLS-1$
					}
					else if (processName.startsWith("C_")) //$NON-NLS-1$
					{
						processName = processName.substring(2);
						threadName += "_C"; //$NON-NLS-1$
					}
					memTrace.addFirstSynchTime(Integer.valueOf((int)threadId),Integer.valueOf((int)sample));
				   	memSamples.add(new MemThread(Integer.valueOf((int)threadId), threadName, processName));

					length = dis.readUnsignedByte();readCount++;
				}
				else if (mode == 0xdd)
				{
					// reading data
					long[] elements = this.readDataElements110Mem(dis,length,sample);
					
					//if (debug)System.out.println(" sample: "+sample);
//if ((elements[0] > 0xffff || elements[0] < 0) && ((int)elements[0] != 0xbabbeaaa))
//					System.out.println(   "Chunk Data"
//					+ ",pointer address,0x" + Integer.toHexString((int)elements[0])
//					+ ",c.iSize," + elements[1]
//					+ ",c.iAttributes,0x" + Integer.toHexString((int)elements[2])
//					+ ",memory model," + (elements[4] & 0xf)
//					+ ",time," + elements[5]);
//else System.out.println(   "Thread Data"
//		+ ",t.Id," + elements[0]
//		+ ",t.iUserStackSize," + elements[1]
//		+ ",t.iUserStackRunAddress,0x" + Integer.toHexString((int)elements[2])
//		+ ",time," + elements[5]);
					
					rawV155MemSamples.add(elements);
					lastSampleRaw.add(elements);
					
					// read the length, it is already known
					//sample = readTUint(dis);

					// read the next length
					length = dis.readUnsignedByte();readCount++;
				}
				else if (mode == 0x00)
				{
					// read the length, it is already known
					sample = readTUint(dis);
					System.out.println(Messages.getString("MemTraceParser.nextSampleNum")+sample); //$NON-NLS-1$
					length = dis.readUnsignedByte();readCount++;;
				}
				else
				{
					throw new Exception(Messages.getString("MemTraceParser.wrongMode")+mode); //$NON-NLS-1$
				}
			}
		}
		catch (EOFException e)
		{
			System.out.println(Messages.getString("MemTraceParser.readingDone")); //$NON-NLS-1$
			this.addRawV155MemSamples(rawV155MemSamples);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void readV157MemSample(DataInputStream dis) throws Exception {
	    Vector rawV157MemSamples = new Vector();
	    int counter = 0;
	    String createdThread = "";
	    
		memTrace = new MemTrace();

		MemThread thread;
		
		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);
				
		// then read if there is thread name data
		int length = dis.readUnsignedByte();readCount++;
		if(length != 1) throw new Exception("Parse error, type after sample missing");
		
		int mode = dis.readUnsignedByte();readCount++;

		try {
			length = dis.readUnsignedByte();readCount++;
			
			while(true) {
				if(length == 0) {
//					if(debug) System.out.println("Length = 0, New sample");
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					
					length = dis.readUnsignedByte();readCount++;
					
					if(length == 4) {
						// there was only the sample header here
						System.out.println("Missing sample #"+sample);
						mode = 0x00;
					}
					else if(length != 1) {
						throw new Exception("Parse error, wrong length "+length+", readCount: "+readCount);
					}
//					else {
//						lastSampleRaw.clear();
//					}
				}
				
				if(length == 1) {
					mode = dis.readUnsignedByte();readCount++;

					// read the next length
					length = dis.readUnsignedByte();readCount++;
				}
				
				if(mode == 0xaa) {
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);
					
					if(debug) System.out.println("New raw name "+rawName+" at #"+sample+", thread id: "+Long.toHexString(threadId));

					int index = rawName.indexOf(':');
				   	if(index != -1) {
				   		processName = rawName.substring(0,index);
				   		//threadName = rawName.substring(rawName.lastIndexOf(':')+1,rawName.length());
				   		threadName = rawName.substring(rawName.indexOf(':')+2,rawName.length());
				   	}
				   	else {
				   		processName = rawName;
				   		threadName = rawName;
				   	}
					if (processName.startsWith("T_")) {
						processName = processName.substring(2);
						threadName += "_T";
					}
					else if (processName.startsWith("C_")) {
						processName = processName.substring(2);
						threadName += "_C";
					}
					else if (processName.startsWith("L_")) //$NON-NLS-1$
					{
						processName = processName.substring(2);
						threadName += "_L"; //$NON-NLS-1$
				
					}
					memTrace.addFirstSynchTime(Integer.valueOf((int)threadId),Integer.valueOf((int)sample));
					thread = new MemThread(Integer.valueOf((int)threadId), threadName, processName);
					memSamples.add(thread);
			   		createdThread = processName+"::"+threadName;
			   		if(debug)System.out.println("Name sample: "+sample+", created thread: "+createdThread+", vector size: "+memSamples.size());
				   	length = dis.readUnsignedByte();readCount++;
				}
				else if(mode == 0xdd) {
					long[] elements = this.readDataElements110Mem(dis,length,sample);
					if(elements[0] != (int)0xffffffffbabbeaaaL || counter == 0) {
						elements[6] = MemTraceParser.SAMPLE_CODE_INITIAL_CHUNK;
					} else if (elements[0] == (int)0xffffffffbabbeaaaL){
						elements[6] = MemTraceParser.SAMPLE_CODE_UPDATE_CHUNK;
					}
					MemThread tempThr = ((MemThread)memSamples.get(counter));
					// reading data
					elements[7] = sum(tempThr.processName+"::"+tempThr.threadName);
					if(debug)System.out.println("Data sample: "+sample+", hash: "+elements[7]+", created thread: "+tempThr.processName+"::"+tempThr.threadName);
					
					rawV157MemSamples.add(elements);

					// read the next length
					length = dis.readUnsignedByte();readCount++;
					createdThread = "";
					
					// hack since total memory code added for top of both thread and chunk lists
					if(elements[0] != (int)0xffffffffbabbeaaaL || counter == 0) {
						counter++;
					}
				}
				else if(mode == 0xda) {	// new chunk code found
					// read the chunk data
					long[] elements = this.readDataElements110Mem(dis,length,sample);
					elements[6] = MemTraceParser.SAMPLE_CODE_NEW_CHUNK;
					elements[7] = sum(createdThread);
					if(debug) System.out.println("New chunk #"+Long.toHexString(elements[0])+", with name: "+createdThread+" found @"+sample);
					
					rawV157MemSamples.add(elements);
					
					// read the next length
				   	length = dis.readUnsignedByte();readCount++;
				}
				else if(mode == 0xdb) { // chunk update code found
					// read the chunk data
					long[] elements = this.readDataElements110Mem(dis,length,sample);
					elements[6] = MemTraceParser.SAMPLE_CODE_UPDATE_CHUNK;
					if(debug) System.out.println("Chunk #"+Long.toHexString(elements[0])+" update found #"+sample);
					
					rawV157MemSamples.add(elements);
					// read the next length
					length = dis.readUnsignedByte();readCount++;
					createdThread = "";
				}
				else if(mode == 0xdc) { // chunk remove code found
					// read the chunk data
					long[] elements = this.readDataElements110Mem(dis,length,sample);
					elements[6] = MemTraceParser.SAMPLE_CODE_DELETE_CHUNK;

					if (debug) System.out.println("Chunk #"+Long.toHexString(elements[0])+" remove found #"+sample);
					
					rawV157MemSamples.add(elements);
					// read the next length
					length = dis.readUnsignedByte();readCount++;
					createdThread = "";
				}
				else if(mode == 0x00) {
					// duplicate the previous sample to replace the missing sample
					
					// read the length, it is already known
					sample = readTUint(dis);
					if(debug) System.out.println("Read next sample #"+sample);
					length = dis.readUnsignedByte();readCount++;;
				}
				else
				{
					throw new Exception("Parse error, wrong mode "+mode);
				}
			}
		}
		catch (EOFException e)
		{
			System.out.println("Finished reading");
			this.addRawV157MemSamples(rawV157MemSamples);
		}
	}
	
	/*
	 * A method for calculating hash value for a string 
	 */
	public static long sum(String arg) {		
		int total = 0;		
		for(int i = 0; i < arg.length(); i++){			
			total += (long)arg.charAt(i);		
			}		return total; // returns the sum of the chars after cast	
	}

	@SuppressWarnings("unchecked")
	private void addRawV157MemSamples(Vector rawV157MemSamples) {

		Hashtable<Integer, String> startedThreads;	// store the id/name of the thread for recognizing the right chunk (same chunk id may be reused)

		String tempThreadName = "";
		String str = "";
		int chunkMode = MemTraceParser.SAMPLE_CODE_INITIAL_CHUNK;
		long nameHash = 0;
	    MemThread[] memThreads = new MemThread[memSamples.size()];
	    memSamples.copyInto((MemThread[]) memThreads);
	    memTrace.setThreads(memThreads);

	    // container for started threads, some of the chunks may share the id (i.e. the chunk start address)
	    startedThreads = new Hashtable(memSamples.size());
	    
	    Enumeration rawEnum = rawV157MemSamples.elements();
	    
	    while(rawEnum.hasMoreElements()) {
	    	long[] element = (long[])rawEnum.nextElement();
	    	threadId = (int)element[0];
	    	heapSize = (int)element[3];
	    	stackSize = (int)element[4];
	    	sampleTime = (int)element[5];
	    	chunkMode = (int)element[6];	// chunk mode for deciding the periods of memory usage per thread/chunk 
	    	nameHash = element[7];			// for checking the right thread/raw sample pairs
	    	
	    	// total memory consumption
	    	if(element[0] == 0xffffffffbabbeaaaL) {
	    		int totalRam = (int)element[1];
	    		int freeRam = (int)element[3];
	    		
	    		heapSize = totalRam-freeRam;
	    		stackSize = freeRam;
		    	if(debug) System.out.println("Used RAM: " + heapSize+" B, free RAM: "+stackSize+" B");
	    	}
	    	
	    	// handle different sample types 
	    	switch(chunkMode) {
		    	case 0: // MemTraceParser.SAMPLE_CODE_INITIAL_CHUNK
		    		// search for a correct sample for the ID of a raw sample
			    	for (Enumeration e = memSamples.elements();e.hasMoreElements();) {
			    		MemThread mt = (MemThread)e.nextElement();
			    		
			    		// find corresponding thread ID among the MemThreads
			    		if (mt.threadId.intValue() == threadId ) {
			    			// create a new sample to memory trace
			    			if(mt.threadName.endsWith("_L")){
			    				sample = new MemSample(mt, (int)element[1], (int)element[2], heapSize, sampleTime, MemTraceParser.SAMPLE_CODE_INITIAL_CHUNK);
			    			}else{
			    				sample = new MemSample(mt, heapSize, stackSize, sampleTime, MemTraceParser.SAMPLE_CODE_INITIAL_CHUNK);
			    			}			    	
			    		
			    			tempThreadName = mt.processName+"::"+mt.threadName;
			    			
			    			// add the thread into started hash table
			    			startedThreads.put((int)threadId, (String)tempThreadName);
			    			
		    				memTrace.addSample(sample);
			    			break;
			    		}
			    	}
		    		break;
		    	case 1: // MemTraceParser.SAMPLE_CODE_NEW_CHUNK
		    		// add to the started threads hash table
			    	for (Enumeration e = memSamples.elements();e.hasMoreElements();) {
			    		MemThread mt = (MemThread)e.nextElement();
		    			tempThreadName = mt.processName+"::"+mt.threadName;
			    		
			    		// find corresponding thread ID among the MemThreads
			    		if (mt.threadId.intValue() == threadId && sum(tempThreadName) == nameHash) {
			    			// create a new sample to memory trace
			    			if(mt.threadName.endsWith("_L")){
			    				sample = new MemSample(mt, (int)element[1], (int)element[2], heapSize, sampleTime, MemTraceParser.SAMPLE_CODE_NEW_CHUNK);
			    			}else{
			    				sample = new MemSample(mt, heapSize, stackSize, sampleTime, MemTraceParser.SAMPLE_CODE_NEW_CHUNK);
			    			}
			    			
			    			// add the thread into started hash table
			    			startedThreads.put((int)threadId, (String)tempThreadName);

			    			// add sample to main trace data structure
		    				memTrace.addSample(sample);
			    			break;
			    		} 
			    	}
		    		break;
		    	case 2: // MemTraceParser.SAMPLE_CODE_UPDATE_CHUNK
		    		str = (String)startedThreads.get(threadId);
		    		if(str == null)
		    			str = "";
		    		// add to the started threads hash table
			    	for (Enumeration e = memSamples.elements();e.hasMoreElements();) {
			    		MemThread mt = (MemThread)e.nextElement();
		    			tempThreadName = mt.processName+"::"+mt.threadName;
		    			
			    		// find corresponding thread ID among the MemThreads
			    		if (mt.threadId.intValue() == threadId ) {
			    			if((str).equalsIgnoreCase(tempThreadName)) {
				    			// create a new sample to memory trace
			    				if(mt.threadName.endsWith("_L")){
				    				sample = new MemSample(mt, (int)element[1], (int)element[2], heapSize, sampleTime, MemTraceParser.SAMPLE_CODE_UPDATE_CHUNK);
				    			}else{
				    				sample = new MemSample(mt, heapSize, stackSize, sampleTime, MemTraceParser.SAMPLE_CODE_UPDATE_CHUNK);
				    			}			    			
				    			
				    			// add sample to main trace data structure
			    				memTrace.addSample(sample);
				    			break;
			    			}
			    		} 
			    	}
		    		break;
		    	case 3: // MemTraceParser.SAMPLE_CODE_DELETE_CHUNK
		    		// add to the started threads hash table
			    	for (Enumeration e = memSamples.elements();e.hasMoreElements();) {
			    		MemThread mt = (MemThread)e.nextElement();
		    			tempThreadName = mt.processName+"::"+mt.threadName;
			    		
			    		// find corresponding thread ID among the MemThreads
			    		if (mt.threadId.intValue() == threadId) {
			    			String thread = startedThreads.get(threadId);
			    			if(thread == null){
			    				if(mt.threadName.endsWith("_L")){
				    				sample = new MemSample(mt, (int)element[1], (int)element[2], heapSize, sampleTime, MemTraceParser.SAMPLE_CODE_DELETE_CHUNK);
				    				// add sample to main trace data structure
				    				memTrace.addSample(sample);
				    				break;
			    				}			    			
			    			}			    			
			    			else if(thread.equalsIgnoreCase(tempThreadName)) {
				    			// create a new sample to memory trace
			    				if(mt.threadName.endsWith("_L")){
				    				sample = new MemSample(mt, (int)element[1], (int)element[2], heapSize, sampleTime, MemTraceParser.SAMPLE_CODE_DELETE_CHUNK);
				    			}else{
				    				sample = new MemSample(mt, heapSize, stackSize, sampleTime, MemTraceParser.SAMPLE_CODE_DELETE_CHUNK);
				    			}
				    			
					    		// remove from the started threads hash table
					    		if(startedThreads.remove(threadId) == null) {
					    			System.out.println(" No match on started list");
				    			}
				    			
				    			// add sample to main trace data structure
			    				memTrace.addSample(sample);
				    			break;
			    			} 
			    		}
			    	}
		    		break;
		    	default:
		    		break;
	    	}
	    	
	    	// add end mark for the thread
    		memTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));

	    }
	    if (debug) System.out.println("Done parsing, size: "+memTrace.samples.size());
	}
	
	private void addRawV110MemSamples(Vector<long[]> rawV110MemSamples)
	{
	    MemThread[] memTreads = new MemThread[memSamples.size()];
	    memSamples.copyInto((MemThread[]) memTreads);
	    memTrace.setThreads(memTreads);
	    
	    Enumeration<long[]> rawEnum = rawV110MemSamples.elements();
	    while(rawEnum.hasMoreElements())
	    {
	    	long[] element = rawEnum.nextElement();
	    	threadId = (int)element[0];
	    	heapSize = (int)element[3];	// NOTE: in older versions this was element[1];
	    	stackSize = (int)element[4];
	    	sampleTime = (int)element[5]; 

	    	for (Enumeration<MemThread> e = memSamples.elements(); e.hasMoreElements(); )
	    	{
	    		MemThread mt = e.nextElement();
	    		if (mt.threadId.intValue() == threadId)
	    		{
	    			sample = new MemSample(mt, heapSize, stackSize, sampleTime);
	    			memTrace.addSample(sample);
	    			break;
	    		}
	    	}
	    	
	    	memTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    }
	    if (debug) System.out.println(Messages.getString("MemTraceParser.parsingDone")); //$NON-NLS-1$
	}
	
	private int calcV155SamplingTime(Vector<long[]> rawV155MemSamples)
	{
		long[] element = rawV155MemSamples.firstElement();
		int time = (int)element[5];
		Enumeration<long[]> rawEnum = rawV155MemSamples.elements();
	    while(rawEnum.hasMoreElements())
	    {
	    	element = rawEnum.nextElement();
	    	if (time != (int)element[5])
	    	{
	    		time = (int)element[5] - time;
	    		break;
	    	}
	    }
	    return time;
	}

	private void addRawV155MemSamples(Vector<long[]> rawV155MemSamples) throws Exception
	{
		//int largest = 0;
	    MemThread[] memTreads = new MemThread[memSamples.size()];
	    memSamples.copyInto((MemThread[]) memTreads);
	    memTrace.setThreads(memTreads);

	    int samplingTime = calcV155SamplingTime(rawV155MemSamples);
	    
	    Enumeration<long[]> rawEnum = rawV155MemSamples.elements();
	    
	    while(rawEnum.hasMoreElements())
	    {
	    	long[] element = rawEnum.nextElement();
	    	threadId = (int)element[0];
	    	heapSize = (int)element[3];
	    	stackSize = (int)element[4];
	    	sampleTime = (int)element[5];
	    	
	    	if (element[0] == 0xffffffffbabbeaaaL)
	    	{
	    		int totalRam = (int)element[1];
	    		int freeRam = (int)element[3];
	    		
	    		heapSize = totalRam-freeRam;
	    		stackSize = freeRam;
	    	}
	    	else
	    	{
	    		if (element[0] == 0xffffffffbabbea20L && this.traceVersion.startsWith("Bappea_V2.01")) //$NON-NLS-1$
	    		{
		    		if (this.traceVersion.startsWith("Bappea_V2.01") && memTrace.getMemoryModel() == MemTrace.MEMORY_UNKNOWN) //$NON-NLS-1$
		    		{
		    			memTrace.setMemoryModel(element[2]);
		    		}
	    		}
	    		else
	    		{
			    	if (Math.abs(threadId) > 10000)
			    	{
			    		sampleTime -= samplingTime;
			    	}
		    	
			    	if (sampleTime % (samplingTime*2) != samplingTime) {
			    		System.out.println(Messages.getString("MemTraceParser.invalidSampleNum") + sampleTime); //$NON-NLS-1$
			    		throw new Exception(Messages.getString("MemTraceParser.invalidSample")); //$NON-NLS-1$
			    	}
	    		}
	    	}

	    	for (Enumeration<MemThread> e = memSamples.elements();e.hasMoreElements();)
	    	{
	    		MemThread mt = e.nextElement();
	    		if (mt.threadId.intValue() == threadId)
	    		{
	    			sample = new MemSample(mt, heapSize, stackSize, sampleTime);

	    			if (element[0] != 0xffffffffbabbeaaaL)
	    			{
	    				memTrace.addSample(sample);
	    			}
	    			else if ((sampleTime/samplingTime)%2 != 0)
	    			{
	    				memTrace.addSample(sample);
	    			}
	    			break;
	    		}
	    	}
	    	
	    	memTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    }

	    if (debug) System.out.println(Messages.getString("MemTraceParser.sizeOfParse")+memTrace.samples.size()/*+" largest "+largest*/); //$NON-NLS-1$
	}
	
	private void addRawV085Samples(Vector<long[]> rawV085Samples)
	{
	    MemThread[] memTreads = new MemThread[memSamples.size()];
	    memSamples.copyInto((MemThread[]) memTreads);
	    memTrace.setThreads(memTreads);
	    
	    PriThread[] priThreads = new PriThread[priSamples.size()];
	    priSamples.copyInto((PriThread[]) priThreads);
	    priTrace.setThreads(priThreads);
	    
	    Enumeration<long[]> rawEnum = rawV085Samples.elements();
	    while(rawEnum.hasMoreElements())
	    {
	    	long[] element = rawEnum.nextElement();
	    	threadId = (int)element[0];
	    	threadPriority = (int)element[5];

	    	// for the null thread, converts 8bit unsigned to signed
	    	if (threadPriority > 32768)
	    	    threadPriority = threadPriority - 65536;
	    	heapSize = (int)element[3];	// NOTE: in older versions this was element[1];
	    	stackSize = (int)element[4];
	    	sampleTime = (int)element[6]; 

	    	for (Enumeration<MemThread> e = memSamples.elements(); e.hasMoreElements(); )
	    	{
	    		MemThread mt = e.nextElement();
	    		if (mt.threadId.intValue() == threadId)
	    		{
	    			sample = new MemSample(mt, heapSize, stackSize, sampleTime);
	    			memTrace.addSample(sample);
	    			break;
	    		}
	    	}
	    	
	    	for (Enumeration<PriThread> e = priSamples.elements(); e.hasMoreElements(); )
	    	{
	    		PriThread pt = e.nextElement();
	    		if (pt.threadId.intValue() == threadId)
	    		{
	    			//int priority = solveAbsolutPriority(processPriority, threadPriority);
	    			priSample = new PriSample(pt, threadPriority, sampleTime);
	    			priTrace.addSample(priSample);
	    			break;
	    		}
	    	}
	    	memTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    	priTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    }
	    if (debug) System.out.println(Messages.getString("MemTraceParser.parsingDone")); //$NON-NLS-1$
	}

	private void readV110PriSample(DataInputStream dis) throws Exception
	{
	    Vector<long[]> rawV110PriSamples = new Vector<long[]>();
	    Vector<long[]> lastSampleRaw = new Vector<long[]>();

		//memTrace = new MemTrace();
	    priTrace = new PriTrace();
	   		
		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);
				
		// then read if there is thread name data
		int length = dis.readUnsignedByte(); readCount++;
		if (length != 1)
			throw new Exception(Messages.getString("MemTraceParser.parseErrorTypeMissing")); //$NON-NLS-1$
		
		int mode = dis.readUnsignedByte(); readCount++;

		try
		{
			// read the first length
			length = dis.readUnsignedByte(); readCount++;

			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

			while(true)
			{
				if (length == 0)
				{
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					
					length = dis.readUnsignedByte(); readCount++;
					
					if (length == 4)
					{
						// there was only the sample header here
						System.out.println(Messages.getString("MemTraceParser.missingSampleNumber") + sample); //$NON-NLS-1$
						mode = 0x00;
					}
					else if (length != 1)
					{
						throw new Exception(Messages.getString("MemTraceParser.parseErrorWrongLength") + length); //$NON-NLS-1$
					}
					else
					{
						lastSampleRaw.clear();
					}
				}
				
				if (length == 1)
				{
					mode = dis.readUnsignedByte(); readCount++;

					// read the next length
					length = dis.readUnsignedByte(); readCount++;

					lastSampleRaw.clear(); //lisätty 16.11.05 -aniskane
				}
				
				if (mode == 0xaa)
				{
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);

				   	int index = rawName.indexOf(':');
				   	if (index != -1)
				   	{
				   		processName = rawName.substring(0,index);
				   		threadName = rawName.substring(rawName.lastIndexOf(':') + 1, rawName.length());
				   	}
				   	else
				   	{
				   		processName = rawName;
				   		threadName = rawName;
				   	}

				   	priTrace.addFirstSynchTime(Integer.valueOf((int)threadId), Integer.valueOf(sampleTime));
				   	priSamples.add(new PriThread(Integer.valueOf((int)threadId), threadName, processName));

				   	// read the next length
					length = dis.readUnsignedByte(); readCount++;
				}
				else if (mode == 0xdd)
				{
					// reading data
					long[] elements = this.readDataElements110Pri(dis, length, sample);
					
					rawV110PriSamples.add(elements);
					lastSampleRaw.add(elements);

					// read the next length
					length = dis.readUnsignedByte(); readCount++;
				}
				else if (mode == 0x00)
				{
					// duplicate the previous sample to replace the missing sample
					Enumeration<long[]> e = lastSampleRaw.elements();
					while(e.hasMoreElements())
					{
						long[] element = e.nextElement();
						long[] newElement = new long[7];
						newElement[0] = element[0];
						newElement[1] = element[1];
						//newElement[2] = element[2];
						//newElement[3] = element[3];
						//newElement[4] = element[4];
						//newElement[5] = element[5];
						// replace the sample number with the missing one
						newElement[2] = sample;
						// add the duplicated sample
						rawV110PriSamples.add(newElement);
					}
					
					// read the length, it is already known
					sample = readTUint(dis);
					System.out.println(Messages.getString("MemTraceParser.sampleRead") + sample); //$NON-NLS-1$
					length = dis.readUnsignedByte(); readCount++;;
				}
				else
				{
					throw new Exception(Messages.getString("MemTraceParser.parseErrorWrongMode") + mode); //$NON-NLS-1$
				}
			}
		}
		catch (EOFException e)
		{
			System.out.println(Messages.getString("MemTraceParser.finishedReading")); //$NON-NLS-1$
			this.addRawV110PriSamples(rawV110PriSamples);
		}
	}

	private void addRawV110PriSamples(Vector<long[]> rawV110PriSamples)
	{
	    PriThread[] priThreads = new PriThread[priSamples.size()];
	    priSamples.copyInto((PriThread[]) priThreads);
	    priTrace.setThreads(priThreads);
	    
	    Enumeration<long[]> rawEnum = rawV110PriSamples.elements();
	    while(rawEnum.hasMoreElements())
	    {
	    	long[] element = rawEnum.nextElement();
	    	threadId = (int)element[0];
	    	threadPriority = (int)element[1];

	    	// for the null thread, converts 8bit unsigned to signed
	    	if (threadPriority > 32768)
	    	    threadPriority = threadPriority - 65536;

	    	sampleTime = (int)element[2]; 

	    	for (Enumeration<PriThread> e = priSamples.elements(); e.hasMoreElements(); )
	    	{
	    		PriThread pt = e.nextElement();
	    		if (pt.threadId.intValue() == threadId)
	    		{
	    			priSample = new PriSample(pt, threadPriority, sampleTime);
	    			priTrace.addSample(priSample);
	    			break;
	    		}
	    	}

	    	priTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    }
	    if (debug) System.out.println(Messages.getString("MemTraceParser.parsingDone")); //$NON-NLS-1$
	}

	private long[] readDataElements085Mem(DataInputStream dis,int length,long sampleTime) throws Exception
	{
		if (length != 0)
		{
			long[] elements = new long[7];
			elements[0] = this.readTUint(dis);
			elements[1] = this.readTUint(dis);
			elements[2] = this.readTUint(dis);
			elements[3] = this.readTUint(dis);
			elements[4] = this.readTUint(dis);
			elements[5] = this.readShort(dis);
			elements[6] = sampleTime;

			return elements;
		}
		else 
		{
			System.out.println(Messages.getString("MemTraceParser.dataLengthIs0")); //$NON-NLS-1$
			return null;
		}
	}

	private long[] readDataElements110Mem(DataInputStream dis,int length,long sampleTime) throws Exception
	{
		if (length != 0)
		{
			long[] elements = new long[8]; // added two elements for sample type and name hash, 07092009
			elements[0] = this.readTUint(dis);
			elements[1] = this.readTUint(dis);
			elements[2] = this.readTUint(dis);
			elements[3] = this.readTUint(dis);
			elements[4] = this.readTUint(dis);
			elements[5] = sampleTime;
			
			if (elements[3] != 0 && elements[4] != 0)
				if (debug) System.err.println(Messages.getString("MemTraceParser.bothHeapAndStack")); //$NON-NLS-1$

			return elements;
		}
		else 
		{
			System.out.println(Messages.getString("MemTraceParser.dataLengthIs0")); //$NON-NLS-1$
			return null;
		}
	}

	private long[] readDataElements110Pri(DataInputStream dis, int length, long sampleTime) throws Exception
	{
		if (length != 0)
		{
			long[] elements = new long[3];
			elements[0] = this.readTUint(dis);  //thread ID
			elements[1] = this.readShort(dis);  //Priority
			elements[2] = sampleTime;  //sample number

			return elements;
		}
		else 
		{
			System.out.println(Messages.getString("MemTraceParser.dataLengthIs0")); //$NON-NLS-1$
			return null;
		}
	}
	
	private String readThreadName(DataInputStream dis, int length) throws Exception
	{
		if (length != 0)
		{
			length -=4;

			byte[] element = new byte[length];
			dis.read(element, 0, length);
			readCount+=length;
			return new String(element);
		}
		else
			return null;
	}
	
	private byte[] readElementWithLength(DataInputStream dis) throws Exception
	{
		byte length = dis.readByte(); readCount++;
		if (length != 0)
		{

			byte[] element = new byte[length];
			dis.read(element, 0, length);
			readCount += length;
			return element;
		}
		else return null;
	}
	
	private long readShort(DataInputStream dis) throws Exception
	{
		long result = dis.readUnsignedByte();
		readCount++;
		result += dis.readUnsignedByte() << 8;
		readCount++;
		return result;
	}
	
	private long readTUint(DataInputStream dis) throws Exception
	{
		long result = dis.readUnsignedByte();
		readCount++;
		result += dis.readUnsignedByte() << 8;
		readCount++;
		result += dis.readUnsignedByte() << 16;
		readCount++;
		result += dis.readUnsignedByte() << 24;
		readCount++;
		return result;
	}

	private long readTUintWithLength(DataInputStream dis) throws Exception
	{
		byte length = (byte)dis.readUnsignedByte();
		readCount++;

		if (length != 4) 
		{
			throw new Exception(Messages.getString("MemTraceParser.parseErrorTUint1") + length + Messages.getString("MemTraceParser.parseErrorTUint2")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		long result = dis.readUnsignedByte();
		readCount++;
		result += dis.readUnsignedByte() << 8;
		readCount++;
		result += dis.readUnsignedByte() << 16;
		readCount++;
		result += dis.readUnsignedByte() << 24;
		readCount++;
		
		return result;
	}

	private void parseMemTrace(File file) throws Exception
	{
		DataInputStream dis = new DataInputStream(new FileInputStream(file));		
	    byte[] traceArray = new byte[(int)file.length()];
	    dis.readFully(traceArray);
	    
	    // test the mem trace version
	    if (traceArray.length > 257)
	    {
	    	String s = new String(traceArray,1,traceArray[0]);
	    	if (!s.startsWith("Bappea_V2.01") && !s.startsWith("Bappea_V1.56")) //up to 1.10 version //$NON-NLS-1$ //$NON-NLS-2$
	    	{
	    		if (s.startsWith("Bappea_V1.10")) //$NON-NLS-1$
	    		{
		    	    //String version = s.substring(8,12);
		    	    parseVersion110Trace(traceArray,s);

		    	    return;
	    		} 	
	    		else if(s.startsWith("Bappea_V2.02")) 
	    		{
		    		parseVersion157Trace(traceArray,s);
		    		return;
		    	} 
	    		else if(s.startsWith("Bappea_V2.03")) 
	    		{
		    		parseVersion157Trace(traceArray,s);
		    		return;
		    	} 

	    		String version = s.substring(8, 12);
	    		String traceType = s.substring(13, s.length());
	    		System.out.println(Messages.getString("MemTraceParser.foundVersion1") + version + Messages.getString("MemTraceParser.foundVersion2") + traceType + Messages.getString("MemTraceParser.foundVersion3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		if (!traceType.equals("MEM")) //$NON-NLS-1$
	    		{
	    			GeneralMessages.showErrorMessage(Messages.getString("MemTraceParser.wrongTraceType1") + traceType + Messages.getString("MemTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$
	    			throw new Exception(Messages.getString("MemTraceParser.wrongTraceType1") + traceType + Messages.getString("MemTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$
	    		}
	    		else
	    		{
	    		    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    		    dis = new DataInputStream(bais);
	    			parseNewMemFile(version,dis);
	    			return;
	    		}
	    	}
	    	else //handling 1.55 and newer versions
	    	{
	    		this.traceVersion = s;
	    	    parseVersion155Trace(traceArray,s);

	    	    return;
	    	}
	    }
	    System.out.println(Messages.getString("MemTraceParser.parsingOldVersion")); //$NON-NLS-1$
	    
	    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    dis = new DataInputStream(bais);
	    
	    int listLength = this.findThreadListLength(dis, traceArray.length);
	    if (listLength == -1)
	    {
	    	GeneralMessages.showErrorMessage(Messages.getString("MemTraceParser.invalidTraceFileOverflow")); //$NON-NLS-1$
	    	System.out.println(Messages.getString("MemTraceParser.invalidTraceFile")); //$NON-NLS-1$
	    	throw new Exception(Messages.getString("MemTraceParser.invalidTraceFile")); //$NON-NLS-1$
	    }
	    memTrace = new MemTrace();
	    priTrace = new PriTrace();
	    
	    for (int i = 0; i < listLength; )
	    {
	    	int j = 0;
	    	for (j = 0; j < buffer.length; j++)
	    	{
	    		byte tmpByte = dis.readByte();
	    		if (tmpByte != 0)
	    		{
	    			buffer[j] = (char)tmpByte;
	    		}
	    		else
	    		{
	    			dis.skipBytes(buffer.length - j - 1);
	    			break;
	    		}
	    	}
	    	
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
	    	threadId = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    			  
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
	    	sampleTime = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	String unparsedName = new String(buffer);
	    	int index = 0;
	    	while ((unparsedName.charAt(index++) != ':') && (index < buffer.length)){}
	    	processName = unparsedName.substring(0, index - 1);
	    	threadName = unparsedName.substring(index + 1,  j);

	    	memTrace.addFirstSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    	memSamples.add(new MemThread(Integer.valueOf(threadId), threadName, processName));
	    	priTrace.addFirstSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    	priSamples.add(new PriThread(Integer.valueOf(threadId), threadName, processName));

	    	i += buffer.length + 8;
	    }
	    MemThread[] threads = new MemThread[memSamples.size()];
	    memSamples.copyInto((MemThread[]) threads);
	    memTrace.setThreads(threads);
	    PriThread[] priThreads = new PriThread[priSamples.size()];
	    priSamples.copyInto((PriThread[]) priThreads);
	    priTrace.setThreads(priThreads);
	    dis.skipBytes(8); //skipping "LIST_END" text
	    for (int k = 0; k < traceArray.length - listLength - 8; )
	    {
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();

	    	threadId = ((c & 0xff) << 8) | (d & 0xff);
	    	processPriority = b;
			threadPriority = a;
	    	
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
	    	heapSize = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
	    	stackSize = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
	    	sampleTime = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	for (Enumeration<MemThread> e = memSamples.elements(); e.hasMoreElements(); )
	    	{
	    		MemThread mt = e.nextElement();
	    		if (mt.threadId.intValue() == threadId)
	    		{
	    			sample = new MemSample(mt, heapSize, stackSize, sampleTime);
	    			memTrace.addSample(sample);
	    			break;
	    		}
	    	}
	    	for (Enumeration<PriThread> e = priSamples.elements(); e.hasMoreElements(); )
	    	{
	    		PriThread pt = e.nextElement();
	    		if (pt.threadId.intValue() == threadId)
	    		{
	    			int priority = solveAbsolutPriority(processPriority, threadPriority);
	    			priSample = new PriSample(pt, priority, sampleTime);
	    			priTrace.addSample(priSample);
	    			break;
	    		}
	    	}
	    	memTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    	priTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleTime));
	    	k += 16;
	    }
	    if (debug) System.out.println(Messages.getString("MemTraceParser.parsingDone")); //$NON-NLS-1$
	    
	}
	
	private void parseVersion110Trace(byte[] traceArray, String version_data) throws Exception
	{
	    
		String version = version_data.substring(8, 12);
	    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    DataInputStream dis = new DataInputStream(bais);

    	if (version_data.indexOf("MEM") > 0 && version_data.indexOf("PRI") > 0) //version 1.10 or newer //$NON-NLS-1$ //$NON-NLS-2$
    	{
    	    parseNewMemFile(version,dis);
    	}
    	else if (version_data.indexOf("MEM") > 0) //only memtrace //$NON-NLS-1$
    	{
    	    this.parseV110MemFile(dis);
    	}
    	else if (version_data.indexOf("PRI") > 0) //only pritrace //$NON-NLS-1$
    	{
    	    this.parseV110PriFile(dis);
    	}
	}
	
	private void parseVersion155Trace(byte[] traceArray, String version_data) throws Exception
	{
    	if (version_data.indexOf("MEM")>0) //version 1.55 or newer //$NON-NLS-1$
    	{
    	    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
    	    DataInputStream dis = new DataInputStream(bais);

    	    this.parseV155MemFile(dis);
    	}
	}

	private void parseVersion157Trace(byte[] traceArray, String version_data) throws Exception
	{
	    
//		String version = version_data.substring(8,12);
//		String traceType = version_data.substring(13,version_data.length());
	    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    DataInputStream dis = new DataInputStream(bais);

    	if (version_data.indexOf("MEM")>0) //version 1.57 or newer
    	{
    	    this.parseV157MemFile(dis);
    	}
	}
	
	private int calcSamplingTime()
	{
	    long time;
	    
	    if (memTrace != null) //if memtrace exists
	    {
	        time = memTrace.getFirstSampleNumber();
	        for (Enumeration e = memTrace.getSamples(); e.hasMoreElements(); )
	        {
	            MemSample tmp = (MemSample) e.nextElement();
	            if (tmp.sampleSynchTime != time)
	            {
	                time = tmp.sampleSynchTime - time;
	                return (int) time;
	            }
	        }
	    return defaultSamplingTime;
	    }
	    else if (priTrace != null) //if pritrace exists
	    {
	        time = priTrace.getFirstSampleNumber();
	        for (Enumeration e = priTrace.getSamples(); e.hasMoreElements(); )
	        {
	            PriSample tmp = (PriSample) e.nextElement();
	            if (tmp.sampleSynchTime != time)
	            {
	                time = tmp.sampleSynchTime - time;
	                return (int) time;
	            }
	        }
	    }
	    //else
	    System.out.println(Messages.getString("MemTraceParser.shouldNotHappen")); //$NON-NLS-1$
	    return 0;
	}
	
	private GenericSampledTrace getTrace()
	{
		return (GenericSampledTrace) memTrace;
	}
	
	public ParsedTraceData getPriorityTrace()
	{
	    if (priTrace != null)
	    {
	        if (priTrace.getPriSample(0).priority != 0)
	        {
	            priTrace.setSamplingTime(samplingTime);
	            ParsedTraceData ptd = new ParsedTraceData();
	    		ptd.traceData = priTrace;
	    		return ptd;
	        }
	    }

		return null;
	}
	
	private int solveAbsolutPriority(int processPriority, int threadPriority)
	{
		int tPriority = threadPriority * 10 - 30;
		int pPriority = processPriority * 10;
		switch (tPriority)
		{
			case 100:		//EPriorityAbsoluteVeryLow
				return 100;
			case 200:		//EPriorityAbsoluteLow
				return 200;
			case 300:		//EPriorityAbsoluteBackground
				return 300;
			case 400:		//EPriorityAbsoluteForeground
				return 400;
			case 500:		//EPriorityAbsoluteHigh
				return 500;
			case -30:		//EPriorityNull
				return 0;
			case 30:		//EPriorityRealTime
				return 850;
			case -20:		//EPriorityMuchLess
				return pPriority - 20;
			case -10:		//EPriorityLess
				return pPriority - 10;
			case 0:			//EPriorityNormal
				return pPriority;
			case 10:		//EPriorityMore
				return pPriority + 10;
			case 20:		//EPriorityMuchMore
				return pPriority + 20;
			default:
				return 0;
		}
	}

	private int findThreadListLength(DataInputStream dis, int traceLength) throws Exception
	{
		char[] buf = new char[8];
		for (int i = 0; i < traceLength / 8; i++)
		{
			for (int j = 0; j < 8; j++)
			{
				buf[j % 8] = (char)dis.readByte();
			}
			if (new String(buf).matches("LIST_END")) //$NON-NLS-1$
			{
				dis.reset();
				return i * 8;
			}
		}
		dis.reset();
		return -1;
	}
	
	private int convertVersionStringToInt(String version){
		// Coverts version number from string to int
		int i = 0;
		int versionInt = 0;
		String versionString = "";
		
		// goes string thru and copies all digits into another string
		while( i < version.length() ){
			if( Character.isDigit(version.charAt(i)) ){
				versionString += version.charAt(i);
			}
			i++;
			
		}
		
		// convert string to int
		try {
			versionInt = Integer.parseInt(versionString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionInt;
	}
}
