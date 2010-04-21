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

package com.nokia.carbide.cpp.pi.priority2;

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


public class NewPriTraceParser extends Parser
{
    private boolean debug = false;

    private String version;
    
	private Vector<NewPriThread> priSamples = new Vector<NewPriThread>();
	private String threadName;
	private String processName;
	private int threadId;
	private int sampleNum;
	private int threadPriority;
	private NewPriSample priSample;
	private NewPriTrace priTrace;
	private int readCount = 0;
	
	public NewPriTraceParser(File file) throws Exception
	{
	}
	
	public ParsedTraceData parse(File f) throws Exception 
	{
		if (!f.exists() || f.isDirectory())
	    {
	      throw new Exception(Messages.getString("NewPriTraceParser.cannotOpenMemoryTrace")); //$NON-NLS-1$
	    }
		if (debug) System.out.println(Messages.getString("NewPriTraceParser.memoryTraceFileLength")+f.length()); //$NON-NLS-1$

	    parsePriTrace(f);
		
		ParsedTraceData ptd = new ParsedTraceData();
		ptd.traceData = this.getTrace();
		return ptd;
	}
	
	private void parseV155PriFile(DataInputStream dis) throws Exception
	{
		// read the version again
		byte[] version = readElementWithLength(dis);
		System.out.println(Messages.getString("NewPriTraceParser.readVersion")+new String(version)); //$NON-NLS-1$
		this.version = new String(version);
		
		this.readV155PriSample(dis);
	}
	
	public String getProfilerVersion()
	{
	    return version;
	}

	private void readV155PriSample(DataInputStream dis) throws Exception
	{
	    Vector<long[]> rawV155PriSamples = new Vector<long[]>();
	    Vector<long[]> lastSampleRaw     = new Vector<long[]>();

		//memTrace = new MemTrace();
	    priTrace = new NewPriTrace();
	   		
		// first there should be 4 bytes of sample time
		long sample = this.readTUintWithLength(dis);
				
		// then read if there is thread name data
		int length = dis.readUnsignedByte();readCount++;
		if(length != 1) throw new Exception(Messages.getString("NewPriTraceParser.missingType")); //$NON-NLS-1$
		
		int mode = dis.readUnsignedByte();readCount++;
		System.out.println(Messages.getString("NewPriTraceParser.firstMode")+Integer.toHexString(mode)); //$NON-NLS-1$
		try
		{
			// read the first length
			length = dis.readUnsignedByte();readCount++;

			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			
			while(true)
			{
				//System.out.println("ReadCount:"+this.readCount);
				if(length == 0)
				{
					// end of sample
					// read new length
					
					// first there should be 4 bytes of sample time
					sample = this.readTUintWithLength(dis);
					//System.out.println("Sample number:"+sample);
					
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("New length "+length);				
					//break;
					
					if(length == 4)
					{
						// there was only the sample header here
						System.out.println(Messages.getString("NewPriTraceParser.missingSampleNum")+sample); //$NON-NLS-1$
						mode = 0x00;
					}
					else if(length != 1)
					{
						throw new Exception(Messages.getString("NewPriTraceParser.wrongLength")+length); //$NON-NLS-1$
					}
					else
					{
						lastSampleRaw.clear();
					}
				}
				
				if(length == 1)
				{
					mode = dis.readUnsignedByte();readCount++;
					//System.out.println("New Mode "+mode+" new length "+length);

					// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
				}
				
				if(mode == 0xbb)
				{
					// reading thread names
					String rawName = readThreadName(dis,length);
					long threadId = readTUint(dis);
					//System.out.println("Raw name "+rawName);
				   	int index = rawName.indexOf(':');
				   	if(index != -1)
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
								    	
				   	priTrace.addFirstSynchTime(Integer.valueOf((int)threadId), Integer.valueOf(sampleNum));
				   	priSamples.add(new NewPriThread(Integer.valueOf((int)threadId), threadName, processName));

				   	// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
				}
				else if(mode == 0xee)
				{
					// reading data
					long[] elements = this.readDataElements155Pri(dis,length,sample);
					
					rawV155PriSamples.add(elements);
					lastSampleRaw.add(elements);
					
					// read the next length
					length = dis.readUnsignedByte();readCount++;
					//System.out.println("Next length "+length);
				}
				else if(mode == 0x00)
				{
					// read the length, it is already known
					sample = readTUint(dis);
					System.out.println(Messages.getString("NewPriTraceParser.readNextSampleNum")+sample); //$NON-NLS-1$
					length = dis.readUnsignedByte();readCount++;;
				}
				else
				{
					throw new Exception(Messages.getString("NewPriTraceParser.wrongMode")+mode); //$NON-NLS-1$
				}
			}
		}
		catch (EOFException e)
		{
			System.out.println(Messages.getString("NewPriTraceParser.finishedReading")); //$NON-NLS-1$
			this.addRawV155PriSamples(rawV155PriSamples);
		}
	}

