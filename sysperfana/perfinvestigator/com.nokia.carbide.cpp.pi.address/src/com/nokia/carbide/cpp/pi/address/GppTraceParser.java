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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
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
  private HashMap<Long,String> threadAddressToName;

  // smp specific
  private int currentCpuNumber = 0;
  private int cpuCount = 0;


  public GppTraceParser() throws IOException
  {
    this.processes = new Hashtable<Integer,GppProcess>();
    this.threads   = new Hashtable<Integer,GppThread>();
    this.threadAddressToName = new HashMap<Long,String>();
  }

	/**
	 * Parses all given input trace files and returns the resulting ParsedTraceData. 
	 * @param traceInputs the trace data files to parse
	 * @return ParsedTraceData containing parsed trace
	 * @throws IOException
	 */
	public ParsedTraceData parse(File[] traceInputs) throws IOException {
		List<GppSample> samples = new ArrayList<GppSample>();
		
		for (File traceInput : traceInputs) {
			//each part of the trace file is independent of each other 
			//so clear previous state
			processes.clear();
			threads.clear();
			internalParse(traceInput, samples);			
		}

		ParsedTraceData pd = new ParsedTraceData();
		pd.traceData = this.getTrace(samples);
		pd.staticData = createTraceContainer();
		return pd;
	}

	private TraceDataContainer createTraceContainer() {
		TraceDataContainer container = new TraceDataContainer("GPP_address2threadname",new String[]{"address","threadname"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (Map.Entry<Long, String> entry : threadAddressToName.entrySet()) {
	    	container.addDataToColumn("threadname",entry.getKey()); //$NON-NLS-1$
	    	container.addDataToColumn("address",entry.getValue()); //$NON-NLS-1$			
		}
	    this.threadAddressToName = null;
		return container;
	}

	/**
	 * Parses the given input trace file and returns the resulting ParsedTraceData. 
	 * @param traceInput the trace data file to parse
	 * @return ParsedTraceData containing parsed trace
	 * @throws IOException
	 */
	@Override
	public ParsedTraceData parse(File traceInput) throws IOException {
		return parse(new File[]{traceInput});
	}
	
  private void internalParse(File traceInput, List<GppSample> gppSamples) throws IOException
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
								Integer.valueOf(addrThreadPeriod)); //$NON-NLS-1$
    
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
          
          if (samples < 3 && thread == null)
          {
	       	// the first sample (or couple of samples for SMP) may be recorded before its thread's name

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
        	gppSample.cpuNumber = currentCpuNumber;
            
        	gppSamples.add(gppSample);
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
        	gppSample.cpuNumber = currentCpuNumber;
            thread.samples++;
            gppSamples.add(gppSample);
          }
        }
      }
      catch (EOFException e) {}
    }
    if (debug) System.out.println(Messages.getString("GppTraceParser.2"));  //$NON-NLS-1$
    // all samples have been parsed
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
        	int separatorIndex = data.indexOf('#'); //$NON-NLS-1$
	        this.traceVersion = data.substring(data.indexOf('_')+1,separatorIndex); //$NON-NLS-1$
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
    		else if(id.equals("CPU"))
    		{
    			
    			this.currentCpuNumber = Integer.parseInt(st.nextToken());
    			cpuCount++;
    		}
    	}
               
        System.out.println(Messages.getString("GppTraceParser.4")+traceVersion+Messages.getString("GppTraceParser.5")+profilerVersion+Messages.getString("GppTraceParser.6")+samplerVersion+" CPU: "+currentCpuNumber);    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		if (   (traceVersion.indexOf("V1.10") != -1)	//$NON-NLS-1$
			|| (traceVersion.indexOf("V1.64") != -1)	//$NON-NLS-1$
			|| (traceVersion.indexOf("V2.00") != -1)	//$NON-NLS-1$
			|| (traceVersion.indexOf("V2.01") != -1))	//$NON-NLS-1$
            return true;
      	else
      		return false;
    }
    
    return false;
  }

  private static void encodeInt(int number, DataOutputStream dos) throws IOException
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
	
  private static void encodeUInt(int number, DataOutputStream dos) throws IOException
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
  
  private static long decodeInt(DataInputStream dis) throws IOException
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


  private static long decodeUInt(DataInputStream dis) throws IOException
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
    Integer pid = Integer.valueOf((int)decodeUInt(dis));

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
    Integer tid = Integer.valueOf((int)decodeUInt(dis));

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
    		String l = name.substring(name.lastIndexOf('[')+1,name.lastIndexOf(']')); //$NON-NLS-1$ //$NON-NLS-2$
    		Long threadAddress = Long.decode("0x"+l); //$NON-NLS-1$
//    		this.threadAddressToName.put(threadAddress,name);
    		this.threadAddressToName.put(threadAddress,p.name+"::"+name);
    		name = name.substring(0,name.lastIndexOf('[')); //$NON-NLS-1$
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
  
  private GppTrace getTrace(List<GppSample> samples)
  {
  	GppTrace trace = new GppTrace();
  	for (GppSample gppSample : samples) {
  		trace.addSample(gppSample);
	}
  	trace.setCPUCount(cpuCount == 0 ? 1 : cpuCount);
  	return trace;
  }
}

