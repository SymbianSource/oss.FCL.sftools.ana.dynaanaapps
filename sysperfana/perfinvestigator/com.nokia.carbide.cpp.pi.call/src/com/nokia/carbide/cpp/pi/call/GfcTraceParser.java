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

package com.nokia.carbide.cpp.pi.call;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.internal.pi.model.TraceDataRepository;
import com.nokia.carbide.cpp.pi.address.GppSample;
import com.nokia.carbide.cpp.pi.address.GppTrace;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class GfcTraceParser extends Parser
{
//  private String profilerVersion;
	private ArrayList<Long[]> completeGfcTrace;
//  private int firstSample,lastSample;
  
	public GfcTraceParser() throws Exception
	{
		this.completeGfcTrace = new ArrayList<Long[]>();
	}
  
	public ParsedTraceData parse(File f) throws Exception 
	{
		if (!f.exists()) //throw new IOException("GFC file does not exist");
		{
			setStateOk(false);
		}
		else
		{
			FileInputStream fis = new FileInputStream(f);
			byte[] data = new byte[(int)f.length()];
			fis.read(data);
			String version = getVersion(data);
			System.out.println(Messages.getString("GfcTraceParser.traceVersion")+version); //$NON-NLS-1$
			//profilerVersion = version;
//			traceVersion = "GFC_V" + version;
			traceVersion = "V" + version; //$NON-NLS-1$
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			
			if (version.equals("0.91")||version.equals("1.00")||version.equals("1.10")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{
				// skip the intro
				int intro = dis.readUnsignedByte();
				for (int i=0;i<intro;i++) dis.readUnsignedByte();
				
				// start parsing
				try
				{
					readEntriesV091(dis);
				}
				catch (Exception e)
				{
					GeneralMessages.showErrorMessage(Messages.getString("GfcTraceParser.invalidTraceFile")); //$NON-NLS-1$
					throw e;				
				}
			}
			else if (version.equals("0.40-0.85")) //$NON-NLS-1$
			{
				try
				{
					readEntries(dis);
				}
				catch (Exception e)
				{
					GeneralMessages.showErrorMessage(Messages.getString("GfcTraceParser.invalidtraceFile")); //$NON-NLS-1$
					throw e;
				}
			}
			else
			{
				GeneralMessages.showErrorMessage(Messages.getString("GfcTraceParser.traceVersionNotSupported1")+version+Messages.getString("GfcTraceParser.traceVersionNotSupported2")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new Exception(Messages.getString("GfcTraceParser.traceVersionNotSupportedException")); //$NON-NLS-1$
			}
	    }
	  	ParsedTraceData ptd = new ParsedTraceData();
	  	ptd.traceData = this.getTrace();
	  	return ptd;
	}
  
	//this returns profiler version for pre 1.0 traces
	public String getProfilerVersion()
	{
    	return traceVersion;
	}
  
	private String getVersion(byte[] data)
	{
	  	int length = data[0];
	  	if (length > 8)
	  	{
	  		String verString = new String(data,1,length);
	  		if (verString.indexOf("Bappea_V") != -1) //$NON-NLS-1$
	  			if (verString.indexOf("GFC") != -1) //$NON-NLS-1$
	  			{
	  				int index = verString.indexOf("Bappea_V")+8; //$NON-NLS-1$
	  				String ver = verString.substring(index,index+4);
	  				return ver;
	  			}
	  	}
	  	return "0.40-0.85"; //$NON-NLS-1$
	}
  
	private void readEntriesV091(DataInputStream dis) throws Exception
	{
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$

		// if address/thread trace's 1st sample was sent before its thread name,
		// then the first GPP sample was thrown out. In that case, the 1st GfcTrace sample needs to be thrown out also
		ParsedTraceData traceData = TraceDataRepository.getInstance().getTrace(NpiInstanceRepository.getInstance().activeUid(), GppTrace.class);
		
		GppSample gppSample = null;
		if (traceData != null && traceData.traceData != null && (traceData.traceData instanceof GppTrace)) {
			Vector gppSamples = ((GppTrace)traceData.traceData).samples; 
			gppSample = ((GppSample)gppSamples.get(0)); //$NON-NLS-1$
		}

		try
		{
			long[] data = new long[3];
			long pc = 0;
			long lr = 0;
			long sampleTime = 0;
	
			for (int i=0;i<3;i++) data[i] = 0;
			
	  		while (true)
	  		{
	  			boolean pcNeg = true;
	  			boolean lrNeg = true;
	  		  	int[] entry = readEntry(dis);
	  		  	
	  		  	// information on whether this increment is positive
	  		  	// or negative, has been encoded to the first bit
	  		  	// of entry indices 0 and 1
	  		  	
	  		  	// they should not be considered in case entry 0
	  		  	// is 0xff, because in that case the repeat length 
	  		  	// is encoded to those values
	  		  	if (entry[0] != 0xff)
	  		  	{
	  		  		if (entry[0] > 4)
	  		  		{
	  		  			pcNeg = false;
	  		  			entry[0] = entry[0] & 0x0f;
	  		  		}
	  		  		if (entry[1] > 4)
	  		  		{
	  		  			lrNeg = false;
	  		  			entry[1] = entry[1] & 0x0f;  		  		
	  		  		}
	  		  	}
	  		  	//System.out.println("Entry "+entry[0]+" "+entry[1]+" "+entry[2]);
	  		  	
	  			long[] newData = getNewData(data,entry,dis);
	  			{
	  				if (newData[0] == 0 && newData[1] == 0)
	  				{
	  					// repeat
	  					for (int i=0;i<newData[2];i++)
	  					{
	  						data[2]+=samplingInterval;
	  						
	  	  					pc = (data[0] << 32) >>> 32;
	  	  					lr = (data[1] << 32) >>> 32;
	  	  					sampleTime = (data[2] << 32) >>> 32;
	  						
	  	  					//System.out.println("REP: pc:"+Long.toHexString(pc)+" lr:"+Long.toHexString(lr)+" sa:"+sa);
	  	  					// sometimes the first address/thread sample had to be discarded,
	  	  					// so 2nd call sample matches first address/thread sample
	  	  					if ( gppSample != null &&  sampleTime == gppSample.sampleSynchTime && gppSample.sampleSynchTime == samplingInterval * 2
	  	  						&& this.completeGfcTrace.size() == 1) {
	  	  						// replace the first sample with this one
	  	  						this.completeGfcTrace.remove(0);
	  	  					}
	  	  					this.completeGfcTrace.add(new Long[]{Long.valueOf(sampleTime),Long.valueOf(pc), Long.valueOf(lr)});
	  					}
	  				}
	  				else
	  				{
	  					if (pcNeg) 
	  					{
	  						//System.out.println("Subtracting PC "+Long.toHexString(newData[0])+" from "+Long.toHexString(data[0]));
	  						data[0]-=newData[0];
	  					}
	  					else 
	  					{
	  						//System.out.println("Adding PC "+Long.toHexString(newData[0])+" to "+Long.toHexString(data[0]));
	  						data[0] += newData[0];
	  					}
	
	  					if (lrNeg) 
	  					{
	  						//System.out.println("Subtracting LR "+Long.toHexString(newData[1])+" from "+Long.toHexString(data[1]));
	  						data[1] -= newData[1];
	  					}
	  					else 
	  					{
	  						//System.out.println("Adding LR "+Long.toHexString(newData[1])+" to "+Long.toHexString(data[1]));
	  						data[1] += newData[1];
	  					}
	  					
	  					data[2] += newData[2];
	  					
	  					pc = (data[0] << 32) >>> 32;
	  					lr = (data[1] << 32) >>> 32;
	  					sampleTime = (data[2] << 32) >>> 32;
	  					//System.out.println("pc:"+Long.toHexString(pc)+" lr:"+Long.toHexString(lr)+" sa:"+sa);
  	  					// sometimes the first address/thread sample had to be discarded,
  	  					// so 2nd call sample matches first address/thread sample
  	  					if ( gppSample != null &&   sampleTime == gppSample.sampleSynchTime && gppSample.sampleSynchTime == samplingInterval * 2
  	  						&& this.completeGfcTrace.size() == 1) {
  	  						// replace the first sample with this one
  	  						this.completeGfcTrace.remove(0);
  	  					}
	  					this.completeGfcTrace.add(new Long[]{Long.valueOf(sampleTime),Long.valueOf(pc), Long.valueOf(lr)});
	  					
	  					data[0] = pc;
	  					data[1] = lr;
	  					data[2] = sampleTime;
	  				}
	  			}
	  		}
		}
	  	catch (EOFException e)
		{
	  		return;
		}
	  	catch (Exception e)
		{
	  		throw e;
		}
	}
	
	private long[] getNewData(long[] prevData,int[] entry,DataInputStream dis) throws Exception
	{
		long[] data = new long[3];
	
	  	if (entry[0] == 0xff)
	  	{
	  		// repeat
	  		long repeat = entry[1]<<8;
	  		repeat += entry[2];
	  		//System.out.println("Repeat "+repeat);
	  		data[0] = 0;
	  		data[1] = 0;
	  		data[2] = repeat;
	  		return data;
	  	}
	  	else
	  	{
	  		// read pc and lr values
	  		for (int k=0;k<2;k++)
	  		{
	  			for (int i=0;i<entry[k];i++)
	  			{
	  				long value = (((long)dis.readUnsignedByte()) << (8*i));
	  				data[k] |= value;
	  			}
	  			//System.out.println("Data ["+k+"] = "+Long.toHexString(data[k]));
	  		}
	  		
	  		// read sample number, if present
	  		if (entry[2] == 0xff)
	  		{
	  			for (int i=0;i<4;i++)
	  			{
	  				long value = (((long)dis.readUnsignedByte()) << (8*i));
	  				data[2] |= value;
	  			}
	  			//System.out.println("SampleNr "+data[2]);
	  		}
	  		else
	  		{
	  			data[2] = entry[2];
	  		}
	  		return data;
	  	}
	}
  
	private int[] readEntry(DataInputStream dis) throws Exception
	{
	  	int[] e = new int[3];
	  	e[0] = dis.readUnsignedByte();
	  	e[1] = dis.readUnsignedByte();
	  	e[2] = dis.readUnsignedByte();
	 	return e;
	}

	private void readEntries(DataInputStream dis) throws Exception
	{
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
	    int byteCount = 0;
	    long previousSample = 0;
	    
	    while (dis.available() >= 4)
	    {
		    long programCounter = readUnsignedIntFromStream(dis);
		    programCounter = ((programCounter << 32) >>> 32);
		   	long linkRegister = readUnsignedIntFromStream(dis);
		   	linkRegister = ((linkRegister << 32) >>> 32);
		    long sample = readUnsignedIntFromStream(dis);
		      
		    if (previousSample != 0)
		    {
		      	if (sample != previousSample+samplingInterval)
		      		System.out.println(Messages.getString("GfcTraceParser.missingSample1")+previousSample+Messages.getString("GfcTraceParser.missingSample2")+sample); //$NON-NLS-1$ //$NON-NLS-2$
		    }
		    previousSample = sample;
	
		    this.completeGfcTrace.add(new Long[]{Long.valueOf(sample),Long.valueOf(programCounter), Long.valueOf(linkRegister)});
		    byteCount += 12;
	    }
//		setProgressBarString("Done");
	    System.out.println(Messages.getString("GfcTraceParser.bytesRead1")+byteCount+Messages.getString("GfcTraceParser.bytesRead2")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private long readUnsignedIntFromStream(DataInputStream dis) throws IOException
	{
	    int b1 = dis.readUnsignedByte();
	    int b2 = dis.readUnsignedByte();
	    int b3 = dis.readUnsignedByte();
	    int b4 = dis.readUnsignedByte();
	
	    return (b4 * 16777216 + b3 * 65536 + b2 * 256 + b1);
	}
  
	private GenericTrace getTrace()
	{
	  	GfcTrace trace = new GfcTrace(this.completeGfcTrace.size());
	  	
		int samplingInterval = (Integer) NpiInstanceRepository.getInstance().activeUidGetPersistState("com.nokia.carbide.cpp.pi.address.samplingInterval"); //$NON-NLS-1$
		trace.setSamplingInterval(samplingInterval);

		int i = 0;
	  	for (Long[] element : this.completeGfcTrace)
	  	{
	  		GfcSample sample = new GfcSample(	element[1].longValue(), // program counter
	  											element[2].longValue(), // link register
	  											i++,
												element[0].longValue());// sample time  		
	  		trace.addSample(sample, element);
	  	}
	  	return trace;
	}

  /*
	public static void main(String a[]) throws Exception
	{
		new GfcTraceParser(new File("C:\\Bappea_1_GFC_Trace.dat"),null,null);
	}
   */
}
