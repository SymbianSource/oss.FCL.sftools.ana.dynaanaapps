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

/*
 * StreamFileParser.java
 */
package com.nokia.carbide.cpp.internal.pi.analyser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;


public class StreamFileParser 
{
	private int readOffset = 0;
	private int currentLength = 0;
	private int currentType = -1;
	private byte[] streamData;
	private Hashtable<Integer,ByteArrayOutputStream> dataBlocks;
	
	public StreamFileParser(File streamFile) throws IOException
	{
		dataBlocks = new Hashtable<Integer,ByteArrayOutputStream>();
		
		if (!streamFile.exists()) throw new IOException(Messages.getString("StreamFileParser.fileNotFound")); //$NON-NLS-1$
		
		streamData = new byte[(int)streamFile.length()];
		FileInputStream fis = new FileInputStream(streamFile);
		fis.read(streamData);
		
		readLoop();
	}
	
	public byte[] getDataForTraceType(int traceType)
	{
		Integer type = Integer.valueOf(traceType);
		if (dataBlocks.containsKey(type))
		{
			ByteArrayOutputStream baos = 
				(ByteArrayOutputStream)dataBlocks.get(type);
			return baos.toByteArray();
		}
		else 
		{
			return null;
		}
	}
	
	public File getTempFileForTraceType(int traceType) throws IOException
	{
		byte[] data = getDataForTraceType(traceType);
		if (data == null) return null;
		File f = File.createTempFile("type_" + traceType + "_trace_file",".dat");    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		//File f = File.createTempFile(getTypeString(Integer.valueOf(traceType)),".dat");
		f.deleteOnExit();
		
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data);
		fos.flush();
		fos.close();
		
		return f;
	}
		
	private void readLoop() throws IOException
	{
		while(setLengthAndMode() == true)
		{
			copyDataBlock();
		}
	}

	/*public void writeToFiles(String path) throws Exception
	{
		Enumeration enumer = dataBlocks.keys();
		while(enumer.hasMoreElements())
		{
			Integer type = (Integer)enumer.nextElement();
			ByteArrayOutputStream baos = (ByteArrayOutputStream)dataBlocks.get(type);
			String typeString = getTypeString(type);
		
			File f = new File(path+typeString+"_trace_file.dat");
			if (f.exists()) f.delete();
			FileOutputStream fos = new FileOutputStream(f);
			System.out.println("Writing "+f.getAbsolutePath());
			fos.write(baos.toByteArray());						
		}
	}*/
	/*private String getTypeString(Integer typeValue)
	{
		if (typeValue.intValue() == 1) return "GPP";
		if (typeValue.intValue() == 2) return "GFC";
		if (typeValue.intValue() == 3) return "ITT";
		if (typeValue.intValue() == 4) return "MEM";
		if (typeValue.intValue() == 5) return "TIP";
		if (typeValue.intValue() == 6) return "IRQ";
		if (typeValue.intValue() == 7) return "BUP";
		
		else return "undefined_type";
	}*/
	
	public boolean setLengthAndMode() throws IOException
	{
		if (readOffset+4 >= streamData.length)
		{
			// end of data buffer reached
			streamData = null;
			return false;
		}
		else
		{
			int b1 = (streamData[readOffset++]<<24)>>>24;
			int b2 = (streamData[readOffset++]<<24)>>>24;
			int b3 = (streamData[readOffset++]<<24)>>>24;
			//System.out.println(" b1:"+b1+" b2:"+b2+" b3:"+b3);
			currentLength = (int)(b1 | b2<<8 | b3 <<16);
			
			if (readOffset+currentLength >= streamData.length){
				throw new IOException(Messages.getString("StreamFileParser.0")); //$NON-NLS-1$
			}
			
			currentType = streamData[readOffset++];
			//System.out.println("Length "+currentLength+" mode "+currentType);
						
			return true;
		}
	}
	
	private void copyDataBlock()
	{
		Integer typeInt = Integer.valueOf(currentType);
		ByteArrayOutputStream baos = null;
		
		if (dataBlocks.containsKey(typeInt))
		{
			baos = (ByteArrayOutputStream)dataBlocks.get(typeInt);
		}
		else
		{
			baos = new ByteArrayOutputStream();
			dataBlocks.put(typeInt, baos);
		}

		baos.write(streamData,readOffset,currentLength);
		readOffset += currentLength;
	}
	
	/* Support for reading available traces */
	public Set<Integer> allTraceType() {
		return dataBlocks.keySet();
	}
}
