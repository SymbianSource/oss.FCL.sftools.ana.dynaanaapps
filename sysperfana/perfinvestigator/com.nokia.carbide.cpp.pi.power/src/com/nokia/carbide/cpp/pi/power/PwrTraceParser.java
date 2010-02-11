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

package com.nokia.carbide.cpp.pi.power;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;


public class PwrTraceParser extends Parser
{
	private boolean debug = false;
	private PwrSample[] traceData;	

	public PwrTraceParser() throws Exception
	{
	}
	
	public PwrTraceParser(File file) throws Exception
	{
		if (!file.exists() || file.isDirectory())
		{
			throw new Exception(Messages.getString("PwrTraceParser.cannotOpenTraceFile")); //$NON-NLS-1$
		}
		
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int)file.length()];
		fis.read(data);
		this.traceVersion = this.getVersion(data);
		System.out.println(Messages.getString("PwrTraceParser.powerVersion") + this.traceVersion); //$NON-NLS-1$
		if (traceVersion.indexOf("V1.57") != -1) //$NON-NLS-1$
		    this.parse(file);
		else
		    System.out.println(Messages.getString("PwrTraceParser.unsupportedVersion") + this.traceVersion); //$NON-NLS-1$
		fis.close();
	}
		
	public ParsedTraceData parse(File file) throws Exception 
	{
		if (!file.exists() || file.isDirectory())
		{
			throw new Exception(Messages.getString("PwrTraceParser.cannotOpenTraceFile")); //$NON-NLS-1$
		}
		
		Vector intermediateTraceData = new Vector();

		try 
		{
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];
			fis.read(data);
			
			this.traceVersion = this.getVersion(data);
			System.out.println(Messages.getString("PwrTraceParser.powerVersion") + this.traceVersion); //$NON-NLS-1$
			if (traceVersion.indexOf("V1.57") == -1) //$NON-NLS-1$
			{
			    System.out.println(Messages.getString("PwrTraceParser.unsupportedVersion") + this.traceVersion); //$NON-NLS-1$
			    fis.close();
			    return null;
			}
			
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			
			// read the length of the header
			int length = dis.readByte();
			// skip the header
			for (int i = 0; i < length; i++)
				dis.readByte();
			
			long voltage = 0;
			long current = 0;
			long capacity = 0;
			long sampleTime = 0;

			try
			{
				while(true)
				{
					capacity = readTUint16(dis);
					voltage = readTUint16(dis);
					current = readTUint(dis);
					sampleTime = readTUint(dis);

					PwrSample sample = new PwrSample(sampleTime, current, voltage, capacity);

					intermediateTraceData.add(sample);
				}
			}
			catch (EOFException eof)
			{
				//return;
			}
			catch (Exception e)
			{
				GeneralMessages.showErrorMessage(Messages.getString("PwrTraceParser.errorReadingTraceFile")); //$NON-NLS-1$
				throw e;				
			}
		}
		
		catch (Exception e)
		{
			GeneralMessages.showErrorMessage(Messages.getString("PwrTraceParser.errorReadingTraceFile")); //$NON-NLS-1$
			throw e;				
		}
	
		if (debug) System.out.println(Messages.getString("PwrTraceParser.traceFileParsed") + intermediateTraceData.size()); //$NON-NLS-1$
		
		// all samples have been parsed
		intermediateTraceData.trimToSize();
		this.traceData = new PwrSample[intermediateTraceData.size()];
		
		// store the trace data into an array
		intermediateTraceData.toArray(this.traceData);
		
		ParsedTraceData ptd = new ParsedTraceData();
		ptd.traceData = this.getTrace();
		return ptd;
	}
	
	private String getVersion(byte[] data)
	{
		int length = data[0];

		String ver = Messages.getString("PwrTraceParser.unknown"); //$NON-NLS-1$
		String verString = new String(data, 1, length);
		if (debug)
			System.out.println(Messages.getString("PwrTraceParser.versionStringDebug") + verString); //$NON-NLS-1$
		
		if(verString.indexOf("Bappea") != -1) //$NON-NLS-1$
			if(verString.indexOf("PWR") != -1) //$NON-NLS-1$
			{
				int index = verString.indexOf("_"); //$NON-NLS-1$
				ver = verString.substring(index + 1, length);
			}
		return ver;
	}		
	
	private long readTUint(DataInputStream dis) throws Exception
	{
		long result = dis.readUnsignedByte();
		result += dis.readUnsignedByte() << 8;
		result += dis.readUnsignedByte() << 16;
		result += dis.readUnsignedByte() << 24;
		return result;
	}

	private long readTUint16(DataInputStream dis) throws Exception
	{
		int result = dis.readUnsignedByte();
		result += dis.readUnsignedByte() << 8;
		return result;
	}

	private GenericTrace getTrace()
	{
		PwrTrace trace = new PwrTrace();
		for (int i = 0; i < traceData.length; i++)
		{
			trace.addSample(this.traceData[i]);
		}
		return trace;
	}
	
    public static void main( String[] args ) 
    {
	    try {
	        PwrTraceParser p = new PwrTraceParser(new File(Messages.getString("PwrTraceParser.tmpFilePath"))); //$NON-NLS-1$
	    } catch ( Exception  e ) {
	        System.out.println(Messages.getString("PwrTraceParser.exceptionMessage") + e.getMessage() ); //$NON-NLS-1$
	    }
    }
}
