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

import java.util.TreeMap;
import java.util.Vector;

import com.nokia.carbide.cpp.internal.pi.analyser.NpiInstanceRepository;
import com.nokia.carbide.cpp.internal.pi.model.CusSample;
import com.nokia.carbide.cpp.internal.pi.test.AnalysisInfoHandler;
import com.nokia.carbide.cpp.internal.pi.test.PIAnalyser;


public class BupUpdater {
	
	static private BupUpdater instance = null;
	private static TreeMap<String, Integer> OneOneHexToStringMap = new TreeMap<String, Integer>();
	
	public static BupUpdater getInstance() {
		if (instance == null) {
			instance = new BupUpdater();
		}
		return instance;
	}
	
	private BupUpdater() {
		// singleton
		
		String nonPrintable[] = {
				Messages.getString("BupUpdater.0"), Messages.getString("BupUpdater.1"), Messages.getString("BupUpdater.2"), Messages.getString("BupUpdater.3"), Messages.getString("BupUpdater.4"), Messages.getString("BupUpdater.5"), Messages.getString("BupUpdater.6"), Messages.getString("BupUpdater.7"), Messages.getString("BupUpdater.8"), Messages.getString("BupUpdater.9"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				Messages.getString("BupUpdater.10"), Messages.getString("BupUpdater.11"), Messages.getString("BupUpdater.12"), Messages.getString("BupUpdater.13"), Messages.getString("BupUpdater.14"), Messages.getString("BupUpdater.15"), Messages.getString("BupUpdater.16"), Messages.getString("BupUpdater.17"), Messages.getString("BupUpdater.18"), Messages.getString("BupUpdater.19"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				Messages.getString("BupUpdater.20"), Messages.getString("BupUpdater.21"), Messages.getString("BupUpdater.22"), Messages.getString("BupUpdater.23"), Messages.getString("BupUpdater.24"), Messages.getString("BupUpdater.25"), Messages.getString("BupUpdater.26"), Messages.getString("BupUpdater.27"), Messages.getString("BupUpdater.28"), Messages.getString("BupUpdater.29"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				Messages.getString("BupUpdater.30"), Messages.getString("BupUpdater.31"), Messages.getString("BupUpdater.32") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			};
		for (int i = 0; i < 33; i++) {
			OneOneHexToStringMap.put(nonPrintable[i], i);
		}
		for (int i = 33; i < 127; i++) {
			OneOneHexToStringMap.put(Character.toString ((char) i), i);
		}
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.33"), 127); //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.34"), 63495);	 //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.35"), 63496);	 //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.36"), 63497);	 //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.37"), 63498); //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.38"), 63499);	 //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.39"), 63554); //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.40"), 63555); //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.41"), 63557);	 //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.42"), 63570); //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.43"), 63586);		 //$NON-NLS-1$
		OneOneHexToStringMap.put(Messages.getString("BupUpdater.44"), 63587); //$NON-NLS-1$

	}
	
	public void convertToLatest(Vector samples) {
		String fileVersion = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler().getFileVersion();
		float version = 0;
		try  {
			version = Float.parseFloat(fileVersion);
		} catch (NumberFormatException e) {
			version = -1;
		}

		if (!fileVersion.equals(PIAnalyser.NPIFileFormat)) {		
			
			if (   fileVersion.startsWith(com.nokia.carbide.cpp.internal.pi.test.Messages.getString("AnalysisInfoHandler.unknownFileVersion")) //$NON-NLS-1$
					|| fileVersion.startsWith(com.nokia.carbide.cpp.internal.pi.test.Messages.getString("AnalysisInfoHandler.unknown")) //$NON-NLS-1$
					|| version <= Float.parseFloat("1.0")) //$NON-NLS-1$ // do not externalize this version number
			{
				NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler().setFileVersion(PIAnalyser.NPIFileFormat);
				Vector <BupSample> bupSamples = new Vector <BupSample>();
				// set additional info on file to proper profile
				AnalysisInfoHandler handler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
				ButtonPlugin.getDefault().setBupMapProfileToInfoHandler(handler, BupEventMapManager.getInstance().getLegacyProfile());
				IBupEventMap map = BupEventMapManager.getInstance().captureMap(BupEventMapManager.getInstance().getLegacyProfile());
				for (int i = 0; i < samples.size(); i++)
				{
					CusSample sample = (CusSample)samples.elementAt(i);
					BupSample bupSample = convertFromOneZeroOrBefore(sample, map);
					bupSamples.add(bupSample);
				}
				BupEventMapManager.getInstance().releaseMap(map);
				samples.clear();
				samples.addAll(bupSamples);
			} else if (version <= Float.parseFloat("1.1")) {	//$NON-NLS-1$	// do not externalize this version number
				// 1.2 introduce BupSample instead of using CusSample, do conversion for any file lower than this
				NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler().setFileVersion(PIAnalyser.NPIFileFormat);
				Vector <BupSample> bupSamples = new Vector <BupSample>();
				// set additional info on file to proper profile
				AnalysisInfoHandler handler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
				ButtonPlugin.getDefault().setBupMapProfileToInfoHandler(handler, BupEventMapManager.getInstance().getLegacyProfile());
				IBupEventMap map = BupEventMapManager.getInstance().captureMap(BupEventMapManager.getInstance().getLegacyProfile());
				for (int i = 0; i < samples.size(); i++)
				{
					CusSample cusSample = (CusSample) samples.elementAt(i);
					BupSample bupSample = convertFromOneOne(cusSample, map);
					bupSamples.add(bupSample);
				}
				BupEventMapManager.getInstance().releaseMap(map);
				samples.clear();
				samples.addAll(bupSamples);
			} else if (version <= Float.parseFloat("1.2")) {	//$NON-NLS-1$	// do not externalize this version number
				// 1.3 introduce isLabelModified
				Vector <BupSample> bupSamples = new Vector <BupSample>();
				AnalysisInfoHandler handler = NpiInstanceRepository.getInstance().activeUidGetAnalysisInfoHandler();
				// 2.0 M1 does not have current profile saved yet, reset to default
				IBupEventMapProfile profile = ButtonPlugin.getDefault().getBupMapProfileFromInfoHandler(handler);
				ButtonPlugin.getDefault().setBupMapProfileToInfoHandler(handler, profile);
				IBupEventMap map = BupEventMapManager.getInstance().captureMap(profile);
				for (int i = 0; i < samples.size(); i++)
				{
					BupSample bupSample = (BupSample) samples.elementAt(i);
					bupSamples.add(convertFromOneTwo(bupSample, map));
				}
				BupEventMapManager.getInstance().releaseMap(map);
				samples.clear();
				samples.addAll(bupSamples);
			}
		}		
	}
	
	
	// 1.0 or before
	// base on CustSample
	// CustSample.name:
	// unmodified name = hex string of corresponding key code e.g. 1 button is "49"
	// modified name = label string typed
	// CustSample.comment
	

	public BupSample convertFromOneZeroOrBefore(CusSample oldSample, IBupEventMap map)
	{
		int hexCode = 0;
		BupSample bupSample = null;
	    try {
	    	// 1.0 or before save the hex string of corresponding key code e.g. 1 button is "49"
	    	if (oldSample.name == null) {
		    	bupSample = new BupSample(oldSample.sampleSynchTime, 0, map);
		    	bupSample.setLabel(""); //$NON-NLS-1$
	    	} else  {
		    	hexCode = Integer.parseInt(oldSample.name);
		    	bupSample = new BupSample(oldSample.sampleSynchTime, hexCode, map);	    		
	    	}
	    } catch (NumberFormatException exc) {
	    	// a modified label with hexcode zero as we cannot recover that with 1.0
	    	bupSample = new BupSample(oldSample.sampleSynchTime, 0, map);
	    	bupSample.setLabel(oldSample.name);
		}
	    bupSample.setComment(oldSample.comment);
	    return bupSample;
	}	
	
	// 1.1
	// base on CustSample
	// CustSample.name:
	// 0-33		ASCII name shorthand
	// 33-126	printable ASCII char
	// 127		DEL
	// 128-		hex string for corresponding key
	// CustSample.comment
	public BupSample convertFromOneOne(CusSample oldSample, IBupEventMap map) {
		Integer keyCode = 0;
		keyCode = OneOneHexToStringMap.get(oldSample.name);
		if (keyCode == null) {
			try {
				keyCode = Integer.parseInt(oldSample.name);
			} catch (NumberFormatException e) {
				// it may be just a label
			}
		}
		BupSample bupSample = new BupSample(oldSample.sampleSynchTime, keyCode != null ? keyCode : 0, map);
		if (keyCode == null) {
			// this is a modified key label
			bupSample.setLabel(oldSample.name);
		}
		bupSample.setComment(oldSample.comment);
		return bupSample;
	}

	// 1.2
	// base on BupSample
	// BupSample.keyCode: hex key code of event
	// BupSample.label: label to display
	// BupSample.comment: comment for label
	public BupSample convertFromOneTwo(BupSample oldSample, IBupEventMap map) {
		BupSample bupSample = new BupSample(oldSample.sampleSynchTime, oldSample.getKeyCode(), map);
		bupSample.setComment(oldSample.getComment());
		// retain modified label
		if (!oldSample.getLabel().equals(map.getLabel(oldSample.getKeyCode()))) {
			bupSample.setLabel(oldSample.getLabel());
		}
		return bupSample;
	}

	// 1.3
	// base on BupSample(same serial UID, additional field)
	// BupSample.keyCode: hex key code of event
	// BupSample.label: label to display
	// BupSample.comment: comment for label	
	// BupSample.labelModified: label is modified, not from mapping
}
