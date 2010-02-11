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

package com.nokia.carbide.cpp.pi.address;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataContainer;


public class GppTraceParser extends Parser
{
//  private String traceVersion;
  private boolean debug = false;
  private String profilerVersion;
  private String samplerVersion;
  private Hashtable<Integer,GppProcess> processes;
  private Hashtable<Integer,GppThread> threads;
  private GppSample[] traceData;
  private GppThread[] sortedThreads;
  private TraceDataContainer container; 
  private Hashtable<Long,String> threadAddressToName;

  public GppTraceParser() throws IOException
  {
    this.processes = new Hashtable<Integer,GppProcess>();
    this.threads   = new Hashtable<Integer,GppThread>();
    this.threadAddressToName = new Hashtable<Long,String>();
  }

  public ParsedTraceData parse(File traceInput) throws IOException
  {
    if (!traceInput.exists())
    	throw new IOException(Messages.getString("GppTraceParser.0"));  //$NON-NLS-1$

    DataInputStream dis = new DataInputStream(new FileInputStream(traceInput));
	  
    if (validate(dis) == false)
    	throw new IOException(Messages.getString("GppTraceParser.1"));  //$NON-NLS-1$

    long pc = 0;
    GppThread thread = null;
    int threadIndexer = 0;
    int count = 1;
    int samples = 0;
    long time = 0;

    Vector<GppSample> intermediateTraceData = new Vector<GppSample>();
    
    // determine the base sampling period (address/thread sampling period) 
    int addrThreadPeriod = 1;
    
    if (traceVersion.indexOf("V2.01") != -1) { //$NON-NLS-1$
    	addrThreadPeriod =   (dis.readUnsignedByte() << 24)
			    		   | (dis.readUnsignedByte() << 16)
			    		   | (dis.readUnsignedByte() <<  8)
			    		   |  dis.readUnsignedByte();
    }

	// initialize the address/thread base sampling rate
	NpiInstanceRepository.getInstance().activeUidSetPersistState(
								"com.nokia.carbide.cpp.pi.address.samplingInterval", //$NON-NLS-1$
								new Integer(addrThreadPeriod)); //$NON-NLS-1$
    
    while (dis.available() > 0)
    {
      try
      {
        count = 1;
        int diff = (int)decodeInt(dis);
 
        if ((diff & 1) == 1)
        {
          diff &= ~1;
          thread = decodeThread(dis);
        }
        else if (diff == 0)
        {
          count = (int)decodeUInt(dis);
        }

        pc += diff;

        while (--count >= 0)
        {
          samples++;
          time += addrThreadPeriod;
          
          if (samples == 1 && thread == null)
          {
	       	// the first sample may be recorded before its thread's name

        	// create a new sample object for this sample
            GppSample gppSample = new GppSample();
            long pcMod = pc << 32;
            pcMod = pcMod >>> 32;
           	gppSample.programCounter = pcMod;
            gppSample.sampleSynchTime = time;
              
            GppThread unknownThread = new GppThread();
            unknownThread.threadId = -1;
            unknownThread.threadName = Messages.getString("GppTraceParser.unknown"); //$NON-NLS-1$
              
            GppProcess unknownProcess = new GppProcess();
            unknownProcess.id = -1;
            unknownProcess.name = Messages.getString("GppTraceParser.unknown"); //$NON-NLS-1$
            this.processes.put(-1,unknownProcess);
 
            unknownThread.process = unknownProcess;
            unknownThread.index = threadIndexer++;
              
            gppSample.thread = unknownThread;

            this.threads.put(-1,unknownThread);

            gppSample.thread = unknownThread;
            intermediateTraceData.add(gppSample);
          }
          else if (thread.index >= -1)
	      {
            if (thread.index == -1) thread.index = threadIndexer++;

            // create a new sample object for this sample

            GppSample gppSample = new GppSample();
           	long pcMod = pc << 32;
           	pcMod = pcMod >>> 32;
           	
           	if (thread.threadName.equals("*Native*")) //$NON-NLS-1$
           		gppSample.programCounter = 0;
           	else
           		gppSample.programCounter = pcMod;
            
            //System.out.println("PC value:"+pc+" "+Long.toHexString(pc));
            
            gppSample.sampleSynchTime = time;
            gppSample.thread = thread;
            thread.samples++;
            intermediateTraceData.add(gppSample);
          }
        }
      }
      catch (EOFException e) {}
    }
    if (debug) System.out.println(Messages.getString("GppTraceParser.2"));  //$NON-NLS-1$
    // all samples have been parsed
    this.traceData = new GppSample[intermediateTraceData.size()];

    // store the trace data into an array
    intermediateTraceData.toArray(this.traceData);

    // sort the threads into an ordered array
    this.sortThreads();
    
    container = new TraceDataContainer("GPP_address2threadname",new String[]{"address","threadname"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Enumeration<Long> ke = this.threadAddressToName.keys();
    Iterator<String>  vi = this.threadAddressToName.values().iterator();
    
    while(ke.hasMoreElements())
    {
    	Long address = ke.nextElement();
    	String name = vi.next();
    	container.addDataToColumn("threadname",name); //$NON-NLS-1$
    	container.addDataToColumn("address",address); //$NON-NLS-1$
    }
    this.threadAddressToName.clear();
    this.threadAddressToName = null;
    
    ParsedTraceData pd = new ParsedTraceData();
    pd.traceData = this.getTrace();
        
    return pd;
  }
  
  private void sortThreads()
  {
    if (this.threads != null)
    {
      boolean sorted = false;

      GppThread[] tArray = new GppThread[this.threads.size()];
      Collection<GppThread> threadCollection = this.threads.values();
      threadCollection.toArray(tArray);

      // set initial sort order to the order in which
      // the threads appear in the array
      for (int i = 0; i < tArray.length; i++)
      {
        tArray[i].sortOrder = i;
      }

      // sort threads using bubble sort
      while (sorted == false)
      {
        sorted = true;
        for (int i = 0; i < tArray.length - 1; i++)
        {
          if (tArray[i].samples < tArray[i + 1].samples)
          {
              // switch the sort order
              GppThread store = tArray[i];
              tArray[i] = tArray[i + 1];
              tArray[i + 1] = store;
              sorted = false;
          }
        }
      }

      // finally, store the ordered array
      this.sortedThreads = tArray;
      for (int i=0;i<this.sortedThreads.length;i++)
      {
        this.sortedThreads[i].sortOrder = i;
      }

    }
  }

  private boolean validate(DataInputStream dis) throws IOException
  {
    String data = decodeName(dis);
    if (debug) System.out.println(Messages.getString("GppTraceParser.3")+data);  //$NON-NLS-1$
    if (data.equals("profile")) //pre 1.10 //$NON-NLS-1$
    if (decodeUInt(dis) == 1) return true;
        
    if (data.startsWith("Bappea_GPP_V")) //version 1.20 //$NON-NLS-1$
    {
        try
        {
        	int separatorIndex = data.indexOf("#"); //$NON-NLS-1$
	        this.traceVersion = data.substring(data.indexOf("_")+1,separatorIndex); //$NON-NLS-1$
	    }
	    catch (Exception e)
	    {
	    	return false;
	    }
    
    	StringTokenizer st = new StringTokenizer(data,"#"); //$NON-NLS-1$
      	while(st.hasMoreElements())
    	{
    		String id = st.nextToken();
    		if (id.equals("Prof")) //$NON-NLS-1$
    		{
    			this.profilerVersion = st.nextToken();
    		}
    		else if (id.equals("Samp"))  //$NON-NLS-1$
    		{
    			this.samplerVersion = st.nextToken();
    		}
    	}
               
        System.out.println(Messages.getString("GppTraceParser.4")+traceVersion+Messages.getString("GppTraceParser.5")+profilerVersion+Messages.getString("GppTraceParser.6")+samplerVersion);    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		if (   (traceVersion.indexOf("V1.10") != -1)	//$NON-NLS-1$
			|| (traceVersion.indexOf("V1.64") != -1)	//$NON-NLS-1$
			|| (traceVersion.indexOf("V2.01") != -1))	//$NON-NLS-1$
            return true;
      	else
      		return false;
    }
    
    return false;
  }

  	public static void encodeInt(int number, DataOutputStream dos) throws IOException
	{
		int digit;
		for (;;) {
			digit = number & 0x7f;
			if ((number >> 6) == (number >> 7))
				break;
			number >>= 7;
			dos.write(digit);
		}
		dos.write(digit | 0x80);
		
		dos.flush();
	}
	
	public static void encodeUInt(int number, DataOutputStream dos) throws IOException
	{
		int digit;
		for (;;) {
			digit = number & 0x7f;
			number >>= 7;
			if (number <= 0)
				break;
			dos.write(digit);
		}
		dos.write(digit | 0x80);		
	}
  
  public static long decodeInt(DataInputStream dis) throws IOException
  {
    //System.out.println("DECODING INT");

	int val = 0;
    int shift = 0;
    int data;
    do
    {
      data = dis.readUnsignedByte();
      //System.out.print(":"+Integer.toHexString(data));
      if (data < 0)
    	  throw new IOException(Messages.getString("GppTraceParser.7"));  //$NON-NLS-1$
      val |= (data & 0x7f) << shift;
      shift += 7;
    } while ((data & 0x80) == 0);

    if (shift < 32)
    {
      shift = 32 - shift;
      val = val << shift >> shift;
    }
    //System.out.println("read int"+(val & 0xffffffff));

    return (val);
  }


  public static long decodeUInt(DataInputStream dis) throws IOException
  {
    //System.out.println("DECODING UINT");
    long val = 0;
    int shift = 0;
    int data;
    do
    {
      data = dis.readUnsignedByte();
      //System.out.println("read byte:"+Integer.toHexString(data));
      if (data < 0)
    	  throw new IOException(Messages.getString("GppTraceParser.8"));  //$NON-NLS-1$
      val |= (data & 0x7f) << shift;
      shift += 7;
    } while ((data & 0x80) == 0);

    //System.out.println("read unsigned int"+val);
    return val;
  }


  private String decodeName(DataInputStream dis) throws IOException
  {
    //System.out.println("DECODING NAME");

    int length = dis.readUnsignedByte();
    //System.out.println("name length "+Integer.toHexString(length));
    byte[] data = new byte[length];
    dis.read(data);
    //System.out.println("Read name:"+new String(data));
    return new String(data);
  }

  private GppProcess decodeProcess(DataInputStream dis) throws IOException
  {
    //System.out.println("DECODING PROCESS");
    Integer pid = new Integer((int)decodeUInt(dis));

    if (this.processes.containsKey(pid))
    {
      return (GppProcess)this.processes.get(pid);
    }
    else
    {
      GppProcess np = new GppProcess();
      np.id = pid;
      np.name = decodeName(dis);
      this.processes.put(pid,np);
      return np;
    }
  }

  private GppThread decodeThread(DataInputStream dis) throws IOException
  {
    //System.out.println("DECODING THREAD");
    Integer tid = new Integer((int)decodeUInt(dis));

    if (this.threads.containsKey(tid))
    {
      return (GppThread)this.threads.get(tid);
    }

    GppProcess p = decodeProcess(dis);
    String name = decodeName(dis);
    
    try
	{
    	if (name.endsWith("]")) //$NON-NLS-1$
    	{
    		String l = name.substring(name.lastIndexOf("[")+1,name.lastIndexOf("]")); //$NON-NLS-1$ //$NON-NLS-2$
    		Long threadAddress = Long.decode("0x"+l); //$NON-NLS-1$
    		this.threadAddressToName.put(threadAddress,name);
    		name = name.substring(0,name.lastIndexOf("[")); //$NON-NLS-1$
    	}
	}
    catch (Exception e)
	{
    	e.printStackTrace();
	}

    GppThread nt = new GppThread();
    nt.threadId = tid;
    nt.threadName = name;
    nt.process = p;
    nt.index = -1;

    this.threads.put(tid,nt);

    return nt;
  }
  
  private GenericTrace getTrace()
  {
  	GppTrace trace = new GppTrace();
  	for (int i = 0; i < traceData.length; i++)
  	{
  		trace.addSample(this.traceData[i]);
  	}
  	return trace;
  }
}

