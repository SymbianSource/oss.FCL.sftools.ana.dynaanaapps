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

package com.nokia.carbide.cpp.pi.priority;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.GenericSampledTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class PriTraceParser extends Parser
{
    private boolean debug = false;

    private String version;
    
	private Vector<PriThread> priSamples = new Vector<PriThread>();
	private char[] buffer = new char[80];
	private String threadName;
	private String processName;
	private int threadId;
	private int sampleNum;
	private int processPriority;
	private int threadPriority;
	private PriSample priSample;
	private PriTrace priTrace;
	private byte a, b, c, d;
	private int readCount = 0;
	
	public PriTraceParser(File file) //throws Exception
	{
	}
	
	public ParsedTraceData parse(File f) throws Exception 
	{
		if (!f.exists() || f.isDirectory())
	    {
	      throw new Exception(Messages.getString("PriTraceParser.cannotOpenMemoryTrace")); //$NON-NLS-1$
	    }
		if (debug) System.out.println(Messages.getString("PriTraceParser.memoryTraceFileLength")+f.length()); //$NON-NLS-1$

	    parsePriTrace(f);
	    
	    if (this.getTrace() == null)
	    	return null;
		
		ParsedTraceData ptd = new ParsedTraceData();
		ptd.traceData = this.getTrace();
		return ptd;
	}
	
	private void parseNewPriFile(String version,DataInputStream dis) throws Exception
	{
		if (version.equals("0.85") || version.equals("0.91") || version.equals("1.00")||version.equals("1.10")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{
			parseV085MemFile(dis);
		}
		else if (version.equals("1.55")) //$NON-NLS-1$
		{
			return;
		}
		else
			throw new Exception(Messages.getString("PriTraceParser.unsupportedMemoryTrace1")+version+Messages.getString("PriTraceParser.unsupportedMemoryTrace2")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void parseV085MemFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		System.out.println(Messages.getString("PriTraceParser.readVersion")+new String(version)); //$NON-NLS-1$
		this.version = new String(version);
		
		this.readV085MemSample(dis);
	}
	
	private void parseV110PriFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		System.out.println(Messages.getString("PriTraceParser.readVersion")+new String(version)); //$NON-NLS-1$
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

		//memTrace = new MemTrace();
	    priTrace = new PriTrace();
	   		
		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);
		//System.out.println("Current sample "+sample);
				
		// then read if there is thread name data
		int length = dis.readUnsignedByte();readCount++;
		if (length != 1) throw new Exception(Messages.getString("PriTraceParser.typeMissing")); //$NON-NLS-1$
		
		int mode = dis.readUnsignedByte();readCount++;
		//System.out.println("First mode "+Integer.toHexString(mode));
		try
		{
			// read the first length
			length = dis.readUnsignedByte();readCount++;
			//System.out.println("First length "+length);
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			while(true)
			{

				//System.out.println("ReadCount:"+this.readCount);
				if (length == 0) //haxor =)
				{
					//System.out.println("Length = 0, New sample");
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					//System.out.println("Sample number:"+sample);
					
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("New length "+length);				
					//break;
					
					if (length == 4)
					{
						// there was only the sample header here
						System.out.println(Messages.getString("PriTraceParser.missingSample")+sample); //$NON-NLS-1$
						mode = 0x00;
					}
					else if (length != 1)
					{
						throw new Exception(Messages.getString("PriTraceParser.wrongLength")+length); //$NON-NLS-1$
					}
					else
					{
						lastSampleRaw.clear();
					}
				}
				
				if (length == 1)
				{
					mode = dis.readUnsignedByte();readCount++;
					//System.out.println("New Mode "+mode+" new length "+length);

					// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
				}
				
				if (mode == 0xaa)
				{
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);
					//System.out.println("Raw name "+rawName);
				   	int index = rawName.indexOf(':');
				   	if (index != -1)
				   	{
				   		processName = rawName.substring(0,index);
				   		threadName = rawName.substring(rawName.lastIndexOf(':')+1,rawName.length());
				   		//System.out.println("Thread: "+threadName+" process: "+processName+" id: "+threadId);
				   	}
				   	else
				   	{
				   		processName = rawName;
				   		threadName = rawName;
				   	}
								    	
				   	priTrace.addFirstSynchTime(new Integer((int)threadId), new Integer(sampleNum));
				   	priSamples.add(new PriThread(new Integer((int)threadId), threadName, processName));

				   	// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
				}
				else if (mode == 0xdd)
				{
					// reading data
					long[] elements = this.readDataElements085Mem(dis,length,sample);
					
					rawV085Samples.add(elements);
					lastSampleRaw.add(elements);

					// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
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
					System.out.println(Messages.getString("PriTraceParser.readNextSample")+sample); //$NON-NLS-1$
					length = dis.readUnsignedByte();readCount++;;
				}
				else
				{
					throw new Exception(Messages.getString("PriTraceParser.wrongMode")+mode); //$NON-NLS-1$
				}
			}
		}
		catch (EOFException e)
		{
			System.out.println(Messages.getString("PriTraceParser.doneReading")); //$NON-NLS-1$
			this.addRawV085Samples(rawV085Samples);
		}
	}
	
	private void addRawV085Samples(Vector<long[]> rawV085Samples)
	{
//	    MemThread[] memTreads = new MemThread[memSamples.size()];
//	    memSamples.copyInto((MemThread[]) memTreads);
//	    memTrace.setThreads(memTreads);
	    
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
	    	sampleNum = (int)element[6]; 

	    	for (Enumeration<PriThread> e = priSamples.elements();e.hasMoreElements();)
	    	{
	    		PriThread pt = e.nextElement();
	    		if (pt.threadId.intValue() == threadId)
	    		{
	    			//int priority = solveAbsolutPriority(processPriority, threadPriority);
	    			priSample = new PriSample(pt, threadPriority, sampleNum);
	    			priTrace.addSample(priSample);
	    			break;
	    		}
	    	}
//	    	memTrace.addLastSynchTime(new Integer(threadId), new Integer(sampleNum));
	    	priTrace.addLastSynchTime(new Integer(threadId), new Integer(sampleNum));
	    }
	    System.out.println(Messages.getString("PriTraceParser.done")); //$NON-NLS-1$
	}

	private void readV110PriSample(DataInputStream dis) throws Exception
	{
	    Vector<long[]> rawV110PriSamples = new Vector<long[]>();
	    Vector<long[]> lastSampleRaw = new Vector<long[]>();

		//memTrace = new MemTrace();
	    priTrace = new PriTrace();
	   		
		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);
		//System.out.println("Current sample "+sample);
				
		// then read if there is thread name data
		int length = dis.readUnsignedByte();readCount++;
		if (length != 1) throw new Exception(Messages.getString("PriTraceParser.typeMissing")); //$NON-NLS-1$
		
		int mode = dis.readUnsignedByte();readCount++;
		//System.out.println("First mode "+Integer.toHexString(mode));
		try
		{
			// read the first length
			length = dis.readUnsignedByte();readCount++;
			//System.out.println("First length "+length);
			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			while(true)
			{

				//System.out.println("ReadCount:"+this.readCount);
				if (length == 0)
				{
					//System.out.println("Length = 0, New sample");
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					//System.out.println("Sample number:"+sample);
					
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("New length "+length);				
					//break;
					
					if (length == 4)
					{
						// there was only the sample header here
						System.out.println(Messages.getString("PriTraceParser.missingSample")+sample); //$NON-NLS-1$
						mode = 0x00;
					}
					else if (length != 1)
					{
						throw new Exception(Messages.getString("PriTraceParser.wrongLength")+length); //$NON-NLS-1$
					}
					else
					{
						lastSampleRaw.clear();
					}
				}
				
				if (length == 1)
				{
					mode = dis.readUnsignedByte();readCount++;
					//System.out.println("New Mode "+mode+" new length "+length);

					// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
				}
				
				if (mode == 0xaa)
				{
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);
					//System.out.println("Raw name "+rawName);
				   	int index = rawName.indexOf(':');
				   	if (index != -1)
				   	{
				   		processName = rawName.substring(0,index);
				   		threadName = rawName.substring(rawName.lastIndexOf(':')+1,rawName.length());
				   		//System.out.println("Thread: "+threadName+" process: "+processName+" id: "+threadId);
				   	}
				   	else
				   	{
				   		processName = rawName;
				   		threadName = rawName;
				   	}
								    	
					//memTrace.addFirstSynchTime(new Integer((int)threadId),new Integer((int)sample));
				   	//memSamples.add(new MemThread(new Integer((int)threadId), threadName, processName));
				   	priTrace.addFirstSynchTime(new Integer((int)threadId), new Integer(sampleNum));
				   	priSamples.add(new PriThread(new Integer((int)threadId), threadName, processName));

				   	// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
				}
				else if (mode == 0xdd)
				{
					// reading data
					long[] elements = this.readDataElements110Pri(dis,length,sample);
					
					/*
					System.out.println("Read elements "+elements);
					if (elements != null)
					{
						for (int i=0;i<elements.length;i++)
							System.out.println("Element #"+i+" "+Long.toHexString(elements[i]));
					}
					*/
					rawV110PriSamples.add(elements);
					lastSampleRaw.add(elements);

					// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
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
					System.out.println(Messages.getString("PriTraceParser.readNextSample")+sample); //$NON-NLS-1$
					length = dis.readUnsignedByte();readCount++;;
				}
				else
				{
					throw new Exception(Messages.getString("PriTraceParser.wrongMode")+mode); //$NON-NLS-1$
				}
			}
		}
		catch (EOFException e)
		{
			System.out.println(Messages.getString("PriTraceParser.doneReading")); //$NON-NLS-1$
			this.addRawV110PriSamples(rawV110PriSamples);
		}
	}

	private void addRawV110PriSamples(Vector<long[]> rawV110PriSamples)
	{
	    //MemThread[] memTreads = new MemThread[memSamples.size()];
	    //memSamples.copyInto((MemThread[]) memTreads);
	    //memTrace.setThreads(memTreads);
	    
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
	    	//heapSize = (int)element[1];
	    	//stackSize = (int)element[4];
	    	sampleNum = (int)element[2]; 
	    	
	    	for (Enumeration<PriThread> e = priSamples.elements();e.hasMoreElements();)
	    	{
	    		PriThread pt = e.nextElement();
	    		if (pt.threadId.intValue() == threadId)
	    		{
	    			//int priority = solveAbsolutPriority(processPriority, threadPriority);
	    			priSample = new PriSample(pt, threadPriority, sampleNum);
	    			priTrace.addSample(priSample);
	    			break;
	    		}
	    	}
//	    	memTrace.addLastSynchTime(new Integer(threadId), new Integer(sampleNum));
	    	priTrace.addLastSynchTime(new Integer(threadId), new Integer(sampleNum));
	    }
	    System.out.println(Messages.getString("PriTraceParser.done")); //$NON-NLS-1$
	}