	private void addRawV155PriSamples(Vector<long[]> rawV110PriSamples)
	{
		NewPriThread[] priThreads = new NewPriThread[priSamples.size()];
	    priSamples.copyInto((NewPriThread[]) priThreads);
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

	    	sampleNum = (int)element[2]; 

	    	for (Enumeration<NewPriThread> e = priSamples.elements();e.hasMoreElements();)
	    	{
	    		NewPriThread pt = e.nextElement();
	    		if (pt.threadId == threadId)
	    		{
	    			//int priority = solveAbsolutPriority(processPriority, threadPriority);
	    			priSample = new NewPriSample(pt, threadPriority, sampleNum);
	    			priTrace.addSample(priSample);
	    			break;
	    		}
	    	}
	    	priTrace.addLastSynchTime(Integer.valueOf(threadId), Integer.valueOf(sampleNum));
	    }
	    System.out.println(Messages.getString("NewPriTraceParser.doneParsing")); //$NON-NLS-1$
	}

	private long[] readDataElements155Pri(DataInputStream dis,int length,long sampleNum) throws Exception
	{
		if(length != 0)
		{
			long[] elements = new long[3];
			elements[0] = this.readTUint(dis);  //thread ID
			elements[1] = this.readShort(dis);  //Priority
			elements[2] = sampleNum;  //sample number

			return elements;
		}
		else 
		{
			System.out.println(Messages.getString("NewPriTraceParser.lengthIs0")); //$NON-NLS-1$
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
			throw new Exception(Messages.getString("NewPriTraceParser.TUINTLengthException1")+length+Messages.getString("NewPriTraceParser.TUINTLengthException2")); //$NON-NLS-1$ //$NON-NLS-2$
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

	    	if (!s.startsWith("Bappea_V1.56")) //up to 1.0 version //$NON-NLS-1$
	    	{
	    		String version = s.substring(8,12);
	    		String traceType = s.substring(13,s.length());
	    		System.out.println(Messages.getString("NewPriTraceParser.foundVersion1")+version+Messages.getString("NewPriTraceParser.foundVersion2")+traceType+Messages.getString("NewPriTraceParser.foundVersion3")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    			GeneralMessages.showErrorMessage(Messages.getString("NewPriTraceParser.wrongTraceType1")+traceType+Messages.getString("NewPriTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$
    			throw new Exception(Messages.getString("NewPriTraceParser.wrongTraceType1")+traceType+Messages.getString("NewPriTraceParser.wrongTraceType2")); //$NON-NLS-1$ //$NON-NLS-2$
	    	}
	    	else //handling 1.10 and newer versions
	    	{
	    	    parseVersion155Trace(traceArray,s);
	    	    return;
	    	}
	    }
	    else 
	    {
			GeneralMessages.showErrorMessage(Messages.getString("NewPriTraceParser.wrongVersionforPRI")); //$NON-NLS-1$
			throw new Exception(Messages.getString("NewPriTraceParser.wrongTypeforPRI")); //$NON-NLS-1$
	    }	    
	}
	
	private void parseVersion155Trace(byte[] traceArray, String version_data) throws Exception
	{
	    ByteArrayInputStream bais = new ByteArrayInputStream(traceArray);
	    DataInputStream dis = new DataInputStream(bais);

	    if (version_data.indexOf("PRI")>0) //only pritrace //$NON-NLS-1$
	    {
    	    	this.parseV155PriFile(dis);
	    }
	}

	private GenericSampledTrace getTrace()
	{
		//MemTrace trace = new MemTrace();
		return (GenericSampledTrace) priTrace;
	}
}
