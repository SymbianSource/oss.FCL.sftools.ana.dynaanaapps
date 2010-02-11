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

package com.nokia.carbide.cpp.pi.button;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.model.GenericTrace;
import com.nokia.carbide.cpp.internal.pi.model.ParsedTraceData;
import com.nokia.carbide.cpp.internal.pi.model.Parser;
import com.nokia.carbide.cpp.pi.importer.SampleImporter;
import com.nokia.carbide.cpp.pi.util.GeneralMessages;
import com.nokia.carbide.cpp.sdk.core.ISymbianSDK;
import com.nokia.carbide.cpp.sdk.core.SDKCorePlugin;


public class BupTraceParser extends Parser
{
	private BupSample[] traceData;	
	private boolean debug = false;

	public BupTraceParser() throws Exception
	{
	}
		
	public ParsedTraceData parse(File file) throws Exception 
	{
		if (!file.exists() || file.isDirectory())
		{
			throw new Exception(Messages.getString("BupTraceParser.unableToOpenTrace")); //$NON-NLS-1$
		}
		
		Vector<BupSample> intermediateTraceData = new Vector<BupSample>();
		IBupEventMap map = null;
		try 
		{
			FileInputStream fis = new FileInputStream(file);
			byte[] data = new byte[(int)file.length()];
			fis.read(data);
			
			this.traceVersion = this.getVersion(data);
			System.out.println(Messages.getString("BupTraceParser.versionTitle") + this.traceVersion); //$NON-NLS-1$
			if (traceVersion.indexOf("V1.20") == -1) //$NON-NLS-1$
			{
			    System.out.println(Messages.getString("BupTraceParser.unsupportedVersion") + this.traceVersion); //$NON-NLS-1$
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
			
			int keyCode = 0;
			long sampleTime = 0;
			long sampleCount = 0;
			
			SampleImporter sampleImporter = SampleImporter.getInstance();
			IBupEventMapProfile profileForParsing = null;
			String sdkId = sampleImporter.getBupMapSymbianSDKId();
			if (sdkId != null && !sdkId.equals("")) { //$NON-NLS-1$
				ISymbianSDK sdk = SDKCorePlugin.getSDKManager().getSDK(sdkId, true);
				ArrayList<IBupEventMapProfile> profiles = BupEventMapManager.getInstance().getProfilesFromSDK(sdk);
				for (IBupEventMapProfile profile : profiles) {
					if (profile.getProfileId().equals(sampleImporter.getBupMapProfileId())) {
						profileForParsing = profile;
					}
				}
			} else if (sampleImporter.isBupMapIsWorkspace()) {
				ArrayList<IBupEventMapProfile> profiles = BupEventMapManager.getInstance().getProfilesFromWorkspacePref();
				for (IBupEventMapProfile profile : profiles) {
					if (profile.getProfileId().equals(sampleImporter.getBupMapProfileId())) {
						profileForParsing = profile;
					}
				}
			} else {
				ArrayList<IBupEventMapProfile> profiles = BupEventMapManager.getInstance().getProfilesFromBuiltin();
				for (IBupEventMapProfile profile : profiles) {
					if (profile.getProfileId().equals(sampleImporter.getBupMapProfileId())) {
						profileForParsing = profile;
					}
				}
			}
			// fall back
			if (profileForParsing == null) {
				profileForParsing = BupEventMapManager.getInstance().getDefaultProfile();
				GeneralMessages.showErrorMessage(Messages.getString("BupTraceParser.profile.cannot.load")); //$NON-NLS-1$
			}
			
			map = BupEventMapManager.getInstance().captureMap(profileForParsing);
			try
			{
				while(true)
				{
					keyCode = (int) readTUint(dis);
					sampleTime = readTUint(dis);
					sampleCount++;
					BupSample sample = new BupSample(sampleTime, keyCode, map);

					intermediateTraceData.add(sample);
//					System.out.println("Keycode: " + keyCode + " sampletime: " + sampleTime);
				}
			}
			catch (EOFException eof)
			{
				//return;
			}
			catch (Exception e)
			{
				GeneralMessages.showErrorMessage(Messages.getString("BupTraceParser.errorReadingTraceFile")); //$NON-NLS-1$
				throw e;				
			}
			BupEventMapManager.getInstance().releaseMap(map);
		} catch (Exception e)
		{
			BupEventMapManager.getInstance().releaseMap(map);
			GeneralMessages.showErrorMessage(Messages.getString("BupTraceParser.errorReadingTraceFile")); //$NON-NLS-1$
			throw e;				
		} finally {
			BupEventMapManager.getInstance().releaseMap(map);
		}
	
		if (debug)
			System.out.println(Messages.getString("BupTraceParser.traceFileParsed") + intermediateTraceData.size()); //$NON-NLS-1$
		
		// all samples have been parsed
		this.traceData = new BupSample[intermediateTraceData.size()];
		
		// store the trace data into an array
		intermediateTraceData.toArray(this.traceData);
		
		ParsedTraceData ptd = new ParsedTraceData();
		ptd.traceData = this.getTrace();
		return ptd;
	}
	
	private String getVersion(byte[] data)
	{
		int length = data[0];

		String ver = Messages.getString("BupTraceParser.unknown"); //$NON-NLS-1$
		String verString = new String(data, 1, length);
		if (debug) System.out.println(Messages.getString("BupTraceParser.version") + verString); //$NON-NLS-1$
		
		if (verString.indexOf("Bappea") != -1) //$NON-NLS-1$
			if (verString.indexOf("BUP") != -1) //$NON-NLS-1$
			{
				int index = verString.indexOf("_"); //$NON-NLS-1$
				ver = verString.substring(index+1,length);
				
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
	
	private GenericTrace getTrace()
	{
		BupTrace trace = new BupTrace();
		for (int i = 0; i < traceData.length; i++)
		{
			trace.addSample(this.traceData[i]);
		}
		return trace;
	}
}