//	private void readAndPrintData(DataInputStream dis, int length) throws Exception
//	{
//		for (int i=0;i<length;i++)
//			System.out.print(" "+Integer.toHexString(dis.readUnsignedByte()));
//		System.out.print("\n");
//	}

	private long[] readDataElements085Mem(DataInputStream dis,int length,long sampleNum) throws Exception
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
			//System.out.println("Priority: "+elements[5]);
			elements[6] = sampleNum;

			return elements;
		}
		else 
		{
			System.out.println(Messages.getString("PriTraceParser.zeroLength")); //$NON-NLS-1$
			return null;
		}
	}

	private long[] readDataElements110Pri(DataInputStream dis,int length,long sampleNum) throws Exception
	{
		if (length != 0)
		{
			long[] elements = new long[3];
			elements[0] = this.readTUint(dis);  //thread ID
			elements[1] = this.readShort(dis);  //Priority
			elements[2] = sampleNum;  //sample number
			//elements[3] = this.readTUint(dis);
			//elements[4] = this.readTUint(dis);
			//elements[5] = this.readShort(dis); //priority is not read
			//elements[5] = sampleNum;

			return elements;
		}
		else 
		{
			System.out.println(Messages.getString("PriTraceParser.zeroLength")); //$NON-NLS-1$
			return null;
		}
	}
	
	private String readThreadName(DataInputStream dis,int length) throws Exception
	{
		if (length != 0)
		{
			length-=4;
			//System.out.println("Thread name length "+length);
			byte[] element = new byte[length];
			dis.read(element,0,length);
			readCount+=length;
			return new String(element);
		}
		else return null;
	}
	
	private byte[] readElementWithLength(DataInputStream dis) throws Exception
	{
		byte length = dis.readByte(); readCount++;
		if (length != 0)
		{
			//System.out.println("Element length "+length);
			byte[] element = new byte[length];
			dis.read(element,0,length);
			readCount += length;
			return element;
		}
		else return null;
	}
	
