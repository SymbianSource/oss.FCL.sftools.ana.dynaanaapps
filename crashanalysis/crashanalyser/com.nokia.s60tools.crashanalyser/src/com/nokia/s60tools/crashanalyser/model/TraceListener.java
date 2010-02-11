/*
* Copyright (c) 2008 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
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

package com.nokia.s60tools.crashanalyser.model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import com.nokia.s60tools.crashanalyser.data.*;
import com.nokia.s60tools.crashanalyser.interfaces.IErrorLibraryObserver;
import com.nokia.s60tools.crashanalyser.files.*;
import com.nokia.s60tools.crashanalyser.export.*;
import com.nokia.s60tools.crashanalyser.plugin.*;

/**
 * This class listens MobileCrash files via TraceViewer 
 *
 */
public class TraceListener implements ITraceDataProcessor, 
										IErrorLibraryObserver  {
	private final static String MOBILE_CRASH_STARTTAG = "<MB_CR_START>"; //$NON-NLS-1$
	private final static String MOBILE_CRASH_LINE_TAG = "<MB_CD>"; //$NON-NLS-1$
	private final static String MOBILE_CRASH_STOPTAG = "<MB_CR_STOP>"; //$NON-NLS-1$
	private final static String MOBILECRASH_START = "MobileCrash_"; //$NON-NLS-1$
	final String EXTENSION_TRACE_PROVIDER = "traceprovider"; //$NON-NLS-1$


	boolean listening = false;
	boolean mobileCrashStarted = false;
	BufferedWriter mcFile = null;
	String dumpFolder = ""; //$NON-NLS-1$
	File dumpFile;
	ErrorLibrary errorLibrary = null;
	boolean decode = false;
	private static ITraceProvider traceProvider = null;
	
	/**
	 * Constructor
	 */
	public TraceListener() {
		readTraceProvider();
	}
	
	/**
	 * Starts trace listening asynchronously
	 */
	public void errorLibraryReady() {
		Runnable refreshRunnable = new Runnable(){
			public void run(){
				startListening();
			}
		};
		
		Display.getDefault().asyncExec(refreshRunnable);        		
	}
	
	/**
	 * Activates MobileCrash file listening
	 */
	public void startListening() {
		if (errorLibrary == null) {
			errorLibrary = ErrorLibrary.getInstance(this);
			return;
		}
		
		if (listening || traceProvider == null)
			return;

		if (traceProvider.start(this))
			listening = true;
	}
	
	/**
	 * Sets whether we should decode imported files or just import them as undecoded state.
	 * @param decodeFiles 
	 */
	public void setDecode(boolean decodeFiles) {
		decode = decodeFiles;
	}
	
	/**
	 * De-activates MobileCrash file listening
	 */
	public void stopListening() {
		if (!listening)
			return;
		
		if (traceProvider != null)
			traceProvider.stop();
		listening = false;
	}

	/**
	 * All lines in trace data will be passed to this method. This method
	 * pics up MobileCrash file content from trace data.
	 * @param line trace data line
	 */
	public void processDataLine(String line) {
		int idx = line.indexOf(MOBILE_CRASH_STARTTAG);
		
		try {
			// Line contained <MC_CR_START>
			if (idx > -1) { 
				mobileCrashStarted = true;
				dumpFolder = FileOperations.addSlashToEnd(DecoderEngine.getNewCrashFolder());
				
				Calendar cal = Calendar.getInstance();
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS"); //$NON-NLS-1$
			    dumpFile = new File(dumpFolder +
			    					MOBILECRASH_START + 
			    					sdf.format(cal.getTime()) + 
			    					"." + 
			    					CrashAnalyserFile.TRACE_EXTENSION);		
				
			    // create a file for this mobilecrash
			    mcFile = new BufferedWriter(new FileWriter(dumpFile));
			    mcFile.write(line);
				mcFile.newLine();
				
			// Line did not contain <MC_CR_START>, but <MB_CR_START> has been found previously (i.e we are reading a file) 
			} else if (mobileCrashStarted){ 
				
				idx = line.indexOf(MOBILE_CRASH_LINE_TAG);
				// Line contained <MB_CD>, add data to file
				if (idx > -1) { 
					mcFile.write(line);
					mcFile.newLine();
				// Line did not contain <MB_CD>
				} else { 
					idx = line.indexOf(MOBILE_CRASH_STOPTAG);
					// Line contained <MC_CR_STOP>, we can finish reading this mobile crash file
					if (idx > -1) {
						mcFile.write(line);
						mcFile.newLine();
						mcFile.close();
						mcFile = null;
						mobileCrashStarted = false;
						
						// give this mobilecrash file for further processing
						MobileCrashImporter tc = new MobileCrashImporter();
						tc.importFrom(dumpFolder, dumpFile.getName(), errorLibrary, decode);
					}
				}
			}
		} catch (Exception e) {
			if (mcFile != null) {
				try {mcFile.close();}catch (Exception E) {E.printStackTrace();}
			}
			mcFile = null;
			mobileCrashStarted = false;
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns whether we have found trace provider plugins.
	 * @return true if trace providers were found, false if not
	 */
	public static boolean traceProviderAvailable() {
		if (traceProvider == null)
			return false;
		return true;
	}
	
	/**
	 * Tries to find plugins which are Trace Providers. Selected the first found
	 * Trace provider plugin.
	 */
	void readTraceProvider() {
		try {
			IExtensionRegistry er = Platform.getExtensionRegistry();
			IExtensionPoint ep = 
				er.getExtensionPoint(CrashAnalyserPlugin.PLUGIN_ID, EXTENSION_TRACE_PROVIDER);
			IExtension[] extensions = ep.getExtensions();
			
			// if plug-ins were found.
			if (extensions != null && extensions.length > 0) {
				
				// read all found trace providers
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement[] ce = extensions[i].getConfigurationElements();
					if (ce != null && ce.length > 0) {
						try {
							ITraceProvider provider = (ITraceProvider)ce[0].createExecutableExtension("class");
							// we support only one trace provider
							if (provider != null) {
								traceProvider = provider;
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