//	private long readReversedTUint(DataInputStream dis) throws Exception
//	{
//		long result = dis.readUnsignedByte() << 24;
//		readCount++;
//		result += dis.readUnsignedByte() << 16;
//		readCount++;
//		result += dis.readUnsignedByte() << 8;
//		readCount++;
//		result += dis.readUnsignedByte();
//		readCount++;
//		return result;
//	}
	
//	private long readReversedShort(DataInputStream dis) throws Exception
//	{
//		long result = dis.readUnsignedByte() << 8;
//		readCount++;
//		result += dis.readUnsignedByte();
//		readCount++;
//		return result;
//	}
	
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
		//System.out.println("Pituus: "+length);
		if (length != 4) 
		{
			throw new Exception(Messages.getString("PriTraceParser.TUINTLengthException1")+length+Messages.getString("PriTraceParser.TUINTLengthException2")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		long result = dis.readUnsignedByte();
		readCount++;
		result += dis.readUnsignedByte() << 8;
		readCount++;
		result += dis.readUnsignedByte() << 16;
		readCount++;
		result += dis.readUnsignedByte() << 24;
		readCount++;
		
		//System.out.println("Read length ok");
		return result;
	}

	private void parsePriTrace(File file) throws Exception
	{
		DataInputStream dis = new DataInputStream(new FileInputStream(file));		
	    byte[] traceArray = new byte[(int)file.length()];
	    dis.readFully(traceArray);
	    
	    // test the mem trace version
	    if (traceArray.length > 257)
	    {
	    	String s = new String(traceArray,1,traceArray[0]);
	    	if (!s.startsWith("Bappea_V1.10") && !s.startsWith("Bappea_V2.01")) //up to 1.0 version  //$NON-NLS-1$ //$NON-NLS-2$
	    	{
	    		if(s.startsWith("Bappea_V1.5") || s.startsWith("Bappea_V2.0"))  //$NON-NLS-1$
	    		{
	    			return;
	    		}
	    		String version = s.substring(8,12);
	    		String traceType = s.substring(13,s.length());
	    		System.out.println(Messages.getString("PriTraceParser.foundVersion1")+version+Messages.getString("PriTraceParser.foundVersion2")+traceType+Messages.getString("PriTraceParser.foundVersion3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    		if (!traceType.equals("MEM"))  //$NON-NLS-1$
	    		{
	    			GeneralMessages.showErrorMessage(Messages.getString("PriTraceParser.wrongTraceType1")+traceType+Messages.getString("PriTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$
	    			throw new Exception(Messages.getString("PriTraceParser.wrongTraceType1")+traceType+Messages.getString("PriTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$
	    		}
	    		else
	    		{
	    		    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    		    dis = new DataInputStream(bais);
	    			parseNewPriFile(version,dis);
	    			return;
	    		}
	    	}
	    	else //handling 1.10 and newer versions
	    	{
	    	    parseVersion110Trace(traceArray,s);
	    	    return;
	    	}
	    }
	    System.out.println(Messages.getString("PriTraceParser.oldVersion")); //$NON-NLS-1$
	    
	    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    dis = new DataInputStream(bais);
	    
	    int listLength = this.findThreadListLength(dis, traceArray.length);
	    if (listLength == -1)
	    {
	    	GeneralMessages.showErrorMessage(Messages.getString("PriTraceParser.invalidMemoryTraceFile1")); //$NON-NLS-1$
	    	System.out.println(Messages.getString("PriTraceParser.invalidMemoryTraceFile")); //$NON-NLS-1$
	    	throw new Exception(Messages.getString("PriTraceParser.invalidMemoryTraceFile")); //$NON-NLS-1$
	    }

	    priTrace = new PriTrace();
	    
	    for (int i=0;i<listLength;)
	    {
	    	int j = 0;
	    	for (j=0;j<buffer.length;j++)
	    	{
	    		byte tmpByte = dis.readByte();
	    		if (tmpByte != 0)
	    		{
	    			buffer[j] = (char)tmpByte;
	    		}
	    		else
	    		{
	    			dis.skipBytes(buffer.length-j-1);
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
	    	sampleNum = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	String unparsedName = new String(buffer);
	    	int index = 0;
	    	while ((unparsedName.charAt(index++) != ':') && (index<buffer.length)){}
	    	processName = unparsedName.substring(0,index-1);
	    	threadName = unparsedName.substring(index+1, j);
//	    	int tmp = threadName.length();
//	    	memTrace.addFirstSynchTime(new Integer(threadId), new Integer(sampleNum));
//	    	memSamples.add(new MemThread(new Integer(threadId), threadName, processName));
	    	priTrace.addFirstSynchTime(new Integer(threadId), new Integer(sampleNum));
	    	priSamples.add(new PriThread(new Integer(threadId), threadName, processName));
	    	//buffer = null; int threadId, String threadName, String processName
	    	i += buffer.length + 8;
	    }
//	    MemThread[] threads = new MemThread[memSamples.size()];
//	    memSamples.copyInto((MemThread[]) threads);
//	    memTrace.setThreads(threads);
	    PriThread[] priThreads = new PriThread[priSamples.size()];
	    priSamples.copyInto((PriThread[]) priThreads);
	    priTrace.setThreads(priThreads);
	    dis.skipBytes(8); //skipping "LIST_END" text
	    for (int k=0;k<traceArray.length-listLength-8;)
	    {
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
//	    	threadId = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	threadId = ((c & 0xff) << 8) | (d & 0xff);
	    	processPriority = b;
			threadPriority = a;
	    	
	    	
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
//	    	heapSize = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
//	    	stackSize = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	
	    	d = dis.readByte();
	    	c = dis.readByte();
	    	b = dis.readByte();
	    	a = dis.readByte();
	    	sampleNum = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
	    	for (Enumeration<PriThread> e = priSamples.elements();e.hasMoreElements();)
	    	{
	    		PriThread pt = e.nextElement();
	    		if (pt.threadId.intValue() == threadId)
	    		{
	    			int priority = solveAbsolutPriority(processPriority, threadPriority);
	    			priSample = new PriSample(pt, priority, sampleNum);
	    			priTrace.addSample(priSample);
	    			break;
	    		}
	    	}
//	    	memTrace.addLastSynchTime(new Integer(threadId), new Integer(sampleNum));
	    	priTrace.addLastSynchTime(new Integer(threadId), new Integer(sampleNum));
	    	k += 16;
	    }
	    System.out.println(Messages.getString("PriTraceParser.done")); //$NON-NLS-1$
	    
	}
	
	private void parseVersion110Trace(byte[] traceArray, String version_data) throws Exception
	{
//		String version = version_data.substring(8,12);

	    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    DataInputStream dis = new DataInputStream(bais);

    	if (version_data.indexOf("PRI") > 0) //only pritrace  //$NON-NLS-1$
    	{
    	    this.parseV110PriFile(dis);
    	}
	}
	
	
	private GenericSampledTrace getTrace()
	{
		//MemTrace trace = new MemTrace();
		return (GenericSampledTrace) priTrace;
	}
	
	private int solveAbsolutPriority(int processPriority, int threadPriority)
	{
		int tPriority = threadPriority*10 - 30;
		int pPriority = processPriority*10;
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
		for (int i=0;i<traceLength/8;i++)
		{
			for (int j=0;j<8;j++)
			{
				buf[j%8] = (char)dis.readByte();
//				int t = 0;
			}
			if (new String(buf).matches("LIST_END"))  //$NON-NLS-1$
			{
				dis.reset();
				return i*8;
			}

		}
		dis.reset();
		return -1;
	}
}
